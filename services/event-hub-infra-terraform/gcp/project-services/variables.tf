variable "project_id" {
  description = "The GCP project ID"
  type        = string
}

variable "region" {
  description = "The GCP region used for provider configuration"
  type        = string
  default     = "us-central1"
}