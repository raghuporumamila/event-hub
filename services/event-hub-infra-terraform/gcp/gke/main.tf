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

# Autopilot Cluster Configuration with Workload Identity
resource "google_container_cluster" "primary" {
  name     = var.cluster_name
  location = var.region

  enable_autopilot = true
  deletion_protection = false

  network    = google_compute_network.vpc.name
  subnetwork = google_compute_subnetwork.subnet.name

  # Enable Workload Identity for pod authentication to GCP APIs
  workload_identity_config {
    workload_pool = "${var.project_id}.svc.id.goog"
  }

  release_channel {
    channel = "REGULAR"
  }
}

# Service Account for applications running in the cluster
resource "google_service_account" "app_sa" {
  account_id   = "event-hub-app-sa"
  display_name = "Event Hub Application Service Account"
  description  = "Service account for event-hub application pods to access GCP APIs"
}

# Grant Pub/Sub Publisher role to app service account
resource "google_project_iam_member" "app_pubsub_publisher" {
  project = var.project_id
  role    = "roles/pubsub.publisher"
  member  = "serviceAccount:${google_service_account.app_sa.email}"
}

# Grant Pub/Sub Subscriber role to app service account
resource "google_project_iam_member" "app_pubsub_subscriber" {
  project = var.project_id
  role    = "roles/pubsub.subscriber"
  member  = "serviceAccount:${google_service_account.app_sa.email}"
}

# Grant Cloud Storage access to app service account
resource "google_project_iam_member" "app_storage_admin" {
  project = var.project_id
  role    = "roles/storage.admin"
  member  = "serviceAccount:${google_service_account.app_sa.email}"
}

# Workload Identity binding: Allow Kubernetes default namespace to use app service account
resource "google_service_account_iam_member" "app_workload_identity" {
  service_account_id = "projects/${var.project_id}/serviceAccounts/${google_service_account.app_sa.email}"
  role               = "roles/iam.workloadIdentityUser"
  member             = "serviceAccount:${var.project_id}.svc.id.goog[default/event-hub-app]"
}

# Grant CI/CD service account cluster deployment access (project-level role)
resource "google_project_iam_member" "cicd_cluster_admin" {
  project = var.project_id
  role    = "roles/container.developer"
  member  = "serviceAccount:${var.cicd_service_account_email}"
}



