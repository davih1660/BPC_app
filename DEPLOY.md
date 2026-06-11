# Deploy do protótipo BPC Remo (Vercel + Railway)

Guia para publicar o app online e compartilhar com professores via celular (fora da rede local).

## Arquitetura

```
Celular → https://seu-app.vercel.app (Next.js)
              ↓ proxy /api/*
         https://sua-api.up.railway.app (Spring Boot + PostgreSQL)
```

O navegador só acessa a Vercel. O Next.js repassa chamadas `/api/*` para a API na Railway, evitando problemas de CORS.

---

## Pré-requisitos

- Conta no [GitHub](https://github.com) com o código do projeto
- Conta na [Railway](https://railway.app) (hobby/trial)
- Conta na [Vercel](https://vercel.com) (hobby gratuito)

---

## Passo 1 — Subir o código no GitHub

```bash
cd BPC_app
git add .
git commit -m "Preparar deploy Vercel + Railway"
git push origin main
```

---

## Passo 2 — Deploy na Railway (API + Postgres)

### 2.1 Criar projeto

1. Acesse [railway.app](https://railway.app) → **New Project**
2. **Add PostgreSQL** (plugin gerenciado)
3. **Add Service** → **GitHub Repo** → selecione o repositório

### 2.2 Configurar o serviço backend

No serviço conectado ao repo:


| Configuração      | Valor         |
| ----------------- | ------------- |
| Root Directory    | `backend`     |
| Builder           | Dockerfile    |
| Health Check Path | `/api/health` |


### 2.3 Conectar Postgres ao backend (obrigatório)

1. No canvas, clique no serviço **BPC_app**
2. Aba **Variables** → **+ New Variable** → **Add Reference**
3. Selecione o serviço **Postgres** e adicione **uma** destas opções:

**Use só `DATABASE_URL`** (rede privada — **não** use `DATABASE_PUBLIC_URL`).

**Remova** variáveis `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` se existirem junto com `DATABASE_URL` — senhas desatualizadas causam `password authentication failed for user "postgres"`.

> Sem `DATABASE_URL` a API sobe com `jdbc:postgresql://:/` e o healthcheck falha.

### 2.4 Variáveis de ambiente


| Variável                 | Valor                                           |
| ------------------------ | ----------------------------------------------- |
| `SPRING_PROFILES_ACTIVE` | `docker`                                        |
| `APP_SEED_ENABLED`       | `true`                                          |
| `APP_UPLOADS_DIR`        | `/app/uploads`                                  |
| `APP_CORS_ORIGINS`       | `http://localhost:3000` *(ajustar após Vercel)* |
| `TZ`                     | `America/Sao_Paulo`                             |
| `POSTGRES_SSLMODE`       | `require`                                       |


**Remova** se existirem: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

### 2.5 Volume para uploads (opcional)

Sem volume, fotos de ocorrências somem a cada redeploy.

1. Serviço backend → **Volumes** → **Add Volume**
2. Mount path: `/app/uploads`

### 2.6 Domínio público

1. Serviço backend → **Settings** → **Networking** → **Generate Domain**
2. Anote a URL (ex.: `bpc-api-production.up.railway.app`)

### 2.7 Validar

Abra no navegador:

```
https://SUA-API.up.railway.app/api/health
```

Resposta esperada: `{"status":"ok"}`

Aguarde o primeiro deploy (build Maven + seed pode levar 3–5 min).

---

## Passo 3 — Deploy na Vercel (frontend)

### 3.1 Importar projeto

1. Acesse [vercel.com](https://vercel.com) → **Add New** → **Project**
2. Importe o repositório GitHub
3. **Root Directory:** `frontend`
4. Framework: Next.js (detectado automaticamente)

### 3.2 Variáveis de ambiente


| Variável                     | Valor                            |
| ---------------------------- | -------------------------------- |
| `API_BACKEND_URL`            | `https://SUA-API.up.railway.app` |
| `NEXT_PUBLIC_API_URL`        | `/api`                           |
| `NEXT_PUBLIC_SHOW_DEMO_HINT` | `true`                           |


> `API_BACKEND_URL` **não** deve terminar com `/api`.

### 3.3 Deploy

Clique em **Deploy**. Anote a URL gerada (ex.: `https://bpc-remo.vercel.app`).

### 3.4 Atualizar CORS na Railway (obrigatório para login)

Sem este passo, o login retorna **403 Forbidden** na Vercel.

Volte ao serviço **BPC_app** na Railway → **Variables** e defina:

```
APP_CORS_ORIGINS=https://bpc-app-steel.vercel.app
```

(use a **URL exata** da Vercel, com `https://`, sem barra no final).

> O código também aceita `https://*.vercel.app` automaticamente após o próximo deploy do backend. Mesmo assim, mantenha `APP_CORS_ORIGINS` com a URL de produção.

Salve — o Railway fará redeploy automaticamente. Teste o login em seguida.

---

## Passo 4 — Testar

### Checklist

- `https://seu-app.vercel.app/login` abre no celular (4G, fora do Wi-Fi)
- Login com `marina@bpc.com` / `123456`
- Dashboard e agenda carregam
- Ocorrências: upload e visualização de foto funcionam
- Seletor "Usuário mock" **não** aparece (só em dev local)

### Mensagem para professores

```
App protótipo BPC Remo:
https://seu-app.vercel.app

Login professor:
E-mail: marina@bpc.com (ou ricardo@bpc.com)
Senha: 123456

Dados fictícios — pode testar à vontade.
```

---

## Desenvolvimento local (inalterado)

```bash
# Terminal 1 — API + Postgres
docker compose up --build

# Terminal 2 — Frontend
cd frontend
npm install
npm run dev
```

Acesse **[http://localhost:3000](http://localhost:3000)**. Sem `API_BACKEND_URL`, o frontend usa `http://localhost:8080/api` diretamente.

---

## Usuários seed


| Perfil    | E-mail                                                                              | Senha  |
| --------- | ----------------------------------------------------------------------------------- | ------ |
| Admin     | [admin@bpc.com](mailto:admin@bpc.com)                                               | 123456 |
| Professor | [marina@bpc.com](mailto:marina@bpc.com)                                             | 123456 |
| Professor | [ricardo@bpc.com](mailto:ricardo@bpc.com)                                           | 123456 |
| Aluno     | [aluno1@bpc.com](mailto:aluno1@bpc.com) … [aluno15@bpc.com](mailto:aluno15@bpc.com) | 123456 |


O seed só roda na primeira vez (banco vazio). Para resetar:

```bash
# Na Railway: delete o volume Postgres e redeploy, ou:
docker compose down -v && docker compose up --build
```

---

## Limitações do protótipo


| Item     | Comportamento                                       |
| -------- | --------------------------------------------------- |
| Uploads  | Sem volume Railway, fotos somem no redeploy         |
| Auth     | Token simples — não é segurança de produção         |
| Custo    | Monitore créditos Railway e limites Vercel Hobby    |
| Redeploy | Mudar `API_BACKEND_URL` na Vercel exige novo deploy |


---

## Troubleshooting

### Frontend: "Não foi possível conectar à API"

- Confirme `API_BACKEND_URL` na Vercel (URL Railway correta, sem `/api`)
- Confirme `NEXT_PUBLIC_API_URL=/api`
- Teste `https://SUA-API.up.railway.app/api/health` diretamente

### Backend: erro de conexão com Postgres / healthcheck falha

- Log `jdbc:postgresql://:/` → adicione referência a `DATABASE_URL` (privada)
- Log `password authentication failed for user "postgres"` → remova `PGHOST`/`PGPASSWORD` avulsos; use **só** `DATABASE_URL` do Postgres atual
- **Remova** `SPRING_DATASOURCE_*` se existirem
- Não use `DATABASE_PUBLIC_URL`
- Veja **Deploy Logs** — procure `Connection refused`, `SSL` ou `JDBC URL invalid`
- O primeiro deploy com seed pode levar 2–3 min até o healthcheck passar

### Login retorna 500 ou timeout de 30s

- `/api/health` ok mas login trava → senha do Postgres errada; reconecte `DATABASE_URL`

### Login retorna 403 / "Erro na requisição"

- Causa mais comum: `APP_CORS_ORIGINS` na Railway ainda é `http://localhost:3000`
- Corrija para `https://SUA-URL.vercel.app` e aguarde redeploy
- O perfil `docker` também aceita `https://*.vercel.app` e `*` (protótipo)
- Confirme nos HTTP logs da Railway: `POST /api/auth/login` deve ser **200**, não **403**

### Login retorna "E-mail ou senha inválidos" (401)

- A API respondeu, mas o usuário não existe ou a senha está errada
- Confirme `APP_SEED_ENABLED=true` na Railway
- Use `marina@bpc.com` / `123456` (senha demo)
- Após redeploy, o `DemoAcessoSeeder` recria usuários demo se faltarem
- Se persistir: apague o Postgres e crie um novo (banco limpo)

### CORS (se testar API direto no browser)

- `APP_CORS_ORIGINS` deve incluir a URL exata da Vercel (com `https://`, sem barra final)
- O backend também aceita o padrão `https://*.vercel.app`

### Build Railway falha

- Root Directory deve ser `backend` (não a raiz do monorepo)
- Verifique logs: build Maven precisa de ~3 min
- Erro `SSL peer shut down` ao baixar do Maven Central: o `Dockerfile` usa imagem Debian (não Alpine) e camada de dependências separada — faça push e redeploy
- Se falhar de novo, clique **Redeploy** (pode ser instabilidade temporária de rede)

### Fotos somem após redeploy

- Adicione volume em `/app/uploads` no serviço Railway

