data "aws_partition" "current" {}

data "tls_certificate" "github_actions" {
  url = "https://token.actions.githubusercontent.com"
}

locals {
  common_tags = {
    Project = "event-hub"
  }
}

# ---------------------------------------------------------------------------
# GitHub Actions OIDC Provider
# ---------------------------------------------------------------------------

resource "aws_iam_openid_connect_provider" "github" {
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.github_actions.certificates[0].sha1_fingerprint]
  url             = "https://token.actions.githubusercontent.com"

  tags = local.common_tags
}

# ---------------------------------------------------------------------------
# ECR Repository
# ---------------------------------------------------------------------------

resource "aws_ecr_repository" "event_hub" {
  name                 = var.ecr_repository_name
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = local.common_tags
}

# ---------------------------------------------------------------------------
# Terraform Execution Role  (AWS_TERRAFORM_ROLE_ARN)
# Assumed by GitHub Actions when running infra-terraform.yml
# ---------------------------------------------------------------------------

resource "aws_iam_role" "terraform" {
  name = var.terraform_role_name

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

  tags = local.common_tags
}

resource "aws_iam_role_policy" "terraform" {
  name = "${var.terraform_role_name}-policy"
  role = aws_iam_role.terraform.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "autoscaling:*",
          "dynamodb:*",
          "ec2:*",
          "eks:*",
          "elasticloadbalancing:*",
          "ecr:*",
          "iam:*",
          "s3:*",
          "sns:*",
          "sqs:*"
        ]
        Resource = "*"
      }
    ]
  })
}

# ---------------------------------------------------------------------------
# CI/CD Deploy Role  (AWS_DEPLOY_ROLE_ARN)
# Assumed by GitHub Actions when running event-hub-multi-cloud-ci-cd.yml
# Permissions: push images to ECR, deploy to EKS
# ---------------------------------------------------------------------------

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

  tags = local.common_tags
}

resource "aws_iam_policy" "github_cicd" {
  name = "${var.cicd_role_name}-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["ecr:GetAuthorizationToken"]
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
        Effect   = "Allow"
        Action   = ["eks:DescribeCluster"]
        Resource = "arn:${data.aws_partition.current.partition}:eks:*:*:cluster/*"
      }
    ]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "github_cicd" {
  role       = aws_iam_role.github_cicd.name
  policy_arn = aws_iam_policy.github_cicd.arn
}
