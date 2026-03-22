output "kubernetes_cluster_name" {
  value       = azurerm_kubernetes_cluster.main.name
  description = "AKS Cluster Name"
}

output "kubernetes_cluster_endpoint" {
  value       = azurerm_kubernetes_cluster.main.kube_config.0.host
  description = "AKS Cluster Endpoint"
  sensitive   = true
}

output "oidc_issuer_url" {
  value       = azurerm_kubernetes_cluster.main.oidc_issuer_url
  description = "AKS OIDC Issuer URL for Workload Identity"
}

output "app_managed_identity_client_id" {
  value       = azurerm_user_assigned_identity.app.client_id
  description = "Client ID of the application managed identity for Workload Identity binding"
}

output "resource_group_name" {
  value       = azurerm_resource_group.main.name
  description = "AKS resource group name (AKS_RESOURCE_GROUP)"
}
