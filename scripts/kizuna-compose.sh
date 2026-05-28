#!/usr/bin/env bash
# Wrapper: sempre usa o projeto Compose "kizuna" (um único stack no Whaler).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT/.env"

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  COMPOSE=(docker compose)
else
  COMPOSE=(docker-compose)
fi

cd "$ROOT"

if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

export COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-kizuna}"

exec "${COMPOSE[@]}" --env-file "$ENV_FILE" -f "$ROOT/docker-compose.yml" -p "$COMPOSE_PROJECT_NAME" "$@"
