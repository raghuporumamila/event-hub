data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_partition" "current" {}

data "aws_iam_role" "github_cicd" {
  name = var.cicd_role_name
}

locals {
  eks_oidc_issuer_host = trimprefix(module.eks.cluster_oidc_issuer_url, "https://")
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

resource "aws_subnet" "subnet3" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.12.0/24"
  availability_zone       = data.aws_availability_zones.available.names[2]
  map_public_ip_on_launch = true

  tags = merge(local.common_tags, {
    Name                                        = "${var.subnet_name}-3"
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

resource "aws_route_table_association" "subnet3" {
  subnet_id      = aws_subnet.subnet3.id
  route_table_id = aws_route_table.public.id
}

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.34"

  cluster_name    = var.cluster_name
  cluster_version = var.cluster_version

  cluster_endpoint_private_access          = true
  cluster_endpoint_public_access           = true
  enable_cluster_creator_admin_permissions = true

  # KMS encryption: uses customer-managed key if provided, otherwise disables encryption
  # To enable module-managed KMS key creation, add IAM permissions: kms:CreateKey, kms:TagResource, kms:PutKeyPolicy, kms:CreateAlias
  create_kms_key            = false
  cluster_encryption_config = {}

  # CloudWatch Logs: disabled by default due to restricted IAM. Set create_cloudwatch_log_group=true after adding logs:CreateLogGroup permission.
  create_cloudwatch_log_group            = false
  cloudwatch_log_group_retention_in_days = var.cluster_log_retention_days
  cloudwatch_log_group_kms_key_id        = var.kms_key_arn != "" ? var.kms_key_arn : null

  vpc_id     = aws_vpc.main.id
  subnet_ids = [aws_subnet.subnet1.id, aws_subnet.subnet2.id, aws_subnet.subnet3.id]

  eks_managed_node_groups = {
    managed = {
      min_size       = var.node_group_min_size
      max_size       = var.node_group_max_size
      desired_size   = var.node_group_desired_size
      instance_types = var.node_group_instance_types
      capacity_type  = "ON_DEMAND"
      subnet_ids     = [aws_subnet.subnet1.id, aws_subnet.subnet2.id, aws_subnet.subnet3.id]
      tags           = local.common_tags
    }
  }

  access_entries = {
    github_cicd = {
      principal_arn = data.aws_iam_role.github_cicd.arn

      policy_associations = {
        admin = {
          policy_arn = "arn:${data.aws_partition.current.partition}:eks::aws:cluster-access-policy/AmazonEKSAdminPolicy"
          access_scope = {
            type = "cluster"
          }
        }
      }
    }
  }

  tags = local.common_tags

  depends_on = [
    aws_route_table_association.subnet1,
    aws_route_table_association.subnet2,
    aws_route_table_association.subnet3,
  ]
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
          Federated = module.eks.oidc_provider_arn
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