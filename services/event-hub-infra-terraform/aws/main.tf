data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_partition" "current" {}

data "tls_certificate" "github_actions" {
  url = "https://token.actions.githubusercontent.com"
}

locals {
  eks_oidc_issuer_host = trimprefix(aws_eks_cluster.main.identity[0].oidc[0].issuer, "https://")
  common_tags = {
    Project = "event-hub"
  }
}

resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = merge(local.common_tags, {
    Name = var.vpc_name
  })
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = merge(local.common_tags, {
    Name = "${var.vpc_name}-igw"
  })
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = merge(local.common_tags, {
    Name = "${var.vpc_name}-public"
  })
}

resource "aws_subnet" "subnet1" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.10.0/24"
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true

  tags = merge(local.common_tags, {
    Name                                        = "${var.subnet_name}-1"
    "kubernetes.io/cluster/${var.cluster_name}" = "shared"
    "kubernetes.io/role/elb"                    = "1"
  })
}

resource "aws_subnet" "subnet2" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.11.0/24"
  availability_zone       = data.aws_availability_zones.available.names[1]
  map_public_ip_on_launch = true

  tags = merge(local.common_tags, {
    Name                                        = "${var.subnet_name}-2"
    "kubernetes.io/cluster/${var.cluster_name}" = "shared"
    "kubernetes.io/role/elb"                    = "1"
  })
}

resource "aws_route_table_association" "subnet1" {
  subnet_id      = aws_subnet.subnet1.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "subnet2" {
  subnet_id      = aws_subnet.subnet2.id
  route_table_id = aws_route_table.public.id
}

resource "aws_iam_role" "eks_cluster" {
  name = "${var.cluster_name}-cluster-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "eks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "eks_cluster_policy" {
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = aws_iam_role.eks_cluster.name
}

resource "aws_iam_role" "eks_node_group" {
  name = "${var.cluster_name}-node-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "eks_worker_node_policy" {
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = aws_iam_role.eks_node_group.name
}

resource "aws_iam_role_policy_attachment" "eks_cni_policy" {
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = aws_iam_role.eks_node_group.name
}

resource "aws_iam_role_policy_attachment" "eks_ecr_pull_policy" {
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AmazonEC2ContainerRegistryPullOnly"
  role       = aws_iam_role.eks_node_group.name
}

resource "aws_eks_cluster" "main" {
  name     = var.cluster_name
  role_arn = aws_iam_role.eks_cluster.arn

  access_config {
    authentication_mode                         = "API_AND_CONFIG_MAP"
    bootstrap_cluster_creator_admin_permissions = true
  }

  vpc_config {
    endpoint_private_access = true
    endpoint_public_access  = true
    subnet_ids              = [aws_subnet.subnet1.id, aws_subnet.subnet2.id]
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_cluster_policy,
  ]

  tags = local.common_tags
}

resource "aws_eks_node_group" "main" {
  cluster_name    = aws_eks_cluster.main.name
  node_group_name = "${var.cluster_name}-managed"
  node_role_arn   = aws_iam_role.eks_node_group.arn
  subnet_ids      = [aws_subnet.subnet1.id, aws_subnet.subnet2.id]

  scaling_config {
    desired_size = var.node_group_desired_size
    max_size     = var.node_group_max_size
    min_size     = var.node_group_min_size
  }

  instance_types = var.node_group_instance_types
  capacity_type  = "ON_DEMAND"

  depends_on = [
    aws_iam_role_policy_attachment.eks_worker_node_policy,
    aws_iam_role_policy_attachment.eks_cni_policy,
    aws_iam_role_policy_attachment.eks_ecr_pull_policy,
  ]

  tags = local.common_tags
}

data "tls_certificate" "eks" {
  url = aws_eks_cluster.main.identity[0].oidc[0].issuer
}

resource "aws_iam_openid_connect_provider" "eks" {
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.eks.certificates[0].sha1_fingerprint]
  url             = aws_eks_cluster.main.identity[0].oidc[0].issuer
}

resource "aws_iam_openid_connect_provider" "github" {
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.github_actions.certificates[0].sha1_fingerprint]
  url             = "https://token.actions.githubusercontent.com"
}

