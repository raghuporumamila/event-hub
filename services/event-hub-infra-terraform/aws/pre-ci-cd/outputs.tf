# Set this as the GitHub Actions environment variable: AWS_TERRAFORM_ROLE_ARN
output "terraform_role_arn" {
  value       = aws_iam_role.terraform.arn
  description = "ARN of the Terraform execution IAM role (AWS_TERRAFORM_ROLE_ARN)"
}

# Set this as the GitHub Actions environment variable: AWS_DEPLOY_ROLE_ARN
output "github_cicd_role_arn" {
  value       = aws_iam_role.github_cicd.arn
  description = "ARN of the GitHub Actions CI/CD deploy IAM role (AWS_DEPLOY_ROLE_ARN)"
}

output "ecr_repository_url" {
  value       = aws_ecr_repository.event_hub.repository_url
  description = "ECR repository URL for Event Hub images"
}

output "github_oidc_provider_arn" {
  value       = aws_iam_openid_connect_provider.github.arn
  description = "ARN of the GitHub Actions OIDC provider"
}
