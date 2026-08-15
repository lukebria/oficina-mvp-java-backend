# Roteiro do Piloto — Fluxo Completo da API

Roteiro funcional de chamadas de API para gravação do piloto: login, cadastro de cliente,
cadastro de veículo, criação de ordem de serviço (OS), aprovação pelo cliente e passagem por
todos os estados até a finalização/entrega.

Todos os exemplos usam `curl`. Ajuste `BASE_URL` para o ambiente onde a aplicação estiver no ar.

```bash
export BASE_URL="http://localhost:3000"
```

> Pré-requisito: a aplicação precisa ter subido pelo menos uma vez com o `DataSeeder` ativo
> (qualquer profile diferente de `test`) para existir o usuário admin (`admin@oficina.com` /
> `Admin@123`). O serviço de catálogo e a peça usados na OS são criados pelo próprio roteiro
> (etapas 4 e 5), então não dependem dos dados semeados existirem.

> **Documento e email usados no roteiro:** o CPF `529.982.247-25` (`52998224725`) é um CPF com
> dígitos verificadores válidos, conferido por `DocumentValidatorTest.shouldValidateCpf` — passa na
> validação de `DocumentValidator` usada pela API. O email do cliente é uma variável
> (`CUSTOMER_EMAIL`, default `lucas.bria@gmail.com`) apontando para uma caixa real, porque a API
> envia notificação por email a cada transição de status (exceto quando o novo status é
> `RECUSADA`) — isso permite confirmar o recebimento de cada email durante a gravação. Troque para
> a caixa de quem for validar. **Envio real de email também exige `MAIL_HOST`/`MAIL_USERNAME`/
> `MAIL_PASSWORD` configurados no ambiente** (ver README — Variáveis de ambiente); sem isso a API
> loga a falha de envio mas o fluxo de status continua normalmente.

---

## Executar tudo de uma vez (para a gravação)

Duas formas prontas de rodar o roteiro inteiro com um clique/comando:

- **Postman**: importe [`docs/pilot-collection.postman_collection.json`](pilot-collection.postman_collection.json)
  e use **Run collection** — executa as 10 requisições em ordem, encadeando token/ids
  automaticamente entre elas via scripts de teste, com asserts visuais (✔/✘) de status HTTP e de
  status da OS em cada etapa. Ajuste as variáveis da collection (aba *Variables*) antes de rodar —
  principalmente `baseUrl` e `customerEmail`.
- **Terminal**: o script [`docs/pilot-script.sh`](pilot-script.sh) roda a mesma sequência completa
  (login → cliente → veículo → serviço → peça → OS → aprovação pelo cliente → finalização →
  entrega → conferência) de ponta a ponta com um único comando. Imprime cada chamada, o corpo da
  resposta e o status HTTP, e para imediatamente com a resposta do erro se algo quebrar — ideal
  para deixar rodando durante a gravação e só narrar por cima.

```bash
bash docs/pilot-script.sh
```

Para apontar para outro ambiente (ex.: o ambiente do piloto em vez de localhost):

```bash
BASE_URL="https://<host-do-piloto>" bash docs/pilot-script.sh
```

Variáveis de ambiente aceitas, todas opcionais:

| Variável             | Default                  | Uso                                                                 |
|----------------------|---------------------------|-----------------------------------------------------------------------|
| `BASE_URL`            | `http://localhost:3000`   | Endereço da API                                                       |
| `SEED_ADMIN_EMAIL`    | `admin@oficina.com`       | Email do admin usado no login                                         |
| `SEED_ADMIN_PASSWORD` | `Admin@123`               | Senha do admin usado no login                                         |
| `CUSTOMER_EMAIL`      | `lucas.bria@gmail.com`    | Caixa real que recebe a notificação a cada transição de status da OS |

O script é tolerante a reexecuções: se cliente/veículo já existirem de uma tentativa anterior, ele
avisa e segue em frente (a criação da OS busca ou cria cliente/veículo automaticamente). O serviço
de catálogo e a peça recebem um sufixo com timestamp da execução (`name`/`sku` são colunas
`UNIQUE`), então também podem ser recriados a cada execução sem conflito. Recomendado rodar uma vez
em ensaio antes da gravação para confirmar que o ambiente está saudável.

