# event-hub-dao
This micro service is responsible in performing CRUD operations needed for Event Hub services. This service is developed using Spring boot & Google firestore client libraries. 

From the following diagram the green highlighted service represents this micro service

![alt text](Architecture.png)

## Building docker and deploying to local Kubernetes(i.e., Docker kubeadm)
- mvn clean package 
- docker build -t event-hub-service-v2 . 
- kubectl apply -f deployment/deploy.yml 
- kubectl apply -f deployment/db-connect-external.yml 
- kubectl apply -f deployment/db-connect-endpoint.yml 
- kubectl logs -f event-hub-service-v2-app-5d64d6ddb-p6bgb 
- kubectl delete -f deployment/deploy.yml

## Deploying to GKE
kubectl apply -f deployment.yaml

## Deploying service to GKE
kubectl apply -f service.yaml

## Egress
For letting egress traffic to Firestore please run the following (Needed only if you have istio enabled on GKE namespace) <br/>
kubectl apply -f egress-rule-google-apis.yaml
