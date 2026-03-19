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
}

# Grant Artifact Registry Writer role
resource "google_project_iam_member" "cicd_artifact_registry_writer" {
  project = var.project_id
  role    = "roles/artifactregistry.writer"
  member  = "serviceAccount:${google_service_account.cicd_sa.email}"
}

# Grant GKE Developer role for deployments
resource "google_project_iam_member" "cicd_gke_developer" {
  project = var.project_id
  role    = "roles/container.developer"
  member  = "serviceAccount:${google_service_account.cicd_sa.email}"
}