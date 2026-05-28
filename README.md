<div align="center">
  <h1>KIZUNA</h1>
  <h3>Plataforma Segura de Gestão e Dados Industriais</h3>
  
  <p>
    <img src="https://img.shields.io/badge/Java|17-000000?style=for-the-badge&logo=java&logoColor=red" alt="Java"/>
    <img src="https://img.shields.io/badge/Spring_Boot-000000?style=for-the-badge&logo=springboot&logoColor=6DB33F" alt="Spring Boot"/>
    <img src="https://img.shields.io/badge/PostgreSQL-000000?style=for-the-badge&logo=postgresql&logoColor=336791" alt="PostgreSQL"/>
    <img src="https://img.shields.io/badge/Keycloak-000000?style=for-the-badge&logo=keycloak&logoColor=blue" alt="Keycloak"/>
    <img src="https://img.shields.io/badge/RabbitMQ-000000?style=for-the-badge&logo=rabbitmq&logoColor=FF6600" alt="RabbitMQ"/>
    <img src="https://img.shields.io/badge/Swagger_UI-000000?style=for-the-badge&logo=swagger&logoColor=85EA2D" alt="Swagger"/>
  </p>
</div>

---

## 🏭 VISÃO GERAL DO SISTEMA

O **KIZUNA Backend** é o núcleo (Core) da plataforma corporativa construída para operar com alta escalabilidade, segurança e eficiência no ambiente industrial. Dentre seus fluxos de atuação, gerencia dados de operadores, integra relatórios via Inteligência Artificial, avalia eficiências de produção e aplica sólidas trilhas de auditoria.

O repositório adota um paradigma de **Arquitetura Orientada a Microsserviços**, promovendo alta coesão estrutural e isolamento de banco de dados por domínio.

<br/>

## 🏗️ ARQUITETURA E PADRÕES ADOTADOS

O desenvolvimento deste backend consolidou diversas boas práticas de engenharia de software para lidar com tráfego distribuído:

1. **Service Discovery e API Gateway**
   - O tráfego externo é inteiramente filtrado, passando passivamente só pelo `gateway-service`; impedindo que clientes conheçam as URLs reais ou topologia da infraestrutura.
   - O tráfego interno (Server-to-Server) é sustentado de modo declarativo via clientes inteligentes (`FeignClient`) acoplados ao `eureka-server`, provendo roteamento e balanceamento por nomes, sem IP's estáticos.

2. **Event-Driven Architecture (EDA)**
   - Operações pesadas e ações de auditoria comunicam-se de forma assíncrona por intermédio das filas do broker **RabbitMQ**.
   - O serviço principal de negócios (`Core Service`) lança eventos em broadcast de qualquer mutação de produto. Microsserviços como Inteligência de Dados e Rastreabilidade (`audit-service` / `data-service`) operam como *Listeners* (Ouvintes) garantindo que nada bloqueie ou afunille as rotas síncronas REST.

3. **Singles Sign-On e Gerência (IAM Acoplada)**
   - O acesso global à plataforma segue em restrito pareamento com o contêiner mestre **Keycloak** através do `kizuna-iam-service`. 
   - Nenhum endpoint livre está autorizado. Tokens rigorosos OAuth2 baseados em escopos (`JWT Bearer Tokens`) circulam via anotações nas controllers dos subsistemas.

4. **Documentação OpenAPI3 (Swagger Integrado)**
   - Todas as portas, subdomínios, DTOs e requisitos das transações são fortemente tipados usando anotações Springdoc (`Swagger UI`).
   - Essa aderência cria um manual vivo acoplado diretamente ao código nativo, zerando discrepâncias informacionais no tráfego API para a camada de Interface Web e facilitando Testes manuais do Back.

5. **Tráfego WebSocket Reativo**
   - No `kizuna_notification_service`, utilizamos websockets para manter o Frontend imeditamente atualizado de eventos cruciais de máquina ou chão de fábrica sem técnicas rudimentares e custosas como Long Polling.

<br/>

## 📦 MICROSSERVIÇOS DA PLATAFORMA

| Identificação Node | Porta (host) | Propósito |
| :--- | :--- | :--- |
| **`frontend`** (nginx) | `80` | Interface web (build Docker). |
| **`gateway-service`** | `8080` | API Gateway e roteamento externo. |
| **`keycloak`** | `8081` | SSO / OAuth2 (admin console no mesmo host). |
| **`eureka-server`** | `8761` | Service discovery. |
| **`Kizuna-core-service`** | `8082` | Core: OP, receitas, estoque, qualidade. |
| **`kizuna-iam-service`** | `8083` | IAM + integração Keycloak. |
| **`kizuna-data-service`** | `8085` | Relatórios, OEE, agregações. |
| **`kizuna-notification-service`** | `8086` | Notificações WebSocket. |
| **`kizuna-audit-service`** | `8087` | Trilha de auditoria. |
| **`kizuna-ai-service`** | `8090` | TAKA / LLM (Groq). |

