locals {
  common_tags = {
    Project = "event-hub"
  }
}

# Resource Group
resource "azurerm_resource_group" "main" {
  name     = var.resource_group_name
  location = var.location
  tags     = local.common_tags
}

# Virtual Network
resource "azurerm_virtual_network" "main" {
  name                = var.vnet_name
  address_space       = ["10.0.0.0/16"]
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  tags                = local.common_tags
}

# Subnets
resource "azurerm_subnet" "subnet1" {
  name                 = "${var.subnet_name}-1"
  resource_group_name  = azurerm_resource_group.main.name
  virtual_network_name = azurerm_virtual_network.main.name
  address_prefixes     = ["10.0.10.0/24"]
}

resource "azurerm_subnet" "subnet2" {
  name                 = "${var.subnet_name}-2"
  resource_group_name  = azurerm_resource_group.main.name
  virtual_network_name = azurerm_virtual_network.main.name
  address_prefixes     = ["10.0.11.0/24"]
}

# User-assigned Managed Identity for application Workload Identity
resource "azurerm_user_assigned_identity" "app" {
  name                = "event-hub-app-identity"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  tags                = local.common_tags
}

# AKS Cluster with OIDC issuer and Workload Identity enabled
resource "azurerm_kubernetes_cluster" "main" {
  name                = var.cluster_name
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  dns_prefix          = "eventhub"

  oidc_issuer_enabled       = true
  workload_identity_enabled = true

  default_node_pool {
    name           = "default"
    node_count     = var.node_count
    vm_size        = var.vm_size
    vnet_subnet_id = azurerm_subnet.subnet1.id
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin = "azure"
    service_cidr   = "10.1.0.0/16"
    dns_service_ip = "10.1.0.10"
  }

  tags = local.common_tags
}

# ACR - created by pre-ci-cd stack, referenced here to grant AKS pull access
data "azurerm_container_registry" "event_hub" {
  name                = "eventhubacrprod"
  resource_group_name = "event-hub-acr-rg"
}

# Grant AKS kubelet identity AcrPull so nodes can pull images at runtime
resource "azurerm_role_assignment" "aks_acr_pull" {
  scope                = data.azurerm_container_registry.event_hub.id
  role_definition_name = "AcrPull"
  principal_id         = azurerm_kubernetes_cluster.main.kubelet_identity[0].object_id
}

# Federated Identity Credential: bind Kubernetes service account to managed identity
resource "azurerm_federated_identity_credential" "app" {
  name                = "event-hub-app-federated"
  resource_group_name = azurerm_resource_group.main.name
  parent_id           = azurerm_user_assigned_identity.app.id
  audience            = ["api://AzureADTokenExchange"]
  issuer              = azurerm_kubernetes_cluster.main.oidc_issuer_url
  subject             = "system:serviceaccount:${var.kubernetes_namespace}:${var.kubernetes_service_account_name}"
}
