# event-hub
Mono repo for event hub

## GCP API Services CI/CD

Workflow: `.github/workflows/gcp-event-hub-api-services-ci-cd.yml`

When running the workflow manually (`workflow_dispatch`), use `deploy_target` to choose what to deploy:

- `all`: builds and deploys `event-hub-schema`, `event-hub-backend-service`, and `event-hub-site`
- `schema`: builds and deploys only `event-hub-schema`
- `backend`: builds and deploys only `event-hub-backend-service`
- `site`: builds and deploys only `event-hub-site`