As seções abaixo mostram cada chamada individualmente — mesmo conteúdo do script, para quem
preferir executar (ou narrar) passo a passo manualmente.

---

## 1. Login (obter token)

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@oficina.com",
  "password": "Admin@123"
}
```

```bash
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@oficina.com","password":"Admin@123"}')

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "$LOGIN_RESPONSE"
```

Resposta esperada (`AuthResponseDto`):

```json
{
  "token": "<jwt>",
  "user": { "id": 1, "name": "Administrador", "email": "admin@oficina.com", "role": "ADMIN" }
}
```

> Credenciais padrão do admin semeado vêm de `SEED_ADMIN_EMAIL` / `SEED_ADMIN_PASSWORD`
> (default `admin@oficina.com` / `Admin@123`). Se o ambiente do piloto tiver sobrescrito essas
> variáveis, ajuste aqui antes de gravar.

Todas as chamadas autenticadas abaixo usam:

```
Authorization: Bearer $TOKEN
```

---

## 2. Cadastro do cliente

```http
POST /api/customers
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "name": "Maria Cliente",
  "document": "52998224725",
  "email": "lucas.bria@gmail.com",
  "phone": "11999999999"
}
```

```bash
curl -s -X POST "$BASE_URL/api/customers" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria Cliente",
    "document": "52998224725",
    "email": "lucas.bria@gmail.com",
    "phone": "11999999999"
  }'
```

Retorna `CustomerResponseDto` com o `id` do cliente. `52998224725` (`529.982.247-25`) é um CPF com
dígitos verificadores válidos; o email deve ser uma caixa real, pois é para ela que a API envia as
notificações de mudança de status da OS.

---

## 3. Cadastro do veículo

```http
POST /api/vehicles
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "customerId": 1,
  "plate": "ABC1D23",
  "brand": "Toyota",
  "model": "Corolla",
  "manufacturingYear": 2022
}
```

```bash
curl -s -X POST "$BASE_URL/api/vehicles" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "plate": "ABC1D23",
    "brand": "Toyota",
    "model": "Corolla",
    "manufacturingYear": 2022
  }'
```

> Use o `id` retornado na etapa 2 em `customerId`. Esta etapa é opcional para o fluxo da OS
> (o `POST /api/service-orders` na etapa 6 já cria/atualiza cliente e veículo por
> documento/placa), mas é útil no piloto para demonstrar o cadastro isolado.

---

## 4. Cadastro do serviço de catálogo usado na OS

Criado pelo próprio roteiro (não depende dos dados semeados do `DataSeeder`). `name` é `UNIQUE`
no banco, então em execuções repetidas use um sufixo (ex.: timestamp) para não colidir — no
script isso é feito automaticamente via `RUN_SUFFIX`.

```http
POST /api/services
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "name": "Troca de óleo (piloto 1755273600)",
  "description": "Troca de óleo do motor",
  "basePrice": 180.00,
  "estimatedMinutes": 60,
  "active": true
}
```

```bash
RUN_SUFFIX=$(date +%s)

SERVICE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/services" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Troca de óleo (piloto $RUN_SUFFIX)\",
    \"description\": \"Troca de óleo do motor\",
    \"basePrice\": 180.00,
    \"estimatedMinutes\": 60,
    \"active\": true
  }")

SERVICE_ITEM_ID=$(echo "$SERVICE_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "$SERVICE_RESPONSE"
echo "Serviço de catálogo id=$SERVICE_ITEM_ID"
```

> Requer role `ADMIN`. Guarde `SERVICE_ITEM_ID` para usar na criação da OS (etapa 6).

---

## 5. Cadastro da peça usada na OS

Criada pelo próprio roteiro. `sku` é `UNIQUE` no banco — mesmo cuidado de usar um sufixo por
execução.

```http
POST /api/parts
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "name": "Filtro de óleo (piloto 1755273600)",
  "sku": "FILTRO-OLEO-1755273600",
  "unitPrice": 45.90,
  "stockQuantity": 25,
  "minStock": 5,
  "active": true
}
```

```bash
PART_RESPONSE=$(curl -s -X POST "$BASE_URL/api/parts" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Filtro de óleo (piloto $RUN_SUFFIX)\",
    \"sku\": \"FILTRO-OLEO-$RUN_SUFFIX\",
    \"unitPrice\": 45.90,
    \"stockQuantity\": 25,
    \"minStock\": 5,
    \"active\": true
  }")

