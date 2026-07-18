# Oficina MVP Backend — Java 25 + Spring Boot 4

Back-end monolítico para um MVP de **oficina mecânica**, desenvolvido com **Java 25**, **Spring Boot 4**, **Maven**, *
*PostgreSQL**, **JPA/Hibernate**, **Flyway**, **Spring Security**, **JWT** e **Swagger/OpenAPI**.

O projeto permite gerenciar clientes, veículos, catálogo de serviços, peças/insumos, ordens de serviço, orçamento
automático, aprovação pelo cliente, histórico de status e relatório de tempo médio de execução.

## Sumário

- [Stack](#stack)
- [Funcionalidades](#funcionalidades)
- [Arquitetura do projeto](#arquitetura-do-projeto)
- [Como rodar localmente](#como-rodar-localmente)
- [Como rodar com Docker](#como-rodar-com-docker)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Usuário admin inicial](#usuário-admin-inicial)
- [Autenticação](#autenticação)
- [Endpoints principais](#endpoints-principais)
- [Payloads úteis](#payloads-úteis)
- [Banco de dados](#banco-de-dados)
- [Testes e cobertura](#testes-e-cobertura)
- [Documentação complementar](#documentação-complementar)
- [Pontos de atenção](#pontos-de-atenção)

## Stack

| Item                | Tecnologia                     |
|---------------------|--------------------------------|
| Linguagem           | Java 25                        |
| Framework           | Spring Boot 4.0.6              |
| Build               | Maven                          |
| API                 | Spring WebMVC                  |
| Persistência        | Spring Data JPA / Hibernate    |
| Banco               | PostgreSQL                     |
| Migrações           | Flyway                         |
| Segurança           | Spring Security + JWT Bearer   |
| JWT                 | JJWT 0.12.6                    |
| Validação           | Jakarta Bean Validation        |
| Documentação da API | SpringDoc OpenAPI / Swagger UI |
| Observabilidade     | Spring Boot Actuator           |
| Testes              | JUnit 5, Mockito, MockMvc, H2  |
| Cobertura           | JaCoCo                         |
| Container           | Docker + Docker Compose        |

## Funcionalidades

- Login administrativo com JWT.
- CRUD de clientes.
- CRUD de veículos vinculados a clientes.
- CRUD de serviços do catálogo da oficina.
- CRUD de peças/insumos com estoque e estoque mínimo.
- Criação de ordem de serviço com cliente, veículo, serviços e peças.
- Geração automática de orçamento da OS.
- Cálculo automático de total de serviços, total de peças e total geral.
- Consulta pública de OS por código e CPF/CNPJ.
- Aprovação pública do orçamento pelo cliente.
- Aprovação administrativa do orçamento.
- Validação de estoque antes da aprovação.
- Baixa automática de estoque na aprovação.
- Controle de transição de status da OS.
- Histórico de status da OS.
- Relatório de tempo médio de execução.
- Healthcheck da aplicação.
- Swagger/OpenAPI.
- Seed inicial de usuário admin, serviços e peças.

## Arquitetura do projeto

O projeto é um **monolito modular organizado por módulos de negócio**, seguindo os princípios de **Arquitetura
Hexagonal (Ports & Adapters)**. Cada módulo (`customer`, `vehicle`, `catalog`, `part`, `auth`, `serviceorder`,
`report`) é um pacote físico próprio com o mesmo esqueleto interno:

```txt
src/main/java/br/com/oficina/mvp/
  OficinaMvpApplication.java
  <modulo>/
    domain/                     entidade JPA e regras de domínio do módulo
    application/                caso de uso (implementa as portas de entrada)
    application/port/in/        portas de entrada — interface de caso de uso + Command/Result
    application/port/out/       portas de saída — o que o módulo precisa de fora (ex: repositório)
    adapter/in/web/             controller REST + DTOs de request/response
    adapter/out/persistence/    repositório Spring Data (package-private) + adapter da porta
  shared/
    domain/     vocabulário compartilhado entre módulos (BaseEntity, enums Role e ServiceOrderStatus)
    config/     segurança, CORS, OpenAPI, seed e properties
    exception/  exceções e handler global
    security/   filtro e serviço JWT
    validation/ validadores de CPF/CNPJ e placa
    api/        endpoints técnicos que não pertencem a um módulo de negócio (healthcheck)
```

Quando um módulo precisa de outro (por exemplo `serviceorder` buscando `Customer`, `Vehicle`, `ServiceCatalogItem` e
`Part`), ele depende sempre da **porta** do módulo alheio (`CustomerRepositoryPort`, `VehicleUseCase` etc.), nunca da
implementação concreta ou do repositório JPA do outro módulo. Detalhamento completo, camada por camada e módulo por
módulo, está em [`docs/architecture.md`](docs/architecture.md).

As principais regras de negócio ficam em:

- `ServiceOrderService` (módulo `serviceorder`): criação, aprovação, validação de estoque, baixa de estoque, consulta
  pública e status de OS;
- `ServiceOrder`: cálculo de totais, histórico e timestamps de status;
- `ServiceOrderStatusPolicy`: transições permitidas de status;
- `DocumentValidator`: validação de CPF/CNPJ;
- `PlateValidator`: validação de placa antiga e Mercosul;
- `Part`: normalização de SKU e baixa de estoque.

## Como rodar localmente

### 1. Pré-requisitos

- Java 25.
- Maven.
- Docker e Docker Compose, caso queira subir o PostgreSQL localmente via container.

### 2. Criar o arquivo `.env`

Linux/macOS:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

### 3. Subir o PostgreSQL

```bash
docker compose up -d postgres
```

### 4. Rodar a aplicação

```bash
mvn spring-boot:run
```

A API sobe em:

```txt
http://localhost:3000
```

URLs úteis:

```txt
Swagger UI: http://localhost:3000/swagger-ui.html
OpenAPI:    http://localhost:3000/v3/api-docs
Health:     http://localhost:3000/api/health
Actuator:   http://localhost:3000/actuator/health
```

## Como rodar com Docker

Para subir banco e API juntos:

```bash
docker compose up --build
```

Para parar:

```bash
docker compose down
```

Para remover também o volume do banco:

```bash
docker compose down -v
```

O `Dockerfile` usa build multi-stage:

1. imagem Maven com Eclipse Temurin 25 para empacotar o projeto;
2. imagem JRE Eclipse Temurin 25 para executar o `app.jar`.

## Variáveis de ambiente

As variáveis estão documentadas no `.env.example` e são lidas pelo `application.yml`.

| Variável                 | Padrão                                                         | Descrição                   |
|--------------------------|----------------------------------------------------------------|-----------------------------|
| `APP_PORT`               | `3000`                                                         | Porta HTTP da API           |
| `DB_HOST`                | `localhost`                                                    | Host do PostgreSQL          |
| `DB_PORT`                | `5432`                                                         | Porta do PostgreSQL         |
| `DB_NAME`                | `oficina_mvp`                                                  | Nome do banco               |
| `DB_USER`                | `oficina`                                                      | Usuário do banco            |
| `DB_PASSWORD`            | `oficina`                                                      | Senha do banco              |
| `JWT_SECRET`             | `troque-este-segredo-em-producao-com-pelo-menos-32-caracteres` | Segredo de assinatura JWT   |
| `JWT_EXPIRES_IN_MINUTES` | `480` no `application.yml`; `30` no `.env.example`             | Expiração do JWT em minutos |
| `CORS_ALLOWED_ORIGINS`   | `http://localhost:5173,http://localhost:3000`                  | Origens permitidas no CORS  |
| `SEED_ADMIN_EMAIL`       | `admin@oficina.com`                                            | Email do admin inicial      |
| `SEED_ADMIN_PASSWORD`    | `Admin@123`                                                    | Senha do admin inicial      |

## Usuário admin inicial

Ao iniciar fora do profile `test`, o sistema cria automaticamente um usuário admin se ele ainda não existir:

```txt
email: admin@oficina.com
senha: Admin@123
```

Esses valores podem ser alterados no `.env` usando:

```txt
SEED_ADMIN_EMAIL=
SEED_ADMIN_PASSWORD=
```

Além do admin, o seed inicial também cria alguns serviços e peças para facilitar testes locais.

## Autenticação

### Login

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

Resposta esperada:

```json
{
  "token": "<jwt>",
  "user": {
    "id": 1,
    "name": "Administrador",
    "email": "admin@oficina.com",
    "role": "ADMIN"
  }
}
```

Use o token nas rotas protegidas:

```http
Authorization: Bearer <token>
```

### Rotas públicas

- `POST /api/auth/login`
- `/api/public/**`
- `/api/health`
- `/actuator/health`
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`

As demais rotas exigem JWT.

## Endpoints principais

### Auth

```http
POST /api/auth/login
```

### Clientes

```http
GET    /api/customers
POST   /api/customers
GET    /api/customers/{id}
PUT    /api/customers/{id}
DELETE /api/customers/{id}
```

### Veículos

```http
GET    /api/vehicles
POST   /api/vehicles
GET    /api/vehicles/{id}
PUT    /api/vehicles/{id}
DELETE /api/vehicles/{id}
```

### Catálogo de serviços

```http
GET    /api/services
POST   /api/services
GET    /api/services/{id}
PUT    /api/services/{id}
DELETE /api/services/{id}
```

### Peças/Insumos

```http
GET    /api/parts
POST   /api/parts
GET    /api/parts/{id}
PUT    /api/parts/{id}
DELETE /api/parts/{id}
```

### Ordens de serviço — fluxo administrativo

```http
GET   /api/service-orders
POST  /api/service-orders
GET   /api/service-orders/{id}
PATCH /api/service-orders/{id}/approve
PATCH /api/service-orders/{id}/status
```

### Ordens de serviço — fluxo público do cliente

```http
GET  /api/public/service-orders/{code}?document=12345678909
POST /api/public/service-orders/{code}/approve
```

### Relatórios

```http
GET /api/reports/average-execution-time
```

### Health

```http
GET /api/health
GET /actuator/health
```

## Payloads úteis

### Criar cliente

```json
{
  "name": "Maria Cliente",
  "document": "12345678909",
  "email": "maria@email.com",
  "phone": "11999999999"
}
```

### Criar veículo

```json
{
  "customerId": 1,
  "plate": "ABC1D23",
  "brand": "Toyota",
  "model": "Corolla",
  "manufacturingYear": 2022
}
```

### Criar serviço do catálogo

```json
{
  "name": "Troca de óleo",
  "description": "Troca de óleo do motor",
  "basePrice": 180.00,
  "estimatedMinutes": 60,
  "active": true
}
```

### Criar peça/insumo

```json
{
  "name": "Filtro de óleo",
  "sku": "FILTRO-OLEO-001",
  "unitPrice": 45.90,
  "stockQuantity": 25,
  "minStock": 5,
  "active": true
}
```

### Criar ordem de serviço

```json
{
  "customerDocument": "12345678909",
  "customer": {
    "name": "Maria Cliente",
    "email": "maria@email.com",
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
    {
      "serviceItemId": 1,
      "quantity": 1
    }
  ],
  "parts": [
    {
      "partId": 1,
      "quantity": 1
    }
  ]
}
```

Ao criar a OS, o sistema:

1. normaliza e valida CPF/CNPJ;
2. normaliza e valida placa;
3. busca ou cria cliente;
4. busca ou cria veículo;
5. valida serviços e peças ativos;
6. calcula orçamento;
7. cria histórico;
8. coloca a OS em `AGUARDANDO_APROVACAO`.

### Aprovar OS pelo fluxo administrativo

```http
PATCH /api/service-orders/1/approve
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "document": "12345678909",
  "comment": "Orçamento aprovado pelo atendimento."
}
```

No fluxo administrativo, o service usa apenas o `comment`; o documento não é validado nesse endpoint.

### Atualizar status da OS

```http
PATCH /api/service-orders/1/status
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "status": "FINALIZADA",
  "comment": "Serviço finalizado."
}
```

### Consultar OS publicamente

```http
GET /api/public/service-orders/OS-20260101-12345?document=12345678909
```

### Aprovar OS publicamente

```http
POST /api/public/service-orders/OS-20260101-12345/approve
Content-Type: application/json
```

```json
{
  "document": "12345678909",
  "comment": "Aprovado pelo cliente."
}
```

## Fluxo de status da OS

Transições permitidas:

```txt
RECEBIDA -> EM_DIAGNOSTICO
RECEBIDA -> AGUARDANDO_APROVACAO
EM_DIAGNOSTICO -> AGUARDANDO_APROVACAO
AGUARDANDO_APROVACAO -> EM_EXECUCAO
EM_EXECUCAO -> FINALIZADA
FINALIZADA -> ENTREGUE
```

A criação da OS envia automaticamente para `AGUARDANDO_APROVACAO`, porque o MVP calcula o orçamento na criação.

Timestamps relevantes:

| Campo         | Quando é preenchido                             |
|---------------|-------------------------------------------------|
| `approvedAt`  | Ao aprovar orçamento                            |
| `startedAt`   | Ao aprovar orçamento ou entrar em `EM_EXECUCAO` |
| `finalizedAt` | Ao entrar em `FINALIZADA`                       |
| `deliveredAt` | Ao entrar em `ENTREGUE`                         |

## Banco de dados

O banco é PostgreSQL e o schema é versionado com Flyway.

Migração principal:

```txt
src/main/resources/db/migration/V1__init.sql
```

Tabelas criadas:

- `users`
- `customers`
- `vehicles`
- `service_catalog_items`
- `parts`
- `service_orders`
- `work_order_services`
- `work_order_parts`
- `service_order_status_history`
- `flyway_schema_history`, criada/gerenciada pelo Flyway

O desenho MER fica em:

```txt
docs/MER.drawio
```

Relações principais:

```txt
customers 1 --- N vehicles
customers 1 --- N service_orders
vehicles  1 --- N service_orders
service_orders 1 --- N work_order_services
service_catalog_items 1 --- N work_order_services
service_orders 1 --- N work_order_parts
parts 1 --- N work_order_parts
service_orders 1 --- N service_order_status_history
```

## Testes e cobertura

Rodar testes:

```bash
mvn test
```

Rodar testes com cobertura e regra JaCoCo:

```bash
mvn verify
```

Relatório do JaCoCo:

```txt
target/site/jacoco/index.html
```

O `pom.xml` configura cobertura mínima de **80% de linhas** para os pacotes/classes incluídos no plugin JaCoCo.

O projeto possui testes para:

- domínio de OS;
- política de status;
- domínio de peças;
- serviços de aplicação;
- autenticação e JWT;
- validadores de documento e placa;
- healthcheck com MockMvc;
- relatório de tempo médio.

## Documentação complementar

Arquivos importantes:

```txt
README.md
```

Visão geral do projeto, execução local, Docker, autenticação, endpoints, payloads e testes.

```txt
docs/architecture.md
```

Detalhamento da arquitetura, camadas, domínio, segurança, banco, fluxos e pontos de atenção.

```txt
docs/source-project-mapping.md
```

Mapeamento da implementação Java/Spring atual.

```txt
docs/MER.drawio
```

Modelo entidade-relacionamento visual do banco.

## Pontos de atenção

- O domínio de cada módulo permanece anotado com JPA (`@Entity`) — não há separação entre entidade de persistência e
  modelo de domínio puro; foi uma escolha pragmática para não multiplicar classes de mapeamento.
- O Hibernate está com `ddl-auto: validate`; alterações de schema devem ser feitas via Flyway.
- O campo `diagnosis` existe em `ServiceOrder` e na tabela `service_orders`, mas ainda não possui endpoint específico de
  preenchimento.
- A API carrega roles no JWT, mas atualmente a autorização dos endpoints exige autenticação sem regras granulares por
  perfil.
- O código da OS é gerado com data + número aleatório e possui unicidade no banco; ainda não existe retry explícito para
  colisão.
