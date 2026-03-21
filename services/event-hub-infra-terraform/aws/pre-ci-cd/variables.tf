variable "region" {
  description = "The AWS region"
  type        = string
  default     = "us-east-2"
}

variable "github_repository" {
  description = "GitHub repository in the format 'owner/repo'"
  type        = string
}

variable "terraform_role_name" {
  description = "IAM role name for Terraform execution via GitHub Actions (AWS_TERRAFORM_ROLE_ARN)"
  type        = string
  default     = "event-hub-terraform-role"
}

variable "cicd_role_name" {
  description = "IAM role name for CI/CD deploy jobs via GitHub Actions (AWS_DEPLOY_ROLE_ARN)"
  type        = string
  default     = "event-hub-github-cicd-role"
}

variable "ecr_repository_name" {
  description = "ECR repository name for Event Hub images"
  type        = string
  default     = "event-hub"
}
