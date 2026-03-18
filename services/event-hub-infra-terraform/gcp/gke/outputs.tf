output "kubernetes_cluster_name" {
  value       = google_container_cluster.primary.name
  description = "GKE Cluster Name"
}

output "kubernetes_cluster_host" {
  value       = google_container_cluster.primary.endpoint
  description = "GKE Cluster Host"
}

output "workload_identity_pool" {
  value       = "${var.project_id}.svc.id.goog"
  description = "Workload Identity Pool for Kubernetes service account binding"
}

output "app_service_account_email" {
  value       = google_service_account.app_sa.email
  description = "Application Service Account Email for Workload Identity binding"
}