# Interoperabilidade (read-only) — kizuna-data-service

API para sistemas externos **lerem** métricas e eventos agregados do KIZUNA. Não há escrita nem alteração de dados.

## Base URL

- Docker / gateway: `http://localhost/data/integration/v1`
- Direto no serviço: `http://localhost:8085/data/integration/v1`

Context path do serviço: `/data`.

## Autenticação

Envie a chave no header:

```http
X-API-Key: kz_live_xxxxxxxx
```

Alternativa: `Authorization: Bearer kz_live_xxxxxxxx`.

## Gestão de chaves (somente ADMIN via JWT)

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/data/integration/admin/api-keys` | Lista chaves ativas (valor mascarado) |
| POST | `/data/integration/admin/api-keys` | Cria chave (`{"name":"ERP parceiro"}`) — resposta traz a chave **uma vez** |
| PATCH | `/data/integration/admin/api-keys/{id}/disable` | Revoga chave |

Requer token Keycloak com role `ADMIN`.

## Bootstrap (Docker / dev)

Defina no `.env`:

```env
KIZUNA_INTEGRATION_BOOTSTRAP_KEY=kz_live_sua_chave_fixa_dev
```

Na subida, se a chave ainda não existir no Mongo (`api_keys`), ela é registrada automaticamente.

## Endpoints de leitura

| GET | Descrição |
|-----|-----------|
| `/info` | Versão da API e metadados |
| `/metrics/summary?period=30d` | Resumo produção, estoque, qualidade |
| `/metrics/oee?period=30d` | OEE do período |
| `/production/orders?period=30d&limit=50` | Lista de ordens (sem IDs internos Mongo) |
| `/inventory/items?period=30d&lowStockOnly=true` | Movimentações de estoque |
| `/quality/inspections?period=30d&result=APPROVED` | Inspeções de qualidade |

Parâmetros de período iguais ao dashboard: `period` (`7d`, `30d`, `90d`, `all`) ou `from` + `to` (ISO-8601).

## Exemplo

```bash
curl -s -H "X-API-Key: $KIZUNA_INTEGRATION_BOOTSTRAP_KEY" \
  "http://localhost/data/integration/v1/metrics/summary?period=30d" | jq
```

## Segurança

- Rotas `/integration/v1/**`: apenas API Key.
- `/dashboard/**` e `/report/**`: JWT (usuários internos).
- `/integration/admin/**`: JWT + role ADMIN.
- Endpoints antigos `public/api-key` foram removidos.
