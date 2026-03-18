terraform {
  backend "gcs" {
    bucket  = "event-hub-terraform-prod"
    prefix  = "terraform/state/project-services"
  }
}