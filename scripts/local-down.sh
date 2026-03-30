#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.local-run"
PID_FILE="$RUN_DIR/pids"

DB_DIR="$ROOT_DIR/services/event-hub-db-server"
PUBSUB_DIR="$ROOT_DIR/services/event-hub-pubsub-emulator"

if [[ -f "$PID_FILE" ]]; then
  echo "Stopping Spring Boot processes..."
  tac "$PID_FILE" | while read -r pid; do
    if [[ -n "$pid" ]] && kill -0 "$pid" >/dev/null 2>&1; then
      kill "$pid" >/dev/null 2>&1 || true
    fi
  done
  rm -f "$PID_FILE"
else
  echo "No PID file found."
fi

echo "Stopping Docker dependencies..."
(
  cd "$DB_DIR"
  docker compose down
)
(
  cd "$PUBSUB_DIR"
  docker compose down
)

echo "Local environment stopped."