PART_ID=$(echo "$PART_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "$PART_RESPONSE"
echo "Peça id=$PART_ID"
```

> Requer role `ADMIN`. Guarde `PART_ID` para usar na criação da OS (etapa 6).

---

## 6. Criação da ordem de serviço

Ao criar a OS o sistema, em uma única chamada: normaliza/valida CPF/CNPJ, normaliza/valida placa,
busca ou cria o cliente, busca ou cria o veículo, valida serviços/peças ativos, calcula o
orçamento, grava o histórico e já coloca a OS no status `AGUARDANDO_APROVACAO`.

```http
POST /api/service-orders
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "customerDocument": "52998224725",
  "customer": {
    "name": "Maria Cliente",
    "email": "lucas.bria@gmail.com",
    "phone": "11999999999"
  },
  "vehicle": {
    "plate": "ABC1D23",
    "brand": "Toyota",
    "model": "Corolla",
    "manufacturingYear": 2022
  },
  "customerNotes": "Barulho ao frear",
  "services": [
    { "serviceItemId": 1, "quantity": 1 }
  ],
  "parts": [
    { "partId": 1, "quantity": 1 }
  ]
}
```

Use os `SERVICE_ITEM_ID` e `PART_ID` capturados nas etapas 4 e 5 (não fixe `1` — esse id só existe
se o `DataSeeder` já rodou nesse ambiente):

```bash
OS_RESPONSE=$(curl -s -X POST "$BASE_URL/api/service-orders" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"customerDocument\": \"52998224725\",
    \"customer\": {
      \"name\": \"Maria Cliente\",
      \"email\": \"lucas.bria@gmail.com\",
      \"phone\": \"11999999999\"
    },
    \"vehicle\": {
      \"plate\": \"ABC1D23\",
      \"brand\": \"Toyota\",
      \"model\": \"Corolla\",
      \"manufacturingYear\": 2022
    },
    \"customerNotes\": \"Barulho ao frear\",
    \"services\": [
      { \"serviceItemId\": $SERVICE_ITEM_ID, \"quantity\": 1 }
    ],
    \"parts\": [
      { \"partId\": $PART_ID, \"quantity\": 1 }
    ]
  }")

OS_ID=$(echo "$OS_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
OS_CODE=$(echo "$OS_RESPONSE" | grep -o '"code":"[^"]*"' | cut -d'"' -f4)
echo "$OS_RESPONSE"
echo "OS criada: id=$OS_ID code=$OS_CODE"
```

Status resultante: **`AGUARDANDO_APROVACAO`**. Guarde `OS_ID` (uso administrativo) e `OS_CODE`
(uso público, formato `OS-AAAAMMDD-NNNNN`).

---

## 7. Aprovação da ordem de serviço pelo cliente

Esta é a etapa de **aprovação pelo cliente**: chamada pública, sem token, feita pelo próprio
cliente informando o documento cadastrado. Transiciona `AGUARDANDO_APROVACAO -> EM_EXECUCAO`,
valida e decrementa o estoque das peças, e preenche `approvedAt`/`startedAt`.

Opcionalmente, antes de aprovar, o cliente pode consultar a OS publicamente:

```http
GET /api/public/service-orders/{code}?document=52998224725
```

```bash
curl -s "$BASE_URL/api/public/service-orders/$OS_CODE?document=52998224725"
```

Aprovação:

```http
POST /api/public/service-orders/{code}/approval
Content-Type: application/json
```

```json
{
  "document": "52998224725",
  "approved": true,
  "comment": "Aprovado pelo cliente."
}
```

```bash
curl -s -X POST "$BASE_URL/api/public/service-orders/$OS_CODE/approval" \
  -H "Content-Type: application/json" \
  -d '{
    "document": "52998224725",
    "approved": true,
    "comment": "Aprovado pelo cliente."
  }'
