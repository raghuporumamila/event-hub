resource "null_resource" "bootstrap_required_apis" {
  triggers = {
    project_id = var.project_id
  }

  provisioner "local-exec" {
    command = "gcloud services enable serviceusage.googleapis.com cloudresourcemanager.googleapis.com --project=${var.project_id} --quiet"
  }
}

resource "google_project_service" "serviceusage" {
  project            = var.project_id
  service            = "serviceusage.googleapis.com"
  disable_on_destroy = false
  disable_dependent_services = true
  depends_on         = [null_resource.bootstrap_required_apis]
}

resource "google_project_service" "cloudresourcemanager" {
  project            = var.project_id
  service            = "cloudresourcemanager.googleapis.com"
  disable_on_destroy = false
  disable_dependent_services = true
  depends_on         = [google_project_service.serviceusage]
}

resource "google_project_service" "compute" {
  project            = var.project_id
  service            = "compute.googleapis.com"
  disable_on_destroy = false
  disable_dependent_services = true
  depends_on         = [google_project_service.cloudresourcemanager]
}

resource "google_project_service" "container" {
  project            = var.project_id
  service            = "container.googleapis.com"
  disable_on_destroy = false
  disable_dependent_services = true
  depends_on         = [google_project_service.cloudresourcemanager]
}

resource "google_project_service" "storage" {
  project            = var.project_id
  service            = "storage.googleapis.com"
  disable_on_destroy = false
  disable_dependent_services = true
  depends_on         = [google_project_service.cloudresourcemanager]
}