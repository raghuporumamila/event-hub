# Set this as the GitHub Actions environment variable: AZURE_TERRAFORM_CLIENT_ID
output "terraform_client_id" {
  value       = azuread_application.terraform.client_id
  description = "Client ID of the Terraform execution service principal (AZURE_TERRAFORM_CLIENT_ID)"
}

# Set this as the GitHub Actions environment variable: AZURE_DEPLOY_CLIENT_ID
output "github_cicd_client_id" {
  value       = azuread_application.github_cicd.client_id
  description = "Client ID of the GitHub Actions CI/CD service principal (AZURE_DEPLOY_CLIENT_ID)"
}

output "acr_name" {
  value       = azurerm_container_registry.event_hub.name
  description = "ACR registry name (ACR_NAME)"
}

output "acr_login_server" {
  value       = azurerm_container_registry.event_hub.login_server
  description = "ACR login server for Event Hub images"
}

output "storage_account_name" {
  value       = azurerm_storage_account.backend.name
  description = "Storage account name for Terraform remote state"
}
