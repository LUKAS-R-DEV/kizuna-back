#!/usr/bin/env bash
# Para infra do projeto Compose kizuna_dev.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE="docker-compose"
COMPOSE_FILE="$ROOT/docker-compose.dev.yml"
ENV_FILE="$ROOT/.env.dev"
PROJECT="kizuna_dev"

cd "$ROOT"

if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi
export COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-$PROJECT}"

echo "==> docker-compose down (projeto $COMPOSE_PROJECT_NAME)..."
$COMPOSE --env-file "$ENV_FILE" -f "$COMPOSE_FILE" -p "$COMPOSE_PROJECT_NAME" down --remove-orphans

echo "Projeto kizuna_dev parado. Volumes dev preservados (kizuna_dev_*)."
