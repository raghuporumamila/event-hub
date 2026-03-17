output "kubernetes_cluster_name" {
  value       = azurerm_kubernetes_cluster.main.name
  description = "AKS Cluster Name"
}

output "kubernetes_cluster_endpoint" {
  value       = azurerm_kubernetes_cluster.main.kube_config.0.host
  description = "AKS Cluster Endpoint"
  sensitive   = true
}