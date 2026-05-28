#!/usr/bin/env bash
# Remove containers KIZUNA fora do projeto Compose "kizuna" (docker run / stacks avulsos).
set -euo pipefail

EXPECTED_PROJECT="${COMPOSE_PROJECT_NAME:-kizuna}"

echo "==> Projeto Compose esperado: $EXPECTED_PROJECT"
echo "==> Removendo containers kizuna-* fora deste projeto:"

removed=0
for c in $(docker ps -aq --filter "name=kizuna-" 2>/dev/null); do
  project=$(docker inspect -f '{{ index .Config.Labels "com.docker.compose.project" }}' "$c" 2>/dev/null || true)
  name=$(docker inspect -f '{{.Name}}' "$c" | sed 's/^\///')
  if [[ "$project" != "$EXPECTED_PROJECT" ]]; then
    echo "    $name (projeto=${project:-avulso})"
    docker rm -f "$c" >/dev/null 2>&1 || true
    removed=$((removed + 1))
  fi
done

echo "==> Containers órfãos conhecidos (builds manuais antigos):"
for c in keen_northcutt optimistic_dirac pensive_swanson frontend_source_rescue; do
  if docker inspect "$c" >/dev/null 2>&1; then
    echo "    $c"
    docker rm -f "$c" >/dev/null 2>&1 || true
    removed=$((removed + 1))
  fi
done

if [[ "$removed" -eq 0 ]]; then
  echo "    (nenhum órfão encontrado)"
fi

echo "Pronto. Use ./scripts/kizuna-stack-up.sh para subir tudo no projeto $EXPECTED_PROJECT."
