#!/usr/bin/env bash
# Sobe infra mínima no projeto Compose kizuna_dev (dev IntelliJ).
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

echo "==> Projeto Compose (dev): $COMPOSE_PROJECT_NAME"

# Aviso se stack kizuna (completo) estiver usando as mesmas portas
if docker ps --format '{{.Names}}' 2>/dev/null | grep -qE '^kizuna-(postgres|keycloak|eureka-server)$'; then
  echo ""
  echo "AVISO: Containers do projeto 'kizuna' detectados (kizuna-postgres, etc.)."
  echo "       Mesmas portas host — pare o stack completo antes:"
  echo "       docker-compose -p kizuna down"
  echo ""
fi

echo "==> docker-compose up -d (infra dev)..."
$COMPOSE --env-file "$ENV_FILE" -f "$COMPOSE_FILE" -p "$COMPOSE_PROJECT_NAME" up -d --build

echo ""
echo "==> Containers no projeto $COMPOSE_PROJECT_NAME:"
docker ps --filter "label=com.docker.compose.project=$COMPOSE_PROJECT_NAME" \
  --format '  {{.Names}} ({{.Status}})'

echo ""
echo "Infra dev pronta:"
echo "  Postgres   localhost:5432"
echo "  Mongo      localhost:27017"
echo "  RabbitMQ   localhost:5672  (UI: http://localhost:15672)"
echo "  Keycloak   http://localhost:8081"
echo "  Eureka     http://localhost:8761"
echo ""
echo "IntelliJ: Active profiles = dev | Stack completo: ./scripts/kizuna-stack-up.sh"
