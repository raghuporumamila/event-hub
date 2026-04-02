#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.local-run"
LOG_DIR="$RUN_DIR/logs"
PID_FILE="$RUN_DIR/pids"

DB_DIR="$ROOT_DIR/services/event-hub-db-server"
PUBSUB_DIR="$ROOT_DIR/services/event-hub-pubsub-emulator"
MODEL_DIR="$ROOT_DIR/services/event-hub-model"
BACKEND_DIR="$ROOT_DIR/services/event-hub-backend-service"
SCHEMA_DIR="$ROOT_DIR/services/event-hub-schema"
PUBLISHER_DIR="$ROOT_DIR/services/event-hub-publisher"
SITE_DIR="$ROOT_DIR/services/event-hub-site"
SEED_DB=0
RESEED_DB=0
FORCE_YES=0
ALLOYDB_CONTAINER="alloydb-omni"

SITE_PORT="${SITE_PORT:-8080}"
BACKEND_PORT="${BACKEND_PORT:-8081}"
SCHEMA_PORT="${SCHEMA_PORT:-8082}"
PUBLISHER_PORT="${PUBLISHER_PORT:-8083}"

DAO_API_ENDPOINT="${DAO_API_ENDPOINT:-http://localhost:${BACKEND_PORT}}"
SCHEMA_API_ENDPOINT="${SCHEMA_API_ENDPOINT:-http://localhost:${SCHEMA_PORT}}"
PUBLISHER_API_ENDPOINT="${PUBLISHER_API_ENDPOINT:-http://localhost:${PUBLISHER_PORT}}"

usage() {
  cat <<EOF
Usage: $(basename "$0") [--seed-db] [--reseed-db] [--yes]

Options:
  --seed-db    Run SQL initialization scripts after DB starts.
  --reseed-db  Drop and recreate eventhub database, then run SQL initialization scripts.
  --yes        Skip confirmation prompt for --reseed-db.
  -h, --help  Show this help message.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --seed-db)
      SEED_DB=1
      shift
      ;;
    --reseed-db)
      RESEED_DB=1
      SEED_DB=1
      shift
      ;;
    --yes)
      FORCE_YES=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      usage
      exit 1
      ;;
  esac
done

confirm_reseed() {
  if [[ "$RESEED_DB" -ne 1 || "$FORCE_YES" -eq 1 ]]; then
    return
  fi

  if [[ ! -t 0 ]]; then
    echo "Refusing --reseed-db without confirmation in non-interactive mode. Re-run with --yes to proceed."
    exit 1
  fi

  echo "WARNING: --reseed-db will DROP and recreate the eventhub database."
  read -r -p "Type 'yes' to continue: " response
  if [[ "$response" != "yes" ]]; then
    echo "Aborted reseed."
    exit 1
  fi
}

confirm_reseed

mkdir -p "$LOG_DIR"

if [[ -f "$PID_FILE" ]]; then
  echo "An existing run was found. Stop it first: $ROOT_DIR/scripts/local-down.sh"
  exit 1
fi

for port in "$SITE_PORT" "$BACKEND_PORT" "$SCHEMA_PORT" "$PUBLISHER_PORT"; do
  pids=$(lsof -ti ":$port" 2>/dev/null || true)
  if [[ -n "$pids" ]]; then
    echo "Port $port is already in use (PIDs: $pids). Killing..."
    echo "$pids" | xargs kill -9 2>/dev/null || true
  fi
done

cat > "$PID_FILE" <<'EOF'
EOF

cleanup_on_error() {
  echo "Startup failed. Cleaning up started processes."
  "$ROOT_DIR/scripts/local-down.sh" >/dev/null 2>&1 || true
}

trap cleanup_on_error ERR

start_service() {
  local name="$1"
  local dir="$2"
  local cmd="$3"
  local log_file="$LOG_DIR/${name}.log"

  echo "Starting ${name}..."
  (
    cd "$dir"
    nohup bash -lc "$cmd" >"$log_file" 2>&1 &
    echo "$!" >>"$PID_FILE"
  )
}

