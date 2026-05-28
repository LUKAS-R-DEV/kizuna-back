#!/usr/bin/env bash
# Sobe o stack KIZUNA inteiro no projeto Compose "kizuna" (um grupo no Whaler).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
KIZUNA_COMPOSE="$ROOT/scripts/kizuna-compose.sh"

export COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-kizuna}"

echo "==> Projeto Compose: $COMPOSE_PROJECT_NAME (stack único KIZUNA)"

bash "$ROOT/scripts/kizuna-cleanup-orphans.sh"

echo "==> docker compose down (projeto $COMPOSE_PROJECT_NAME)..."
"$KIZUNA_COMPOSE" down --remove-orphans 2>/dev/null || true

echo "==> docker compose up -d --build (stack completo — 13 serviços)..."
"$KIZUNA_COMPOSE" up -d --build

echo ""
echo "==> Containers no projeto $COMPOSE_PROJECT_NAME (esperado: 13):"
docker ps --filter "label=com.docker.compose.project=$COMPOSE_PROJECT_NAME" \
  --format '  {{.Names}} ({{.Status}})'
count="$(docker ps -q --filter "label=com.docker.compose.project=$COMPOSE_PROJECT_NAME" | wc -l)"
echo ""
echo "Total em execução: $count/13"
if [[ "$count" -lt 13 ]]; then
  echo "AVISO: faltam containers. Rode de novo ou verifique portas 8080/8082/8083 (IntelliJ)."
fi

echo ""
echo "Stack: http://localhost  |  Keycloak: http://localhost:8081"
echo "Dev frontend: cd frontend && npm install && npm run dev  →  http://localhost:5173"
