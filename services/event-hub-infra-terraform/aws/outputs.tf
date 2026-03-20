output "kubernetes_cluster_name" {
  value       = aws_eks_cluster.main.name
  description = "EKS Cluster Name"
}

output "kubernetes_cluster_endpoint" {
  value       = aws_eks_cluster.main.endpoint
  description = "EKS Cluster Endpoint"
}

output "workload_identity_provider_arn" {
  value       = aws_iam_openid_connect_provider.eks.arn
  description = "OIDC provider ARN used for IAM roles for service accounts"
}

output "app_iam_role_arn" {
  value       = aws_iam_role.app_irsa.arn
  description = "Application IAM role ARN for Kubernetes service account binding"
}

output "github_actions_role_arn" {
  value       = aws_iam_role.github_cicd.arn
  description = "GitHub Actions IAM role ARN for CI/CD"
}

output "ecr_repository_url" {
  value       = aws_ecr_repository.event_hub.repository_url
  description = "ECR repository URL for Event Hub images"
}

output "event_topic_arn" {
  value       = aws_sns_topic.event_hub.arn
  description = "SNS topic ARN for published events"
}

output "event_queue_url" {
  value       = aws_sqs_queue.event_hub.url
  description = "SQS queue URL for consumed events"
}

output "app_bucket_name" {
  value       = aws_s3_bucket.app_storage.bucket
  description = "S3 bucket name for Event Hub application storage"
}