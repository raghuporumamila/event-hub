# Use this value in your GitHub Actions workflow as 'workload_identity_provider'
output "wif_provider_name" {
  description = "The full name of the Workload Identity Federation provider"
  value       = google_iam_workload_identity_pool_provider.github_provider.name
}

# Use this value in your GitHub Actions workflow as 'service_account'
output "cicd_sa_email" {
  description = "The email of the CI/CD service account"
  value       = google_service_account.cicd_sa.email
}
