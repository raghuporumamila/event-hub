# Local Scripts Guide

This folder contains helper scripts to run the Event Hub services locally in Codespaces.

## Scripts

- `local-up.sh`: starts local dependencies and Spring Boot services.
- `local-down.sh`: stops services started by `local-up.sh` and shuts down Docker dependencies.

## Prerequisites

- Docker and Docker Compose available in the environment.
- Java and Maven available in the environment.
- Run commands from repository root (`/workspaces/event-hub`).

## Startup Script

```bash
./scripts/local-up.sh [--seed-db] [--reseed-db] [--yes]
```

### Flags

- `--seed-db`: Runs DB initialization SQL scripts after DB container starts.
- `--reseed-db`: Drops and recreates `eventhub` database, then runs initialization SQL scripts.
- `--yes`: Skips confirmation prompt for `--reseed-db`.
- `-h`, `--help`: Shows usage.

### Important behavior

- `--reseed-db` implies `--seed-db`.
- In non-interactive mode, `--reseed-db` requires `--yes`.
- If a previous run is active (`.local-run/pids` exists), `local-up.sh` exits and asks you to stop first.

## Common Usage

### 1) Start services only

```bash
./scripts/local-up.sh
```

### 2) First-time run with DB seed

```bash
./scripts/local-up.sh --seed-db
```

### 3) Clean reset for repeatable testing

```bash
./scripts/local-up.sh --reseed-db
```

### 4) CI/non-interactive clean reset

```bash
./scripts/local-up.sh --reseed-db --yes
```

### 5) Stop everything

```bash
./scripts/local-down.sh
```

## What local-up.sh starts

1. Docker dependencies:
   - AlloyDB/Postgres (from `services/event-hub-db-server`)
   - Pub/Sub emulator (from `services/event-hub-pubsub-emulator`)
2. Shared library build:
   - `services/event-hub-model`
3. Spring Boot services (background):
   - Backend: port `8081`
   - Schema: port `8082`
   - Publisher: port `8083`
   - Site: port `8080`

## Logs and Runtime Files

- Logs directory: `.local-run/logs`
  - `.local-run/logs/backend.log`
  - `.local-run/logs/schema.log`
  - `.local-run/logs/publisher.log`
  - `.local-run/logs/site.log`
- PID file: `.local-run/pids`

Example log tail:

```bash
tail -f .local-run/logs/site.log
```

## Troubleshooting

### Services do not start

1. Check logs:

```bash
tail -n 200 .local-run/logs/backend.log
tail -n 200 .local-run/logs/schema.log
tail -n 200 .local-run/logs/publisher.log
tail -n 200 .local-run/logs/site.log
```

2. Ensure ports are available:

```bash
ss -ltnp | grep -E ':8080|:8081|:8082|:8083|:5432|:8085'
```

3. Restart cleanly:

```bash
./scripts/local-down.sh
./scripts/local-up.sh
```

### Reseed confirmation blocks automation

Use:

```bash
./scripts/local-up.sh --reseed-db --yes
```
