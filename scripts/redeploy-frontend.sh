#!/usr/bin/env bash
# Rebuild do frontend (Docker multi-stage em ./frontend) e recriação via Compose.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
KIZUNA_COMPOSE="$ROOT/scripts/kizuna-compose.sh"

export COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-kizuna}"

bash "$ROOT/scripts/kizuna-cleanup-orphans.sh"

echo "==> docker compose build frontend (./frontend)"
"$KIZUNA_COMPOSE" build frontend

echo "==> docker compose up frontend (projeto $COMPOSE_PROJECT_NAME)"
"$KIZUNA_COMPOSE" up -d --no-deps frontend

sleep 2
JS=$(curl -s http://localhost/ | grep -oP '/assets/index-[^"]+\.js' | head -1 || true)
echo "Bundle: ${JS:-desconhecido}"
curl -sf -o /dev/null -w "GET / -> %{http_code}\n" http://localhost/ || true
echo ""
echo "Frontend no stack '$COMPOSE_PROJECT_NAME'."
