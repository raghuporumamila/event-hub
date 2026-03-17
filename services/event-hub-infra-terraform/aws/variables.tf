variable "region" {
  description = "The AWS region for the cluster and networking"
  type        = string
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "The name of the EKS cluster"
  type        = string
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