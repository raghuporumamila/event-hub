terraform {
  backend "s3" {
    bucket = "event-hub-terraform-prod"
    key    = "terraform/state"
    region = "us-east-2"
  }
}