resource "aws_ecr_repository" "event_hub" {
  name                 = var.ecr_repository_name
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = local.common_tags
}

resource "aws_s3_bucket" "app_storage" {
  bucket = var.app_bucket_name

  tags = merge(local.common_tags, {
    Name = var.app_bucket_name
  })
}

resource "aws_s3_bucket_versioning" "app_storage" {
  bucket = aws_s3_bucket.app_storage.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_sns_topic" "event_hub" {
  name = var.event_topic_name

  tags = local.common_tags
}

resource "aws_sqs_queue" "event_hub" {
  name = var.event_queue_name

  tags = local.common_tags
}

resource "aws_sns_topic_subscription" "event_hub_queue" {
  topic_arn = aws_sns_topic.event_hub.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.event_hub.arn
}

resource "aws_sqs_queue_policy" "event_hub" {
  queue_url = aws_sqs_queue.event_hub.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowSnsPublish"
        Effect = "Allow"
        Principal = {
          Service = "sns.amazonaws.com"
        }
        Action   = "sqs:SendMessage"
        Resource = aws_sqs_queue.event_hub.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_sns_topic.event_hub.arn
          }
        }
      }
    ]
  })
}

resource "aws_iam_role" "app_irsa" {
  name = var.app_iam_role_name

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = aws_iam_openid_connect_provider.eks.arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "${local.eks_oidc_issuer_host}:aud" = "sts.amazonaws.com"
            "${local.eks_oidc_issuer_host}:sub" = "system:serviceaccount:${var.kubernetes_namespace}:${var.kubernetes_service_account_name}"
          }
        }
      }
    ]
  })
}

resource "aws_iam_policy" "app_runtime" {
  name = "${var.app_iam_role_name}-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "sns:Publish"
        ]
        Resource = aws_sns_topic.event_hub.arn
      },
      {
        Effect = "Allow"
        Action = [
          "sqs:ChangeMessageVisibility",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes",
          "sqs:GetQueueUrl",
          "sqs:ReceiveMessage"
        ]
        Resource = aws_sqs_queue.event_hub.arn
      },
      {
        Effect = "Allow"
        Action = [
          "s3:*"
        ]
        Resource = [
          aws_s3_bucket.app_storage.arn,
          "${aws_s3_bucket.app_storage.arn}/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "app_runtime" {
  role       = aws_iam_role.app_irsa.name
  policy_arn = aws_iam_policy.app_runtime.arn
}

resource "aws_iam_role" "github_cicd" {
  name = var.cicd_role_name

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = aws_iam_openid_connect_provider.github.arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
          }
          StringLike = {
            "token.actions.githubusercontent.com:sub" = "repo:${var.github_repository}:*"
          }
        }
      }
    ]
  })
}

resource "aws_iam_policy" "github_cicd" {
  name = "${var.cicd_role_name}-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:BatchGetImage",
          "ecr:CompleteLayerUpload",
          "ecr:GetDownloadUrlForLayer",
          "ecr:InitiateLayerUpload",
          "ecr:PutImage",
          "ecr:UploadLayerPart"
        ]
        Resource = aws_ecr_repository.event_hub.arn
      },
      {
        Effect = "Allow"
        Action = [
          "eks:DescribeCluster"
        ]
        Resource = aws_eks_cluster.main.arn
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "github_cicd" {
  role       = aws_iam_role.github_cicd.name
  policy_arn = aws_iam_policy.github_cicd.arn
}

resource "aws_eks_access_entry" "github_cicd" {
  cluster_name  = aws_eks_cluster.main.name
  principal_arn = aws_iam_role.github_cicd.arn
  type          = "STANDARD"
}

resource "aws_eks_access_policy_association" "github_cicd" {
  cluster_name  = aws_eks_cluster.main.name
  principal_arn = aws_iam_role.github_cicd.arn
  policy_arn    = "arn:${data.aws_partition.current.partition}:eks::aws:cluster-access-policy/AmazonEKSAdminPolicy"

  access_scope {
    type = "cluster"
  }
}