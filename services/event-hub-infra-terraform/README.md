# event-hub-infra

Multi-Cloud Kubernetes Infrastructure — Terraform Reference Documentation

**AWS · Azure · GCP**

---

## Table of Contents

- [Overview](#overview)
- [Repository Structure](#repository-structure)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
  - [AWS](#aws)
  - [Azure](#azure)
  - [GCP](#gcp)
- [Architecture Details](#architecture-details)
  - [AWS](#aws-architecture)
  - [Azure](#azure-architecture)
  - [GCP](#gcp-architecture)
- [Key Variables Reference](#key-variables-reference)
- [Networking Summary](#networking-summary)
- [Security Considerations](#security-considerations)
- [Remote State Configuration](#remote-state-configuration)
- [Common Operations](#common-operations)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

---

## Overview

This repository contains Terraform configurations to provision and manage Event Hub infrastructure across three cloud providers: **Amazon Web Services (AWS)**, **Microsoft Azure**, and **Google Cloud Platform (GCP)**. Each cloud module follows the same two-phase deployment pattern:

| Phase | Directory | Purpose |
|-------|-----------|---------|
| **Bootstrap** | `pre-ci-cd/` | Creates the container registry, OIDC identity federation, and CI/CD service principals/roles needed before any automated pipeline can run |
| **Cluster** | `aks/` or `gke/` | Provisions the Kubernetes cluster, networking, message broker, object storage, and workload identity bindings for the application |

The project uses **GitHub Actions** as the CI/CD platform and authenticates to each cloud provider via **keyless OIDC federation**, eliminating the need to store long-lived credentials as GitHub secrets.

### GitHub Workflow Equivalents

If you want to run the same infrastructure operations through GitHub Actions instead of locally, use the cloud-specific workflow files in `.github/workflows/`:

| Cloud | Workflow File | Target Input |
|-------|---------------|--------------|
| AWS | `infra-terraform-aws.yml` | `aws_target` |
| Azure | `infra-terraform-azure.yml` | `azure_target` |
| GCP | `infra-terraform-gcp.yml` | `gcp_target` |

These workflows mirror the same module order documented below and support both `apply` and `destroy` operations.

---

## Repository Structure

```
services/event-hub-infra-terraform/
├── aws/
│   ├── pre-ci-cd/          # ECR, OIDC provider, IAM roles for GitHub Actions
│   └── aks/                # VPC, EKS, SNS/SQS, S3, IRSA
├── azure/
│   ├── pre-ci-cd/          # ACR, federated credentials, service principals
│   └── aks/                # VNet, AKS, Workload Identity managed identity
└── gcp/
    ├── project-services/   # Enable required GCP APIs (run first)
    ├── pre-ci-cd/          # Artifact Registry, WIF pool, CI/CD service account
    └── gke/                # VPC, GKE Autopilot, Pub/Sub SA, Workload Identity
```

Each module contains the standard Terraform files:

- `main.tf` — resource definitions
- `variables.tf` — input variable declarations
- `outputs.tf` — output values
- `provider.tf` — provider and version requirements
- `backend.tf` — remote state configuration
- `terraform.tfvars` — variable values for the `prod` environment

---

## Prerequisites

### General

- Terraform >= 1.3
- Git and a GitHub account with admin access to the target repository (`raghuporumamila/event-hub`)

### AWS

- AWS CLI configured with credentials that have sufficient IAM permissions
- An S3 bucket named `event-hub-terraform-prod` in `us-east-2` must exist before running `aws/eks` (the `aws/pre-ci-cd` module does not create it)

### Azure

- Azure CLI (`az`) logged in to the target subscription
- The `azuread` and `azurerm` providers require Contributor + Azure AD permissions
- **Run `azure/pre-ci-cd` before `azure/aks`** — the pre-ci-cd module creates the storage account used as the Terraform backend for the cluster module

### GCP

- `gcloud` CLI authenticated as a project Owner or with equivalent permissions
- **Run `gcp/project-services` first** to enable `compute`, `container`, and `storage` APIs
- A GCS bucket named `event-hub-terraform-prod` must exist before applying any GCP module (create it manually or via `gsutil mb`)

---

## Quick Start

### AWS

#### Step 1 — Bootstrap (`pre-ci-cd`)

```bash
cd services/event-hub-infra-terraform/aws/pre-ci-cd
terraform init
terraform apply
```

After apply, configure the following GitHub Actions environment variables from the outputs:

| Terraform Output | GitHub Secret / Variable |
|-----------------|--------------------------|
| `terraform_role_arn` | `AWS_TERRAFORM_ROLE_ARN` |
| `github_cicd_role_arn` | `AWS_DEPLOY_ROLE_ARN` |
| `ecr_repository_url` | `AWS_ECR_REPOSITORY_URL` |

#### Step 2 — Cluster (`eks`)

```bash
cd ../eks
terraform init
terraform apply
```

Key outputs:

| Output | Description |
|--------|-------------|
| `kubernetes_cluster_name` | EKS cluster name |
| `app_iam_role_arn` | IRSA role ARN — annotate your Kubernetes service account with this |
| `event_topic_arn` | SNS topic ARN for publishing events |
| `event_queue_url` | SQS queue URL for consuming events |
| `app_bucket_name` | S3 bucket name for application storage |

---

### Azure

#### Step 1 — Bootstrap (`pre-ci-cd`)

```bash
cd services/event-hub-infra-terraform/azure/pre-ci-cd
terraform init
terraform apply
```

Configure these GitHub secrets from the outputs:

| Terraform Output | GitHub Secret / Variable |
|-----------------|--------------------------|
| `terraform_client_id` | `AZURE_TERRAFORM_CLIENT_ID` |
| `github_cicd_client_id` | `AZURE_DEPLOY_CLIENT_ID` |
| `acr_login_server` | `AZURE_ACR_LOGIN_SERVER` |

You will also need `AZURE_TENANT_ID` and `AZURE_SUBSCRIPTION_ID` from your Azure portal.

#### Step 2 — Cluster (`aks`)

```bash
cd ../aks
terraform init
terraform apply
```

Key outputs:

| Output | Description |
|--------|-------------|
| `kubernetes_cluster_name` | AKS cluster name |
| `app_managed_identity_client_id` | Set as `AZURE_CLIENT_ID` annotation on the Kubernetes service account |
| `oidc_issuer_url` | AKS OIDC issuer URL for federated credential configuration |
| `resource_group_name` | Resource group containing the cluster (`AKS_RESOURCE_GROUP`) |

---

### GCP

#### Step 0 — Enable APIs (`project-services`)

```bash
cd services/event-hub-infra-terraform/gcp/project-services
terraform init
terraform apply
```

> This step uses a `local-exec` provisioner to enable `serviceusage.googleapis.com` and `cloudresourcemanager.googleapis.com` via `gcloud` before Terraform can manage other APIs declaratively.

#### Step 1 — Bootstrap (`pre-ci-cd`)

```bash
cd ../pre-ci-cd
terraform init
terraform apply
```

Configure these GitHub Actions workflow parameters from the outputs:

| Terraform Output | GitHub Actions Parameter |
|-----------------|--------------------------|
| `wif_provider_name` | `workload_identity_provider` |
| `cicd_sa_email` | `service_account` |

#### Step 2 — Cluster (`gke`)

```bash
cd ../gke
terraform init
terraform apply
```

Key outputs:

| Output | Description |
|--------|-------------|
| `kubernetes_cluster_name` | GKE Autopilot cluster name |
| `app_service_account_email` | Annotate the Kubernetes service account with this email |
| `workload_identity_pool` | Workload Identity pool (`PROJECT_ID.svc.id.goog`) |

---

## Architecture Details

### AWS Architecture

The AWS deployment provisions a full VPC with two public subnets across availability zones, an EKS managed node group, and the supporting messaging and storage services.

| Resource | Name / Value | Notes |
|----------|-------------|-------|
| VPC | `main-eks-vpc` (10.0.0.0/16) | DNS hostnames and DNS support enabled |
| Subnets | 10.0.10.0/24, 10.0.11.0/24 | One per AZ; tagged for EKS load balancer discovery |
| Internet Gateway | `main-eks-vpc-igw` | Attached to VPC with a public route table |
| EKS Cluster | `prod-eks-cluster` | `API_AND_CONFIG_MAP` auth; public + private endpoint |
| Node Group | `prod-eks-cluster-managed` | `t3.medium`, 2 desired / 1–3 range, `ON_DEMAND` |
| SNS Topic | `event-hub-topic` | Publisher target for the application |
| SQS Queue | `consumer-events-subscription` | Subscribed to the SNS topic via `sqs` protocol |
| S3 Bucket | `event-hub-app-storage-prod` | Versioning enabled |
| IRSA Role | `event-hub-app-role` | `SNS:Publish`, SQS receive/delete/visibility, `S3:*` |
| CI/CD Role | `event-hub-github-cicd-role` | ECR push permissions + `EKS:DescribeCluster` |
| Terraform Role | `event-hub-terraform-role` | Broad permissions for infrastructure management |

**Workload Identity (IRSA):** The OIDC provider is derived from the EKS cluster's issuer URL. The `event-hub-app-role` trust policy restricts assumption to the service account `system:serviceaccount:default:event-hub-app` only.

**CI/CD Access:** The GitHub Actions CI/CD role is added as an EKS access entry with `AmazonEKSAdminPolicy` at cluster scope, allowing `kubectl apply` during deployments.

---

### Azure Architecture

The Azure deployment uses AKS with **Workload Identity** (Azure AD federated credentials) and a user-assigned managed identity for application pods.

| Resource | Name / Value | Notes |
|----------|-------------|-------|
| Resource Group | `event-hub-rg` | Contains all cluster resources; `East US 2` |
| VNet | `main-aks-vnet` (10.0.0.0/16) | Two subnets: 10.0.10.0/24, 10.0.11.0/24 |
| AKS Cluster | `prod-aks-cluster` | Azure CNI; OIDC issuer enabled; Workload Identity enabled |
| Node Pool | `default` | `Standard_D2s_v3`, 1 node (configurable) |
| Managed Identity | `event-hub-app-identity` | User-assigned; bound via federated credential |
| Federated Credential | `event-hub-app-federated` | Audience: `api://AzureADTokenExchange` |
| ACR | `eventhubacrprod` | Basic SKU; admin access disabled |
| Backend Storage | `eventhubterraformprod` | Standard LRS; container `tfstate` |
| Terraform SP | `event-hub-terraform-sp` | Contributor on subscription; OIDC federated |
| CI/CD SP | `event-hub-github-cicd-sp` | `AcrPush` on ACR + `AKS Cluster Admin Role` on subscription |

**Workload Identity binding:** The federated credential binds `system:serviceaccount:default:event-hub-app` in the AKS cluster to the managed identity, enabling pods to authenticate to Azure services without storing any credentials.

---

### GCP Architecture

GCP uses **GKE Autopilot** with Workload Identity. The application service account is granted specific IAM roles and bound to a Kubernetes service account.

| Resource | Name / Value | Notes |
|----------|-------------|-------|
| GKE Cluster | `prod-autopilot-cluster` | Autopilot mode; `REGULAR` release channel; `us-central1` |
| VPC / Subnet | `main-gke-vpc` / `gke-nodes-subnet` | 10.0.0.0/16 CIDR |
| App Service Account | `event-hub-app-sa@event-hub-317019.iam.gserviceaccount.com` | `roles/pubsub.publisher`, `roles/pubsub.subscriber`, `roles/storage.admin` |
| WIF Binding | `event-hub-app-sa` | Allows `serviceAccount:PROJECT.svc.id.goog[default/event-hub-app]` |
| CI/CD SA | `github-cicd-sa@event-hub-317019.iam.gserviceaccount.com` | `roles/artifactregistry.writer`, `roles/container.developer` |
| WIF Pool | `github-actions-pool-v3` | Attribute condition scoped to `raghuporumamila/event-hub` |
| WIF Provider | `github-provider-v3` | OIDC; maps `assertion.repository` attribute |
| Artifact Registry | `event-hub` (DOCKER) | `us-central1`; created in pre-ci-cd |
| State Bucket | `event-hub-terraform-prod` | Shared GCS bucket across all GCP modules |

**Autopilot note:** GKE Autopilot manages node provisioning automatically. The `node_count` and instance type are not configurable — Autopilot scales based on pod resource requests.

---

## Key Variables Reference

### AWS — `aws/eks/variables.tf`

| Variable | Default | Description |
|----------|---------|-------------|
| `region` | `us-east-1` | AWS region (overridden to `us-east-2` in tfvars) |
| `cluster_name` | *(required)* | EKS cluster name |
| `node_group_instance_types` | `["t3.medium"]` | EC2 instance types for the managed node group |
| `node_group_desired_size` | `2` | Target number of worker nodes |
| `node_group_min_size` | `1` | Minimum nodes for autoscaling |
| `node_group_max_size` | `3` | Maximum nodes for autoscaling |
| `kubernetes_namespace` | `default` | Namespace for IRSA trust policy |
| `kubernetes_service_account_name` | `event-hub-app` | Service account name for IRSA binding |
| `event_topic_name` | `event-hub-topic` | SNS topic name |
| `event_queue_name` | `consumer-events-subscription` | SQS queue name |
| `app_bucket_name` | *(required)* | S3 bucket name for application storage |
| `cicd_role_name` | `event-hub-github-cicd-role` | IAM role name assumed by GitHub Actions deploys |

### Azure — `azure/aks/variables.tf`

| Variable | Default | Description |
|----------|---------|-------------|
| `location` | `East US 2` | Azure region |
| `cluster_name` | *(required)* | AKS cluster name |
| `vm_size` | `Standard_D2s_v3` | Node pool VM size |
| `node_count` | `1` | Number of nodes in the default node pool |
| `kubernetes_namespace` | `default` | Namespace for Workload Identity federated credential |
| `kubernetes_service_account_name` | `event-hub-app` | Service account name for Workload Identity binding |

### GCP — `gcp/gke/variables.tf`

| Variable | Default | Description |
|----------|---------|-------------|
| `project_id` | *(required)* | GCP project ID |
| `region` | `us-central1` | GCP region for the cluster and networking |
| `cluster_name` | *(required)* | GKE cluster name |
| `vpc_name` | *(required)* | VPC network name |
| `subnet_name` | *(required)* | Subnet name |
| `cicd_service_account_email` | *(required)* | CI/CD SA email from `pre-ci-cd` outputs |

---

## Networking Summary

| Cloud | VPC / VNet CIDR | Subnet 1 | Subnet 2 | Notes |
|-------|----------------|----------|----------|-------|
| AWS | 10.0.0.0/16 | 10.0.10.0/24 (AZ-0) | 10.0.11.0/24 (AZ-1) | Both public; Internet Gateway + route table |
| Azure | 10.0.0.0/16 | 10.0.10.0/24 | 10.0.11.0/24 | Azure CNI; service CIDR 10.1.0.0/16; DNS 10.1.0.10 |
| GCP | 10.0.0.0/16 | `gke-nodes-subnet` (single) | — | Autopilot manages node networking internally |

---

## Security Considerations

**Keyless authentication** — All three cloud providers use OIDC Workload Identity Federation. No long-lived cloud credentials are stored in GitHub secrets.

**Least-privilege IAM** — The CI/CD roles are scoped to container registry push and Kubernetes describe/deploy only. Application roles are scoped to the specific SNS topic, SQS queue, and S3 bucket (or equivalent GCP/Azure resources).

**Remote state isolation** — Terraform state is stored in provider-managed object storage (S3, Azure Blob, GCS) with separate keys/prefixes per module to prevent state conflicts.

**EKS endpoint exposure** — Both public and private API server access are enabled (`endpoint_public_access = true`). For stricter environments, consider restricting `public_access_cidrs` or disabling public access entirely.

**S3 versioning** — The application S3 bucket has versioning enabled to support recovery from accidental deletion or overwrite.

**ECR image scanning** — `scan_on_push = true` is set on the ECR repository to detect vulnerabilities on every push.

**Azure admin disabled** — ACR admin credentials are disabled (`admin_enabled = false`); authentication uses service principal OIDC only.

---

## Remote State Configuration

| Cloud | Backend | Bucket / Account | Key / Prefix |
|-------|---------|-----------------|--------------|
| AWS | S3 | `event-hub-terraform-prod` (us-east-2) | `terraform/state` |
| Azure | azurerm | `eventhubterraformprod` / RG `event-hub-terraform-rg` | `aks/terraform.tfstate` |
| GCP — pre-ci-cd | GCS | `event-hub-terraform-prod` | `terraform/state/pre-ci-cd` |
| GCP — gke | GCS | `event-hub-terraform-prod` | `terraform/state/gke` |
| GCP — project-services | GCS | `event-hub-terraform-prod` | `terraform/state/project-services` |

> **Azure note:** The storage account used as the Azure backend is created by `azure/pre-ci-cd`. You must apply that module locally (with `terraform init` using a local backend or no backend) before `azure/aks` can initialise its remote backend.

---

## Common Operations

### Update kubeconfig

```bash
# AWS
aws eks update-kubeconfig --name prod-eks-cluster --region us-east-2

# Azure
az aks get-credentials --resource-group event-hub-rg --name prod-aks-cluster

# GCP
gcloud container clusters get-credentials prod-autopilot-cluster \
  --region us-central1 --project event-hub-317019
```

### Plan before applying

```bash
terraform plan -out=tfplan
terraform apply tfplan
```

### Refresh outputs without re-applying

```bash
terraform output
```

### Destroy a module

```bash
terraform destroy  # from within the module directory
```

> **Destroy order matters.** Always destroy the cluster module (`aks/` or `gke/`) before `pre-ci-cd`. Destroying `pre-ci-cd` first removes the IAM roles/service principals that Terraform needs to clean up cluster resources, leaving orphaned infrastructure.

---

## Troubleshooting

| Symptom | Likely Cause | Resolution |
|---------|-------------|------------|
| GitHub Actions: `Error assuming role` | OIDC subject condition mismatch | Verify the `sub` condition matches the workflow trigger — `environment:prod` vs `ref:refs/heads/main` differ |
| Terraform: 403 on state bucket | Service account lacks `storage.objectAdmin` on the bucket | Re-run `pre-ci-cd` or grant the role manually via `gcloud` |
| EKS nodes stuck `NotReady` | Node IAM role missing a policy attachment | Confirm all three `aws_iam_role_policy_attachment` resources for the node group applied successfully |
| AKS pod identity errors | Federated credential subject mismatch | Verify `kubernetes_namespace` and `kubernetes_service_account_name` variables match the deployed Kubernetes manifests exactly |
| GCP: `API not enabled` error | `project-services` not applied first | Run `gcp/project-services/terraform apply` before any other GCP module |
| Azure: storage account name conflict | Storage account names are globally unique across all of Azure | Change `storage_account_name` in `terraform.tfvars` to a unique value |
| `Error: Invalid provider configuration` on Azure | Missing `AZURE_TENANT_ID` or `AZURE_SUBSCRIPTION_ID` env vars | Export both before running Terraform or use `az login` |
| SNS → SQS delivery failures | SQS queue policy not allowing SNS as principal | Verify `aws_sqs_queue_policy` was applied and the `aws:SourceArn` condition matches the SNS topic ARN |

---

## Contributing

The Terraform state files and `.terraform` directories are gitignored. When adding new resources:

- Add new variables to `variables.tf` with clear `description` fields and sensible `default` values.
- Expose important resource identifiers as outputs in `outputs.tf` with descriptions noting which GitHub secret or environment variable they map to.
- Follow the `common_tags` / `local.common_tags` pattern to ensure all resources are tagged with `Project = "event-hub"`.
- Run `terraform plan` and review the diff carefully before applying, especially for IAM and networking changes.
- Prefer least-privilege IAM — avoid `Resource = "*"` except where the API requires it (e.g., `ecr:GetAuthorizationToken`).
- Keep the two-phase structure (`pre-ci-cd` → cluster) intact so the bootstrap can always be run manually without a working CI/CD pipeline.
