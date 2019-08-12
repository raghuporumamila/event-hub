# event-hub-schema
This micro service is responsible for validating the event hub event against schema defined for it. This is developed using SpringBoot and https://github.com/everit-org/json-schema

## Building docker
mvn compile jib:dockerBuild

## Pushing to container registry
mvn compile jib:build

## Deploying workload to GKE
kubectl apply -f deployment

## Deploying service to GKE
kubectl apply -f service.yaml
