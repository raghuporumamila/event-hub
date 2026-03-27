output "kubernetes_cluster_name" {
  value       = module.eks.cluster_name
  description = "EKS Cluster Name"
}

output "kubernetes_cluster_endpoint" {
  value       = module.eks.cluster_endpoint
  description = "EKS Cluster Endpoint"
}

output "workload_identity_provider_arn" {
  value       = module.eks.oidc_provider_arn
  description = "OIDC provider ARN used for IAM roles for service accounts"
}

output "app_iam_role_arn" {
  value       = aws_iam_role.app_irsa.arn
  description = "Application IAM role ARN for Kubernetes service account binding"
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