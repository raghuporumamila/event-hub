# Event Hub GitHub Workflows

This repository includes two manually-triggered GitHub Actions workflows for managing the Event Hub multi-cloud infrastructure and application deployments.

---

## Workflows

### 1. `event-hub-multi-cloud-ci-cd.yml` — Application CI/CD

Builds and deploys Event Hub application services to Kubernetes clusters across GCP, AWS, and Azure.

**Trigger:** Manual (`workflow_dispatch`)

**Services:**

| Service | Description |
|---|---|
| `schema` | Event Hub schema service |
| `backend` | Event Hub backend service |
| `site` | Event Hub frontend site |
| `db` | Event Hub database (AlloyDB StatefulSet) |

**Inputs:**

| Input | Required | Default | Description |
|---|---|---|---|
| `cloud` | Yes | `gcp` | Target cloud: `gcp`, `aws`, or `azure` |
| `deploy_target` | Yes | `all` | Service(s) to deploy: `all`, `schema`, `backend`, `site`, or `db` |
| `k8s_namespace` | Yes | `prod` | Kubernetes namespace for deployment |
| `registry_repository` | Yes | `event-hub` | Container registry repository name |
| `ar_location` | No | `us-central1` | GCP Artifact Registry location (GCP only) |
| `aws_region` | No | `us-east-2` | AWS region (AWS only) |

**How it works:**

Each service follows a two-phase pipeline — build then deploy — that runs in parallel per service, gated on the selected cloud and deploy target:

1. **Build phase** — Checks out the code, authenticates to the target cloud, builds the Docker image tagged with the short commit SHA, and pushes both a versioned tag and a `latest` tag to the cloud's container registry (GCP Artifact Registry, AWS ECR, or Azure ACR).

2. **Deploy phase** — Authenticates to the target Kubernetes cluster (GKE, EKS, or AKS), applies cloud-specific service account manifests with identity bindings, substitutes the image URI into the deployment manifest, applies it, and verifies the rollout with a 7-minute timeout. On failure, diagnostics are automatically collected (pod descriptions, logs, events).

The `db` deploy job has no build phase — it applies a pre-defined StatefulSet manifest directly and monitors the AlloyDB StatefulSet rollout.

**Required GitHub environment variables (`prod`):**

| Cloud | Variables |
|---|---|
| GCP | `GCP_PROJECT_ID`, `GCP_WIF`, `GCP_CICD_GITHUB_SA`, `GCP_APP_SERVICE_ACCOUNT_EMAIL` |
| AWS | `AWS_ACCOUNT_ID`, `AWS_DEPLOY_ROLE_ARN`, `APP_IAM_ROLE_ARN`, `EKS_CLUSTER_NAME` |
| Azure | `AZURE_DEPLOY_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID`, `AKS_CLUSTER_NAME`, `AKS_RESOURCE_GROUP`, `ACR_NAME`, `APP_AZURE_CLIENT_ID` |

---

### 2. `infra-terraform.yml` — Infrastructure Terraform

Provisions and tears down Event Hub cloud infrastructure using Terraform, with support for phased deployments and ordered destroy sequences.

**Trigger:** Manual (`workflow_dispatch`)

**Inputs:**

| Input | Required | Default | Description |
|---|---|---|---|
| `cloud` | Yes | `gcp` | Target cloud: `gcp`, `aws`, or `azure` |
| `operation` | Yes | `apply` | Terraform operation: `apply` or `destroy` |
| `gcp_target` | No | `both` | GCP target: `pre-ci-cd`, `project-services`, `gke`, or `both` |
| `aws_target` | No | `both` | AWS target: `pre-ci-cd`, `aks`, or `both` |
| `azure_target` | No | `both` | Azure target: `pre-ci-cd`, `aks`, or `both` |

**Terraform modules and execution order:**

Modules run in dependency order on `apply` and in reverse order on `destroy`:

**GCP:**
```
pre-ci-cd → project-services → gke
```
The `pre-ci-cd` module provisions the Terraform service account and Workload Identity Federation. `project-services` enables required GCP APIs. `gke` provisions the Autopilot cluster. Before running `gke`, the workflow verifies that the Terraform state bucket, required APIs, and CI/CD service account all exist.

**AWS:**
```
pre-ci-cd → aks (EKS)
```
The `pre-ci-cd` module creates the GitHub CI/CD IAM role. The `aks` module provisions the EKS cluster and verifies the CI/CD IAM role exists before proceeding.

**Azure:**
```
pre-ci-cd → aks (AKS)
```
The `pre-ci-cd` module creates the Terraform backend resource group and storage account. The `aks` module provisions the AKS cluster and verifies the backend storage account exists before proceeding.

When `operation` is `destroy` and the target is `both`, the workflow automatically runs an additional cleanup job to destroy `pre-ci-cd` resources after the dependent modules have been torn down.

Each module job runs `fmt -check`, `init`, `validate`, and `plan` before applying or destroying. Plans are saved to `tfplan` and applied in a separate step.

**Required GitHub environment variables (`prod`):**

| Cloud | Variables |
|---|---|
| GCP | `GCP_PROJECT_ID`, `GCP_WIF`, `GCP_TERRAFORM_SA_ID` |
| AWS | `AWS_REGION`, `AWS_TERRAFORM_ROLE_ARN` |
| Azure | `AZURE_TERRAFORM_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID` |

---

## Authentication

All workflows use keyless, short-lived credential authentication:

- **GCP** — Workload Identity Federation (OIDC) via `google-github-actions/auth`
- **AWS** — IAM role assumption (OIDC) via `aws-actions/configure-aws-credentials`
- **Azure** — Federated identity (OIDC) via `azure/login` with client ID + tenant + subscription

No long-lived secrets or service account keys are stored in GitHub.

---

## Running the Workflows

Both workflows are triggered manually from the **Actions** tab in GitHub. Select the workflow, click **Run workflow**, fill in the inputs, and click the green **Run workflow** button.

For a first-time cloud setup, run the Terraform workflow before the CI/CD workflow to ensure the cluster and registry infrastructure exists.
