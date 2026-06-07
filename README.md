# BPC Remo — Protótipo Gestão Escola de Canoa Havaiana

Protótipo funcional para validação de fluxos operacionais: aulas coletivas, reservas de embarcações, planos de alunos, disponibilidade (incluindo trimarã/OC6), ocorrências e manutenção.

## Stack

| Camada | Tecnologias |
|--------|-------------|
| Backend | Java 21, Spring Boot 3.3, Spring Data JPA, PostgreSQL, Lombok |
| Frontend | Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS 4 |
| Infra | Docker Compose (PostgreSQL + API) |

## Pré-requisitos

- Docker Desktop
- Node.js 20+
- (Opcional) Java 21 + Maven para rodar o backend localmente

## Início rápido

### 1. Subir backend e banco

```bash
docker compose up --build
```

Aguarde até a API responder em **http://localhost:8080/api/dashboard**

O seed roda automaticamente na primeira execução (`app.seed.enabled=true`).

### 2. Subir frontend

```bash
cd frontend
npm install
npm run dev
```

Acesse **http://localhost:3000**

### 3. Autenticação mock

Use o seletor **“Usuário mock”** no header para alternar entre ADMIN, PROFESSOR e ALUNO. O ID é enviado no header `X-Usuario-Id` (ex.: ocorrências).

## Usuários seed

| Perfil | Email | Nome |
|--------|-------|------|
| Admin | admin@bpc.com | Admin Recepção |
| Professor | ricardo@bpc.com | Prof. Ricardo |
| Professor | marina@bpc.com | Prof. Marina |
| Alunos | aluno1@bpc.com … aluno15@bpc.com | 15 alunos fictícios |

## Planos seed

- **1x Semana** — máximo 1 reserva de aula por semana ISO
- **2x Semana** — máximo 2 reservas de aula por semana
- **Ilimitado** — sem limite de aulas
- **Pacote 10 Remadas** — consome remadas em reservas de embarcação

## Embarcações seed

- OC1, OC2, OC3, OC4, várias **OC6**
- 2 **Katamarãs**, 2 **Trimarãs** (cada trimarã composta por 3 OC6)
- OC6 #2 e #6 **interditadas**; OC6 #4 em **manutenção**
- Trimarã Nalu usa OC6 interditadas/manutenção → indisponível por regra de composição

## Horários de aulas

Todos os slots fixos (SEG–DOM) da especificação foram pré-cadastrados via `HorariosFixos.java` (incluindo horários com meia hora, ex.: 12h30–13h30).

## Telas

| Rota | Função |
|------|--------|
| `/` | Dashboard operacional |
| `/agenda` | Calendário semanal |
| `/alunos` | CRUD alunos + planos |
| `/embarcacoes` | Status, interdição, ocorrências, manutenções |
| `/aulas` | Listagem + presença |
| `/reservas` | Reservar/cancelar aula e embarcação |
| `/ocorrencias` | Abrir e alterar status |

## API REST (prefixo `/api`)

- `GET /dashboard`
- `GET|POST|PUT /usuarios`
- `GET /planos`, `GET|POST /alunos/{id}/planos`
- `GET|POST|PUT /embarcacoes`, `POST /embarcacoes/{id}/interditar`
- `GET /aulas`, `GET /aulas/agenda?de=&ate=`, `GET /aulas/{id}/inscritos?data=`
- `POST|DELETE /reservas-aula`, `PATCH /reservas-aula/{id}/presenca`
- `POST|DELETE /reservas-embarcacao`
- `GET|POST /ocorrencias`, `PATCH /ocorrencias/{id}/status`
- `GET|POST /manutencoes`

Paginação: `?page=0&size=20` · Busca: `?q=texto`

## Regras de negócio implementadas

1. Embarcação **interditada** → não reserva
2. **Manutenção** → não disponível
3. OC6 em uso por **trimarã** → não reserva individual da OC6
4. OC6 **interditada** → trimarã indisponível
5. Cancelamento só até **1h antes**
6–8. Limites de plano **1x / 2x / ilimitado** por semana ISO
9. Pacote **avulso** consome remadas automaticamente
10. Sem **dupla reserva** no mesmo horário
11. **Capacidade máxima** da aula

## Reset do banco

```bash
docker compose down -v
docker compose up --build
```

## Desenvolvimento local (sem Docker para API)

1. Subir só o Postgres: `docker compose up postgres -d`
2. Backend: `cd backend && mvn spring-boot:run`
3. Frontend: `cd frontend && npm run dev`

## Estrutura

```
BPC_app/
├── docker-compose.yml
├── backend/          # Spring Boot
├── frontend/         # Next.js
└── README.md
```

## Troubleshooting

### Docker não inicia (`dockerDesktopLinuxEngine` não encontrado)

Abra o **Docker Desktop** e aguarde ficar "Running" antes de `docker compose up --build`.

### `npm install` trava infinitamente ou erro EBADF

**Sempre rode na pasta `frontend/`, nunca na raiz:**

```powershell
cd frontend
npm ci --no-audit --no-fund
```

Requisitos: **Node >= 22.13.0** (`nvm use 22.13.0`), **npm 10.x** (evite npm 11 no Windows).

Se travar ou falhar em `Documents`:

1. Copie o projeto para `C:\dev\BPC_app` (OneDrive bloqueia escrita em `node_modules`).
2. Ou use **pnpm**:

```powershell
corepack enable
corepack prepare pnpm@9.15.0 --activate
cd frontend
pnpm install
pnpm dev
```

Da raiz do monorepo também funciona:

```powershell
npm run install:frontend
npm run dev:frontend
```

### `npm install` com erro EBADF ou `npm run build` falha em `.next`

1. Feche processos Next.js (`next dev`) e o Cursor se estiver bloqueando arquivos.
2. No PowerShell **como administrador**, na pasta `frontend`:

```powershell
Remove-Item -Recurse -Force node_modules, .next -ErrorAction SilentlyContinue
Remove-Item -Force package-lock.json -ErrorAction SilentlyContinue
npm cache clean --force
npm install
npm run build
```

3. Se persistir, rode os comandos em um terminal **externo** (Windows Terminal), não no agente integrado.

### Maven não cria `target/classes`

Verifique permissão de escrita em `Documents\BPC_app`. Pastas sincronizadas (OneDrive) podem bloquear builds — mova o projeto para um caminho local simples, ex.: `C:\dev\BPC_app`.

## Fora de escopo (protótipo)

JWT/OAuth, pagamentos, WebSocket, testes automatizados, microserviços.
