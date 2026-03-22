locals {
  common_tags = {
    Project = "event-hub"
  }
}

# ---------------------------------------------------------------------------
# Terraform State Backend Resources
# ---------------------------------------------------------------------------

resource "azurerm_resource_group" "backend" {
  name     = "event-hub-terraform-rg"
  location = var.location
  tags     = local.common_tags
}

resource "azurerm_storage_account" "backend" {
  name                     = var.storage_account_name
  resource_group_name      = azurerm_resource_group.backend.name
  location                 = azurerm_resource_group.backend.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  tags                     = local.common_tags
}

resource "azurerm_storage_container" "backend" {
  name                  = "tfstate"
  storage_account_id    = azurerm_storage_account.backend.id
  container_access_type = "private"
}

# ---------------------------------------------------------------------------
# Azure Container Registry  (AZURE_ACR_LOGIN_SERVER)
# Equivalent to ECR (AWS) / Artifact Registry (GCP)
# ---------------------------------------------------------------------------

resource "azurerm_resource_group" "acr" {
  name     = "event-hub-acr-rg"
  location = var.location
  tags     = local.common_tags
}

resource "azurerm_container_registry" "event_hub" {
  name                = var.acr_name
  resource_group_name = azurerm_resource_group.acr.name
  location            = azurerm_resource_group.acr.location
  sku                 = "Basic"
  admin_enabled       = false
  tags                = local.common_tags
}

# ---------------------------------------------------------------------------
# Terraform Execution Service Principal  (AZURE_TERRAFORM_CLIENT_ID)
# Assumed by GitHub Actions when running infra-terraform.yml
# ---------------------------------------------------------------------------

resource "azuread_application" "terraform" {
  display_name = var.terraform_sp_name
}

resource "azuread_service_principal" "terraform" {
  client_id = azuread_application.terraform.client_id
}

resource "azuread_application_federated_identity_credential" "terraform" {
  application_id = azuread_application.terraform.id
  display_name   = "github-actions-terraform"
  description    = "GitHub Actions OIDC for Terraform execution"
  audiences      = ["api://AzureADTokenExchange"]
  issuer         = "https://token.actions.githubusercontent.com"
  subject        = "repo:${var.github_repository}:environment:prod"
}

# ---------------------------------------------------------------------------
# CI/CD Deploy Service Principal  (AZURE_DEPLOY_CLIENT_ID)
# Assumed by GitHub Actions when running event-hub-multi-cloud-ci-cd.yml
# Permissions: push images to ACR, deploy to AKS
# ---------------------------------------------------------------------------

resource "azuread_application" "github_cicd" {
  display_name = var.cicd_sp_name
}

resource "azuread_service_principal" "github_cicd" {
  client_id = azuread_application.github_cicd.client_id
}

resource "azuread_application_federated_identity_credential" "github_cicd" {
  application_id = azuread_application.github_cicd.id
  display_name   = "github-actions-cicd"
  description    = "GitHub Actions OIDC for CI/CD deploy jobs"
  audiences      = ["api://AzureADTokenExchange"]
  issuer         = "https://token.actions.githubusercontent.com"
  subject        = "repo:${var.github_repository}:environment:prod"
}

data "azurerm_subscription" "current" {}

# Grant Terraform SP Contributor role on the subscription
resource "azurerm_role_assignment" "terraform_contributor" {
  scope                = data.azurerm_subscription.current.id
  role_definition_name = "Contributor"
  principal_id         = azuread_service_principal.terraform.object_id
}

# Grant CI/CD SP AcrPush role on the container registry
resource "azurerm_role_assignment" "cicd_acr_push" {
  scope                = azurerm_container_registry.event_hub.id
  role_definition_name = "AcrPush"
  principal_id         = azuread_service_principal.github_cicd.object_id
}

# Grant CI/CD SP AKS Cluster Admin role on the subscription
# Cluster Admin (not just User) is required for kubectl apply during deployments
resource "azurerm_role_assignment" "cicd_aks_admin" {
  scope                = data.azurerm_subscription.current.id
  role_definition_name = "Azure Kubernetes Service Cluster Admin Role"
  principal_id         = azuread_service_principal.github_cicd.object_id
}
