#!/bin/bash
echo "Starting Transport Management Service environment..."
set -e

# ---------- Helper Functions ----------

# Check whether a docker compose project is running
is_compose_running() {
  local dir="$1"

  if [ ! -f "$dir/docker-compose.yml" ]; then
    return 1
  fi

  # Check if any container in the project is "Up"
  docker compose -f "$dir/docker-compose.yml" ps --status running | grep -q "Up"
}

# Start docker compose project only if not already running
start_compose_if_needed() {
  local dir="$1"
  local name="$2"

  if is_compose_running "$dir"; then
    echo "✔ $name already running — skipping startup"
  else
    echo "▶ Starting $name..."
    (cd "$dir" && docker compose up -d)
    echo "✔ $name started"
  fi
}

# ---------- Locate Platform Directory ----------

PLATFORM_DIR=""

if [ -d "../limport-platform" ]; then
  PLATFORM_DIR="../limport-platform"
elif [ -d "../Limport-platform" ]; then
  PLATFORM_DIR="../Limport-platform"
fi

# ---------- Start Shared Infrastructure ----------

if [ -n "$PLATFORM_DIR" ]; then
  echo "Checking shared infrastructure in: $PLATFORM_DIR"
  start_compose_if_needed "$PLATFORM_DIR" "Shared Infrastructure"
else
  echo "⚠ No shared platform directory found — skipping"
fi

# ---------- Start Transport Management Service Containers ----------

start_compose_if_needed "." "Transport Management Service Containers"

echo "Environment ready ✔"