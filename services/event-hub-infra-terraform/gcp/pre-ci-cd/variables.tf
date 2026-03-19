variable "project_id" {
  description = "The GCP project ID"
  type        = string
}

variable "region" {
  description = "The GCP region"
  type        = string
  default     = "us-central1"
}

variable "github_repository" {
  description = "The GitHub repository in the format 'owner/repo'"
  type        = string
}

variable "terraform_service_account_id" {
  description = "The existing Terraform service account email for Workload Identity"
  type        = string
}

variable "terraform_state_bucket" {
  description = "Terraform remote state GCS bucket name"
  type        = string
  default     = "event-hub-terraform-prod"
}