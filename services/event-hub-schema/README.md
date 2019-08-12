# event-hub-schema
This micro service is responsible for validating the event hub event against schema defined for it.

## Building docker
mvn compile jib:dockerBuild

## Pushing to container registry
mvn compile jib:build

## Deploying to GKE
kubectl apply -f deployment

## Deploying service to GKE
kubectl apply -f service.yaml
