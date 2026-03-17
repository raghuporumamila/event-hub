resource "google_project_service" "compute" {
  project = var.project_id
  service = "compute.googleapis.com"
}

resource "google_project_service" "container" {
  project = var.project_id
  service = "container.googleapis.com"
}

resource "google_project_service" "storage" {
  project = var.project_id
  service = "storage.googleapis.com"
}

# Network Configuration
resource "google_compute_network" "vpc" {
  name                    = var.vpc_name
  auto_create_subnetworks = false
}

resource "google_compute_subnetwork" "subnet" {
  name          = var.subnet_name
  region        = var.region
  network       = google_compute_network.vpc.name
  ip_cidr_range = "10.0.0.0/16"
}

# Autopilot Cluster Configuration
resource "google_container_cluster" "primary" {
  name     = var.cluster_name
  location = var.region

  enable_autopilot = true
  deletion_protection = false

  network    = google_compute_network.vpc.name
  subnetwork = google_compute_subnetwork.subnet.name

  # Deleting the default node pool is not required for Autopilot 
  # as it handles node management automatically.
  release_channel {
    channel = "REGULAR"
  }
}