seed_db() {
  echo "Seeding database schema and sample data..."

  for i in {1..30}; do
    if docker exec "$ALLOYDB_CONTAINER" pg_isready -U postgres >/dev/null 2>&1; then
      break
    fi
    sleep 2
  done

  if [[ "$RESEED_DB" -eq 1 ]]; then
    echo "Resetting eventhub database..."
    docker exec -i "$ALLOYDB_CONTAINER" psql -U postgres -d postgres -v ON_ERROR_STOP=1 \
      -c "DROP DATABASE IF EXISTS eventhub;" \
      -c "CREATE DATABASE eventhub;"
    docker exec -i "$ALLOYDB_CONTAINER" psql -U postgres -d eventhub -v ON_ERROR_STOP=1 \
      -c "CREATE SCHEMA IF NOT EXISTS security;" \
      -c "CREATE SCHEMA IF NOT EXISTS event;"
  else
    docker exec -i "$ALLOYDB_CONTAINER" psql -U postgres < "$DB_DIR/sql/1_database.sql"
  fi

  for sql_file in \
    "$DB_DIR/sql/2_organization.sql" \
    "$DB_DIR/sql/3_role.sql" \
    "$DB_DIR/sql/4_user.sql" \
    "$DB_DIR/sql/5_workspace.sql" \
    "$DB_DIR/sql/6_source.sql" \
    "$DB_DIR/sql/7_target.sql" \
    "$DB_DIR/sql/8_event_definition.sql" \
    "$DB_DIR/sql/9_event.sql"; do
    docker exec -i "$ALLOYDB_CONTAINER" psql -U postgres -d eventhub < "$sql_file"
  done

  # Keep the latest row per email and enforce uniqueness to prevent login lookup failures.
  docker exec -i "$ALLOYDB_CONTAINER" psql -U postgres -d eventhub -v ON_ERROR_STOP=1 <<'SQL'
DELETE FROM security."user" u
USING security."user" newer
WHERE u.email = newer.email
  AND u.id < newer.id;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'uq_user_email'
      AND conrelid = 'security."user"'::regclass
  ) THEN
    ALTER TABLE security."user" ADD CONSTRAINT uq_user_email UNIQUE (email);
  END IF;
END $$;
SQL
}

echo "[1/5] Starting Docker dependencies (DB + Pub/Sub emulator)..."
(
  cd "$DB_DIR"
  docker compose up -d
)
(
  cd "$PUBSUB_DIR"
  docker compose up -d
)

if [[ "$SEED_DB" -eq 1 ]]; then
  if [[ "$RESEED_DB" -eq 1 ]]; then
    echo "[2/6] Running optional DB reseed..."
  else
    echo "[2/6] Running optional DB seed..."
  fi
  seed_db
  next_step_label="[3/6]"
  start_step_label="[4/6]"
  wait_step_label="[5/6]"
  final_step_label="[6/6]"
else
  next_step_label="[2/5]"
  start_step_label="[3/5]"
  wait_step_label="[4/5]"
  final_step_label="[5/5]"
fi

echo "$next_step_label Building shared model library..."
(
  cd "$MODEL_DIR"
  chmod +x mvnw
  ./mvnw -q clean install -DskipTests
)

echo "$start_step_label Starting Spring Boot services in background..."
start_service "backend" "$BACKEND_DIR" "chmod +x mvnw && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=${BACKEND_PORT}"
start_service "schema" "$SCHEMA_DIR" "mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=${SCHEMA_PORT}"
start_service "publisher" "$PUBLISHER_DIR" "mvn spring-boot:run -Dspring-boot.run.arguments='--server.port=${PUBLISHER_PORT} --spring.profiles.active=local'"
start_service "site" "$SITE_DIR" "DAO_API_ENDPOINT=${DAO_API_ENDPOINT} SCHEMA_API_ENDPOINT=${SCHEMA_API_ENDPOINT} PUBLISHER_API_ENDPOINT=${PUBLISHER_API_ENDPOINT} mvn spring-boot:run -Dspring-boot.run.arguments='--server.port=${SITE_PORT} --spring.profiles.active=local'"

echo "$wait_step_label Waiting for service ports..."
for i in {1..60}; do
  ok=1
  nc -z localhost "$BACKEND_PORT" >/dev/null 2>&1 || ok=0
  nc -z localhost "$SCHEMA_PORT" >/dev/null 2>&1 || ok=0
  nc -z localhost "$PUBLISHER_PORT" >/dev/null 2>&1 || ok=0
  nc -z localhost "$SITE_PORT" >/dev/null 2>&1 || ok=0
  if [[ "$ok" -eq 1 ]]; then
    break
  fi
  sleep 2
done

echo "$final_step_label Local environment status"
echo "Services logs: $LOG_DIR"
echo "PID file: $PID_FILE"
echo "UI expected on port $SITE_PORT"
echo
echo "Tail logs:"
echo "  tail -f $LOG_DIR/site.log"
echo "  tail -f $LOG_DIR/backend.log"
echo "  tail -f $LOG_DIR/schema.log"
echo "  tail -f $LOG_DIR/publisher.log"
echo
echo "Stop everything: $ROOT_DIR/scripts/local-down.sh"

trap - ERR