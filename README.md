# Event Hub GitHub Workflows

This repository includes six manually-triggered GitHub Actions workflows for managing Event Hub infrastructure and application deployments, split by cloud provider.

## Local Development Scripts

For running services locally in Codespaces, see [scripts/Readme.md](scripts/Readme.md).

---

## Workflow Matrix

| Goal | GCP | AWS | Azure |
|---|---|---|---|
| Build and deploy app services | [event-hub-ci-cd-gcp.yml](.github/workflows/event-hub-ci-cd-gcp.yml) | [event-hub-ci-cd-aws.yml](.github/workflows/event-hub-ci-cd-aws.yml) | [event-hub-ci-cd-azure.yml](.github/workflows/event-hub-ci-cd-azure.yml) |
| Provision or destroy infrastructure | [infra-terraform-gcp.yml](.github/workflows/infra-terraform-gcp.yml) | [infra-terraform-aws.yml](.github/workflows/infra-terraform-aws.yml) | [infra-terraform-azure.yml](.github/workflows/infra-terraform-azure.yml) |
| Target selector input | `deploy_target`, `gcp_target` | `deploy_target`, `aws_target` | `deploy_target`, `azure_target` |
| Cloud-specific extra input | `ar_location` | `aws_region` | None |

Use the Terraform workflow first for a new environment, then run the matching CI/CD workflow for the same cloud.

### 1. Application CI/CD Workflows

The application delivery workflow is now split into one file per cloud provider:

| Workflow | Cloud | Extra Inputs |
|---|---|---|
| [event-hub-ci-cd-gcp.yml](.github/workflows/event-hub-ci-cd-gcp.yml) | GCP | `ar_location` |
| [event-hub-ci-cd-aws.yml](.github/workflows/event-hub-ci-cd-aws.yml) | AWS | `aws_region` |
| [event-hub-ci-cd-azure.yml](.github/workflows/event-hub-ci-cd-azure.yml) | Azure | None |

All three workflows are manually triggered with `workflow_dispatch` and support the same service targets:

| Service | Description |
|---|---|
| `schema` | Event Hub schema service |
| `backend` | Event Hub backend service |
| `site` | Event Hub frontend site |
| `db` | Event Hub database (AlloyDB StatefulSet) |

**Shared inputs:**

| Input | Required | Default | Description |
|---|---|---|---|
| `deploy_target` | Yes | `all` | Service(s) to deploy: `all`, `schema`, `backend`, `site`, or `db` |
| `k8s_namespace` | Yes | `prod` | Kubernetes namespace for deployment |
| `registry_repository` | Yes | `event-hub` | Container registry repository name |

**How they work:**

Each provider-specific workflow follows the same two-phase pipeline — build then deploy — per selected service:

1. **Build phase** — Checks out the code, authenticates to the cloud provider, builds the Docker image tagged with the short commit SHA, and pushes both a versioned tag and a `latest` tag to the provider registry.

2. **Deploy phase** — Authenticates to the target Kubernetes cluster (GKE, EKS, or AKS), applies cloud-specific workload identity or service account manifests, substitutes the image URI into the deployment manifest, applies it, and verifies rollout. On failure, diagnostics are collected automatically.

The `db` deploy job has no build phase; it applies the StatefulSet manifest directly and monitors rollout.

**Required GitHub environment variables (`prod`):**

| Cloud | Variables |
|---|---|
| GCP | `GCP_PROJECT_ID`, `GCP_WIF`, `GCP_CICD_GITHUB_SA`, `GCP_APP_SERVICE_ACCOUNT_EMAIL` |
| AWS | `AWS_ACCOUNT_ID`, `AWS_DEPLOY_ROLE_ARN`, `APP_IAM_ROLE_ARN`, `EKS_CLUSTER_NAME` |
| Azure | `AZURE_DEPLOY_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID`, `AKS_CLUSTER_NAME`, `AKS_RESOURCE_GROUP`, `ACR_NAME`, `APP_AZURE_CLIENT_ID` |

---

### 2. Infrastructure Terraform Workflows

The Terraform automation is also split into one workflow per cloud provider:

| Workflow | Cloud | Target Input |
|---|---|---|
| [infra-terraform-gcp.yml](.github/workflows/infra-terraform-gcp.yml) | GCP | `gcp_target` |
| [infra-terraform-aws.yml](.github/workflows/infra-terraform-aws.yml) | AWS | `aws_target` |
| [infra-terraform-azure.yml](.github/workflows/infra-terraform-azure.yml) | Azure | `azure_target` |

Each workflow is manually triggered with `workflow_dispatch` and shares the `operation` input with values `apply` or `destroy`.

**Inputs by workflow:**

| Workflow | Target Values |
|---|---|
| [infra-terraform-gcp.yml](.github/workflows/infra-terraform-gcp.yml) | `pre-ci-cd`, `project-services`, `gke`, `both` |
| [infra-terraform-aws.yml](.github/workflows/infra-terraform-aws.yml) | `pre-ci-cd`, `aks`, `both` |
| [infra-terraform-azure.yml](.github/workflows/infra-terraform-azure.yml) | `pre-ci-cd`, `aks`, `both` |

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

When `operation` is `destroy` and the target is `both`, each provider workflow automatically runs additional cleanup jobs to destroy `pre-ci-cd` resources after dependent modules are torn down.

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

All workflows are triggered manually from the **Actions** tab in GitHub. Select the cloud-specific workflow, click **Run workflow**, fill in the inputs, and start the run.

For a first-time cloud setup, run the cloud-specific Terraform workflow before the corresponding CI/CD workflow to ensure the cluster and registry infrastructure exist.

## Examples

### 1. GCP first-time environment setup

1. Run [infra-terraform-gcp.yml](.github/workflows/infra-terraform-gcp.yml)
2. Set `gcp_target` to `both`
3. Set `operation` to `apply`
4. After infrastructure completes, run [event-hub-ci-cd-gcp.yml](.github/workflows/event-hub-ci-cd-gcp.yml)
5. Set `deploy_target` to `all`
6. Set `k8s_namespace` to `prod`
7. Set `registry_repository` to `event-hub`
8. Set `ar_location` to your Artifact Registry region, for example `us-central1`

### 2. AWS application-only deploy

Use this when AWS infrastructure already exists and you only want to roll out application changes.

1. Run [event-hub-ci-cd-aws.yml](.github/workflows/event-hub-ci-cd-aws.yml)
2. Set `deploy_target` to `schema`, `backend`, `site`, or `all`
3. Set `k8s_namespace` to the target namespace
4. Set `registry_repository` to the ECR repository name
5. Set `aws_region` to the target AWS region, for example `us-east-2`

### 3. Azure full destroy sequence

Use this when you want GitHub Actions to tear down Azure infrastructure in dependency order.

1. Run [infra-terraform-azure.yml](.github/workflows/infra-terraform-azure.yml)
2. Set `azure_target` to `both`
3. Set `operation` to `destroy`
4. The workflow will destroy the AKS layer first
5. After that, it will automatically destroy the `pre-ci-cd` layer
