variable "region" {
  description = "The AWS region for the cluster and networking"
  type        = string
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "The name of the EKS cluster"
  type        = string
}

variable "cluster_version" {
  description = "Kubernetes version for the EKS control plane"
  type        = string
  default     = "1.31"
}

variable "vpc_name" {
  description = "The name of the VPC"
  type        = string
}

variable "subnet_name" {
  description = "The name of the subnet"
  type        = string
}

variable "state_bucket_name" {
  description = "The name of the S3 bucket for Terraform state"
  type        = string
}

variable "github_repository" {
  description = "The GitHub repository in the format 'owner/repo'"
  type        = string
}

variable "cicd_role_name" {
  description = "IAM role name assumed by GitHub Actions via OIDC"
  type        = string
  default     = "event-hub-github-cicd-role"
}

variable "terraform_admin_role_name" {
  description = "IAM role name used by Terraform executions that should retain EKS admin access"
  type        = string
  default     = "event-hub-terraform-role"
}

variable "app_iam_role_name" {
  description = "IAM role name used by the application Kubernetes service account"
  type        = string
  default     = "event-hub-app-role"
}

variable "kubernetes_namespace" {
  description = "Kubernetes namespace hosting the application service account"
  type        = string
  default     = "default"
}

variable "kubernetes_service_account_name" {
  description = "Kubernetes service account name bound to the application IAM role"
  type        = string
  default     = "event-hub-app"
}

variable "app_bucket_name" {
  description = "S3 bucket used by Event Hub workloads"
  type        = string
}

variable "event_topic_name" {
  description = "SNS topic name used as the Event Hub publisher target"
  type        = string
  default     = "event-hub-topic"
}

variable "event_queue_name" {
  description = "SQS queue name used as the Event Hub consumer target"
  type        = string
  default     = "consumer-events-subscription"
}

variable "node_group_instance_types" {
  description = "Instance types for the EKS managed node group"
  type        = list(string)
  default     = ["t3.small", "t2.small"]
}

variable "node_group_desired_size" {
  description = "Desired number of worker nodes in the EKS managed node group"
  type        = number
  default     = 2
}

variable "node_group_min_size" {
  description = "Minimum number of worker nodes in the EKS managed node group"
  type        = number
  default     = 1
}

variable "node_group_max_size" {
  description = "Maximum number of worker nodes in the EKS managed node group"
  type        = number
  default     = 3
}

variable "enable_cluster_logging" {
  description = "Enable EKS cluster logging to CloudWatch"
  type        = bool
  default     = true
}

variable "cluster_log_types" {
  description = "List of EKS cluster log types to enable (api, audit, authenticator, controllerManager, scheduler)"
  type        = list(string)
  default     = ["api", "audit"]
}

variable "cluster_log_retention_days" {
  description = "CloudWatch log group retention in days"
  type        = number
  default     = 7
}

variable "kms_key_arn" {
  description = "ARN of customer-managed KMS key for EKS cluster encryption. If not provided, cluster encryption will be disabled until IAM permissions allow module-managed key creation."
  type        = string
  default     = ""
}