region       = "us-east-2"
cluster_name = "prod-eks-cluster"
vpc_name     = "main-eks-vpc"
subnet_name  = "eks-nodes-subnet"

state_bucket_name = "event-hub-terraform-prod"
github_repository = "raghuporumamila/event-hub"
app_bucket_name   = "event-hub-app-storage-prod"