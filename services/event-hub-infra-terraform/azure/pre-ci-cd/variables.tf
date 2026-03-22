variable "location" {
  description = "The Azure region for resources"
  type        = string
  default     = "East US 2"
}

variable "storage_account_name" {
  description = "The name of the storage account for Terraform state"
  type        = string
}

variable "acr_name" {
  description = "The name of the Azure Container Registry"
  type        = string
}

variable "github_repository" {
  description = "The GitHub repository in the format 'owner/repo'"
  type        = string
}

variable "terraform_sp_name" {
  description = "Display name for the Terraform execution service principal (AZURE_TERRAFORM_CLIENT_ID)"
  type        = string
  default     = "event-hub-terraform-sp"
}

variable "cicd_sp_name" {
  description = "Display name for the CI/CD deploy service principal (AZURE_DEPLOY_CLIENT_ID)"
  type        = string
  default     = "event-hub-github-cicd-sp"
}