<br/>

## 🚀 AMBIENTE DE DESENVOLVIMENTO

Este repositório é **autocontido**: backend, frontend (`frontend/`), infra (`docker-compose.yml`) e scripts (`scripts/`). Não é necessário `KIZUNA_BUNDLE`.

### 1. Ferramental necessário

- **Java 17+** e **Maven**
- **Node.js 20+** (frontend)
- **Docker** e **Docker Compose**
- Arquivo `.env` na raiz (ex.: `COMPOSE_PROJECT_NAME=kizuna`, `GROQ_API_KEY=...`)

### 2. Stack completo (demo / produção local)

```bash
./scripts/kizuna-stack-up.sh
```

Todos os **13 containers** ficam no **mesmo projeto Docker Compose `kizuna`** (um grupo no Whaler / Docker Desktop). **Não use `docker run`** para subir frontend, IAM ou notification — isso cria cards separados fora do stack.

Comandos avulsos (sempre no projeto `kizuna`):

```bash
./scripts/kizuna-compose.sh ps
./scripts/kizuna-compose.sh logs -f kizuna-iam-service
./scripts/kizuna-compose.sh up -d --build kizuna-notification-service
```

- App: http://localhost  
- Keycloak: http://localhost:8081  

O frontend é buildado pelo Dockerfile multi-stage em `frontend/` (Vite + nginx).

Rebuild só do frontend:

```bash
./scripts/redeploy-frontend.sh
```

Se no Whaler aparecerem vários grupos “Kizuna” soltos, rode:

```bash
./scripts/kizuna-cleanup-orphans.sh
./scripts/kizuna-stack-up.sh
```

### 3. Dois ambientes Docker

| Projeto Compose | Arquivo | Script | Uso |
| :--- | :--- | :--- | :--- |
| **`kizuna`** | `docker-compose.yml` + `.env` | `./scripts/kizuna-stack-up.sh` | Stack **completo** (demo, teste integrado) |
| **`kizuna_dev`** | `docker-compose.dev.yml` + `.env.dev` | `./scripts/kizuna-dev-infra-up.sh` | Só **infra**; microsserviços no **IntelliJ** |

Containers e volumes são **isolados** (`kizuna-*` vs `kizuna-dev-*`, volumes `kizuna_*` vs `kizuna_dev_*`).

**Mesmas portas no host** (`5432`, `8081`, `8761`, …) — rode **apenas um stack por vez**. Antes de subir `kizuna_dev`, pare o stack completo:

```bash
docker-compose -p kizuna down
./scripts/kizuna-dev-infra-up.sh
```

Para voltar ao teste completo:

```bash
./scripts/kizuna-dev-infra-down.sh
./scripts/kizuna-stack-up.sh
```

**Infra `kizuna_dev` inclui:** Postgres, Mongo, RabbitMQ, Keycloak, Eureka.

### 4. Dev diário com IntelliJ (recomendado)

1. Subir infra dev: `./scripts/kizuna-dev-infra-up.sh`
2. No IntelliJ: Run no serviço que está codando com **Active profiles: `dev`**
3. Frontend (opcional): `cd frontend && npm run dev` → http://localhost:5173

**Keycloak (dev):** no client `kizuna-app`, inclua redirect `http://localhost:5173/*` além de `http://localhost/*`.

**Ordem sugerida no IDE** (quando precisar de vários serviços + Vite):

1. Eureka (já no Docker em `kizuna_dev`)
2. `kizuna-iam-service` e/ou `Kizuna-core-service` (conforme a feature)
3. `gateway-service` (profile `dev` — rotas em `application-dev.yaml` apontam para `localhost`)
4. Demais serviços sob demanda (data, audit, notification, ai)

| Trabalhando em… | Infra `kizuna_dev` | Runs no IntelliJ (profile `dev`) |
| :--- | :---: | :--- |
| **Core** | sim | Core; + IAM se usar usuários |
| **IAM** | sim | IAM |
| **Data / Audit / Notification** | sim | serviço alvo (+ Core se consumir eventos) |
| **Gateway + frontend Vite** | sim | Gateway + serviços que as telas chamam |
| **AI (TAKA)** | sim | AI; variável `GROQ_API_KEY` no Run Configuration |

### 5. Estrutura do repositório

```
KIZUNA/
├── eureka-server/
├── gateway-service/
├── Kizuna-core-service/
├── kizuna-iam-service/
├── kizuna-data-service/
├── kizuna-audit-service/
├── kizuna_notification_service/
├── kizuna-ai-service/
├── docker-compose.yml       # projeto kizuna (stack completo)
├── docker-compose.dev.yml   # projeto kizuna_dev (só infra)
├── .env.dev
├── scripts/
└── themes/                # Tema Keycloak
```

<br/>  
---
<div align="center">
  <small>© 2026 KIZUNA Project. Todos os direitos de arquitetura reservados.</small>
</div>
