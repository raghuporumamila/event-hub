variable "location" {
  description = "The Azure region for the cluster and networking"
  type        = string
  default     = "East US 2"
}

variable "cluster_name" {
  description = "The name of the AKS cluster"
  type        = string
}

variable "resource_group_name" {
  description = "The name of the resource group"
  type        = string
}

variable "vnet_name" {
  description = "The name of the virtual network"
  type        = string
}

variable "subnet_name" {
  description = "The name of the subnet"
  type        = string
}

variable "state_storage_account_name" {
  description = "Storage account name used for the Terraform remote state backend"
  type        = string
}

variable "kubernetes_namespace" {
  description = "Kubernetes namespace hosting the application service account"
  type        = string
  default     = "default"
}

variable "kubernetes_service_account_name" {
  description = "Kubernetes service account name bound to the application managed identity"
  type        = string
  default     = "event-hub-app"
}

variable "node_count" {
  description = "Number of nodes in the default node pool"
  type        = number
  default     = 1
}

variable "vm_size" {
  description = "VM size for the default node pool"
  type        = string
  default     = "Standard_D2s_v3"
}
