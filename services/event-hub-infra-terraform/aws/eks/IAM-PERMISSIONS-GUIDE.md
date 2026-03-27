# EKS Terraform IAM Permissions Guide

Your GitHub Actions CI/CD role (`event-hub-terraform-role/GitHubActions`) currently has limited permissions for EKS infrastructure provisioning. This guide explains how to:

1. **Option A**: Use an existing customer-managed KMS key (fastest path)
2. **Option B**: Add KMS and CloudWatch Logs permissions to your Terraform role (production-grade path)

## Current Status

✅ **Working**: EKS cluster, node groups, networking, and application IAM  
❌ **Blocked**: KMS key creation, CloudWatch log groups, server-side encryption for Kubernetes secrets

## Option A: Use an Existing Customer-Managed KMS Key (Fastest)

If you already have a KMS key in your AWS account:

1. Get the ARN of your customer-managed KMS key:
   ```bash
   aws kms describe-key --key-id arn:aws:kms:us-east-2:055664482203:key/12345678-1234-... \
     --query 'KeyMetadata.Arn' --output text
   ```

2. Update `terraform.tfvars` in this directory:
   ```hcl
   kms_key_arn = "arn:aws:kms:us-east-2:055664482203:key/your-key-id"
   ```

3. Run Terraform:
   ```bash
   terraform plan
   terraform apply
   ```

This will enable server-side encryption for Kubernetes secrets using your existing key.

---

## Option B: Add IAM Permissions (Production Recommended)

To let Terraform create and manage KMS keys and CloudWatch log groups, add these permissions to your `event-hub-terraform-role/GitHubActions` IAM role.

### Step 1: Create an inline policy with these permissions

Attach this policy to `arn:aws:iam::055664482203:role/event-hub-terraform-role`:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "EKSKMSKeyManagement",
      "Effect": "Allow",
      "Action": [
        "kms:CreateKey",
        "kms:CreateAlias",
        "kms:PutKeyPolicy",
        "kms:TagResource",
        "kms:UntagResource",
        "kms:DescribeKey",
        "kms:GetKeyPolicy",
        "kms:GetKeyRotationStatus",
        "kms:ListAliases",
        "kms:ListKeyPolicies",
        "kms:ListResourceTags",
        "kms:UpdateKeyDescription",
        "kms:ListKeys"
      ],
      "Resource": "arn:aws:kms:*:055664482203:key/*"
    },
    {
      "Sid": "EKSKMSAliasManagement",
      "Effect": "Allow",
      "Action": [
        "kms:CreateAlias",
        "kms:DeleteAlias",
        "kms:UpdateAlias"
      ],
      "Resource": "arn:aws:kms:*:055664482203:alias/*"
    },
    {
      "Sid": "EKSCloudWatchLogs",
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutRetentionPolicy",
        "logs:TagLogGroup",
        "logs:UntagLogGroup",
        "logs:DescribeLogGroups"
      ],
      "Resource": "arn:aws:logs:*:055664482203:log-group:/aws/eks/*"
    }
  ]
}
```

### Step 2: Update Terraform to enable KMS and logs

Once the IAM permissions are added, update the `module "eks"` block in `main.tf`:

```hcl
module "eks" {
  # ... existing config ...

  # Enable module-managed KMS key creation (now that IAM permissions are in place)
  create_kms_key = true
  cluster_encryption_config = {
    resources = ["secrets"]
  }

  # Enable CloudWatch log group creation
  create_cloudwatch_log_group      = true
  cluster_log_types                = var.cluster_log_types  # ["api", "audit"] by default
  cloudwatch_log_group_retention_in_days = var.cluster_log_retention_days  # 7 days by default
}
```

### Step 3: Plan and apply

```bash
cd /workspaces/event-hub/services/event-hub-infra-terraform/aws/eks
terraform plan
terraform apply
```

---

## Terraform Variables for Log Configuration

Control cluster logging with:

```hcl
# In terraform.tfvars
enable_cluster_logging       = true
cluster_log_types           = ["api", "audit", "authenticator", "controllerManager", "scheduler"]
cluster_log_retention_days  = 7
```

Valid log types: `api`, `audit`, `authenticator`, `controllerManager`, `scheduler`

---

## Current Temporary Workaround

Until you apply either Option A or B:
- ✅ Cluster and node groups provision successfully
- ✅ IRSA (IAM roles for service accounts) work
- ❌ Kubernetes secrets are not encrypted at rest
- ❌ No cluster logs in CloudWatch

This is safe for testing but not recommended for production.

---

## Troubleshooting

**Q**: I get "AccessDeniedException: kms:CreateKey"  
**A**: Your GitHub role doesn't have KMS permissions yet. Either:
- Use Option A (provide an existing KMS key ARN in `terraform.tfvars`)
- Complete Step 1 of Option B to add the IAM policy

**Q**: I get "AccessDeniedException: logs:CreateLogGroup"  
**A**: Same as above - add the CloudWatch Logs permissions from the policy in Option B, Step 1.

**Q**: How do I know which KMS key to use?  
**A**: 
```bash
aws kms list-keys --region us-east-2
aws kms describe-key --key-id <key-id> --region us-east-2
```

---

## References

- [Terraform AWS Modules EKS](https://github.com/terraform-aws-modules/terraform-aws-eks)
- [AWS EKS Cluster Encryption](https://docs.aws.amazon.com/eks/latest/userguide/encryption-secrets.html)
- [CloudWatch Logs Encryption with KMS](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/encrypt-log-data-kms.html)
