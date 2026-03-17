variable "project_id" {
  description = "The GCP project ID"
  type        = string
}

variable "region" {
  description = "The GCP region for the cluster and networking"
  type        = string
  default     = "us-central1"
}

variable "cluster_name" {
  description = "The name of the GKE Autopilot cluster"
  type        = string
}

variable "vpc_name" {
  description = "The name of the VPC network"
  type        = string
}

variable "subnet_name" {
  description = "The name of the subnet"
  type        = string
}

variable "state_bucket_name" {
  description = "The name of the GCS bucket for Terraform state"
  type        = string
}