```

Status resultante: **`EM_EXECUCAO`**.

> Alternativa administrativa (equipe registra a decisão em nome do cliente, roles `ADMIN`/`MECHANIC`
> apenas — `ATTENDANT` recebe `403`): `PATCH /api/service-orders/{id}/approval` com o mesmo corpo
> `{"approved": true, "comment": "..."}`, autenticado com `Authorization: Bearer <token>`.

> `approved: false` transicionaria a OS para `RECUSADA` (estado terminal) em vez de
> `EM_EXECUCAO` — não usar no fluxo feliz do piloto.

---

## 8. Finalização do serviço

```http
PATCH /api/service-orders/{id}/status
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "status": "FINALIZADA",
  "comment": "Serviço finalizado."
}
```

```bash
curl -s -X PATCH "$BASE_URL/api/service-orders/$OS_ID/status" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "FINALIZADA",
    "comment": "Serviço finalizado."
  }'
```

Status resultante: **`FINALIZADA`** (preenche `finalizedAt`). Requer role `ADMIN` ou `MECHANIC`.

---

## 9. Entrega do veículo ao cliente

```http
PATCH /api/service-orders/{id}/status
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "status": "ENTREGUE",
  "comment": "Veículo entregue ao cliente."
}
```

```bash
curl -s -X PATCH "$BASE_URL/api/service-orders/$OS_ID/status" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "ENTREGUE",
    "comment": "Veículo entregue ao cliente."
  }'
```

Status resultante: **`ENTREGUE`** (estado terminal, preenche `deliveredAt`). Fim do fluxo.

---

## 10. Conferência final

```http
GET /api/service-orders/{id}
Authorization: Bearer <token>
```

```bash
curl -s "$BASE_URL/api/service-orders/$OS_ID" \
  -H "Authorization: Bearer $TOKEN"
```

Confirma no JSON de resposta: `status: "ENTREGUE"`, orçamento (`services`/`parts`/totais),
histórico completo (`RECEBIDA`/criação → `AGUARDANDO_APROVACAO` → `EM_EXECUCAO` → `FINALIZADA` →
`ENTREGUE`) e os timestamps `approvedAt`, `startedAt`, `finalizedAt`, `deliveredAt` preenchidos.

---

## Referência — máquina de estados da OS

```txt
RECEBIDA              -> EM_DIAGNOSTICO, AGUARDANDO_APROVACAO
EM_DIAGNOSTICO         -> AGUARDANDO_APROVACAO
AGUARDANDO_APROVACAO   -> EM_EXECUCAO, RECUSADA
EM_EXECUCAO            -> FINALIZADA
FINALIZADA             -> ENTREGUE
ENTREGUE               -> (terminal)
RECUSADA               -> (terminal)
```

A criação da OS já dispara automaticamente para `AGUARDANDO_APROVACAO` (o orçamento é calculado na
criação), por isso o roteiro do piloto não passa por `RECEBIDA`/`EM_DIAGNOSTICO` manualmente. Uma
transição inválida (fora dessa tabela) retorna `422 UNPROCESSABLE_CONTENT`.

## Referência — roles por endpoint

| Endpoint                                              | Roles permitidas          |
|--------------------------------------------------------|----------------------------|
| `POST /api/auth/login`                                  | público                    |
| `POST /api/customers`, `/api/vehicles`                  | ADMIN, ATTENDANT, MECHANIC |
| `POST /api/services`, `/api/parts`                       | ADMIN                       |
| `POST /api/service-orders`                               | ADMIN, ATTENDANT, MECHANIC |
| `PATCH /api/service-orders/{id}/approval`                | ADMIN, MECHANIC             |
| `PATCH /api/service-orders/{id}/status`                  | ADMIN, MECHANIC             |
| `PATCH /api/service-orders/{id}/diagnosis`               | ADMIN, MECHANIC             |
| `GET /api/public/service-orders/{code}`                  | público                     |
| `POST /api/public/service-orders/{code}/approval`        | público                     |

Mais detalhes e payloads adicionais em [`README.md` — Payloads úteis](../README.md#payloads-úteis)
e [Fluxo de status da OS](../README.md#fluxo-de-status-da-os).
