# The Workload Identity Pool
resource "google_iam_workload_identity_pool" "github_pool" {
  workload_identity_pool_id = "github-actions-pool-v2"
  display_name              = "GitHub Actions Pool"
}

# The OIDC Provider for GitHub
resource "google_iam_workload_identity_pool_provider" "github_provider" {
  workload_identity_pool_id          = google_iam_workload_identity_pool.github_pool.workload_identity_pool_id
  workload_identity_pool_provider_id = "github-provider-v2"

  attribute_mapping = {
    "google.subject"       = "assertion.sub"
    "attribute.repository" = "assertion.repository"
    "attribute.owner"      = "assertion.repository_owner"
  }

  attribute_condition = "attribute.repository == '${var.github_repository}'"

  oidc {
    issuer_uri = "https://token.actions.githubusercontent.com"
  }
}

# Allow GitHub to impersonate the CI/CD Service Account
resource "google_service_account_iam_member" "cicd_wif_user" {
  service_account_id = google_service_account.cicd_sa.name
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github_pool.name}/attribute.repository/${var.github_repository}"
}

# Allow GitHub to impersonate the Terraform Service Account
resource "google_service_account_iam_member" "terraform_wif_user" {
  service_account_id = "projects/${var.project_id}/serviceAccounts/${var.terraform_service_account_id}"
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github_pool.name}/attribute.repository/${var.github_repository}"
}

# Required APIs for managing project IAM and project services via Terraform.
resource "google_project_service" "serviceusage" {
  project            = var.project_id
  service            = "serviceusage.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "cloudresourcemanager" {
  project            = var.project_id
  service            = "cloudresourcemanager.googleapis.com"
  disable_on_destroy = false
  depends_on         = [google_project_service.serviceusage]
}

# Allow Terraform service account to read and write remote state objects in GCS.
resource "google_project_iam_member" "terraform_storage_object_admin" {
  project = var.project_id
  role    = "roles/storage.objectAdmin"
  member  = "serviceAccount:${var.terraform_service_account_id}"
  depends_on = [google_project_service.cloudresourcemanager]
}

# Allow Terraform SA to list/enable/disable Google APIs via google_project_service.
resource "google_project_iam_member" "terraform_service_usage_admin" {
  project = var.project_id
  role    = "roles/serviceusage.serviceUsageAdmin"
  member  = "serviceAccount:${var.terraform_service_account_id}"
  depends_on = [google_project_service.cloudresourcemanager]
}

# Required for APIs that enforce consumer project usage checks.
resource "google_project_iam_member" "terraform_service_usage_consumer" {
  project = var.project_id
  role    = "roles/serviceusage.serviceUsageConsumer"
  member  = "serviceAccount:${var.terraform_service_account_id}"
  depends_on = [google_project_service.cloudresourcemanager]
}

# Allow Terraform SA to create/manage VPC networks and subnets.
resource "google_project_iam_member" "terraform_compute_network_admin" {
  project = var.project_id
  role    = "roles/compute.networkAdmin"
  member  = "serviceAccount:${var.terraform_service_account_id}"
  depends_on = [google_project_service.cloudresourcemanager]
}

# Allow Terraform SA to create and manage GKE clusters.
resource "google_project_iam_member" "terraform_container_cluster_admin" {
  project = var.project_id
  role    = "roles/container.clusterAdmin"
  member  = "serviceAccount:${var.terraform_service_account_id}"
  depends_on = [google_project_service.cloudresourcemanager]
}

# Allow Terraform SA to create and manage service accounts.
resource "google_project_iam_member" "terraform_service_account_admin" {
  project = var.project_id
  role    = "roles/iam.serviceAccountAdmin"
  member  = "serviceAccount:${var.terraform_service_account_id}"
  depends_on = [google_project_service.cloudresourcemanager]
}

# Ensure Terraform SA has object permissions on the exact remote state bucket.
resource "google_storage_bucket_iam_member" "terraform_state_bucket_object_admin" {
  bucket = var.terraform_state_bucket
  role   = "roles/storage.objectAdmin"
  member = "serviceAccount:${var.terraform_service_account_id}"
}

# CI/CD Service Account
resource "google_service_account" "cicd_sa" {
  account_id   = "github-cicd-sa"
  display_name = "CI/CD Service Account"
  description  = "Service account for CI/CD operations"
}

# Grant Artifact Registry Reader role
resource "google_project_iam_member" "cicd_artifact_registry_reader" {
  project = var.project_id
  role    = "roles/artifactregistry.reader"
  member  = "serviceAccount:${google_service_account.cicd_sa.email}"
  depends_on = [google_project_service.cloudresourcemanager]
}

# Grant Artifact Registry Writer role
resource "google_project_iam_member" "cicd_artifact_registry_writer" {
  project = var.project_id
  role    = "roles/artifactregistry.writer"
  member  = "serviceAccount:${google_service_account.cicd_sa.email}"
  depends_on = [google_project_service.cloudresourcemanager]
}

# Grant GKE Developer role for deployments
resource "google_project_iam_member" "cicd_gke_developer" {
  project = var.project_id
  role    = "roles/container.developer"
  member  = "serviceAccount:${google_service_account.cicd_sa.email}"
  depends_on = [google_project_service.cloudresourcemanager]
}