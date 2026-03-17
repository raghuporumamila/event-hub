terraform {
  backend "azurerm" {
    resource_group_name  = "event-hub-terraform-rg"
    storage_account_name = "eventhubterraformprod"
    container_name       = "tfstate"
    key                  = "terraform.tfstate"
    # Location: eastus2
  }
}