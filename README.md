# Oficina MVP Backend — Java 25 + Spring Boot 4

Back-end monolítico em **Java 25**, **Maven**, **Spring Boot 4**, **PostgreSQL**, **JPA/Hibernate**, **Flyway**, **JWT** 
e **Swagger/OpenAPI**.

Este projeto é uma abstração de um negocio de uma oficina mecanica,
o backend inclui essas entidades, funionalidades e objetos de valor:

- clientes;
- veículos;
- catálogo de serviços;
- peças/insumos com estoque;
- ordens de serviço;
- histórico de status;
- orçamento automático;
- aprovação pelo cliente;
- relatório de tempo médio de execução.

## Decisões de arquitetura

O sistema é um **monolito em camadas com DDD**.

```txt
src/main/java/br/com/oficina/mvp/
  auth/            autenticação, usuário e JWT
  customers/       domínio, aplicação, infra e API de clientes
  vehicles/        domínio, aplicação, infra e API de veículos
  catalog/         domínio, aplicação, infra e API de serviços
  parts/           domínio, aplicação, infra e API de peças
  serviceorders/   domínio crítico da OS, casos de uso, API e relatório
  shared/          exceções, segurança, validações, auditoria temporal
```

A camada de domínio é mais rica onde o negócio é mais sensível:

- validação de CPF/CNPJ;
- validação de placa brasileira;
- política de transição de status da OS;
- cálculo de orçamento;
- baixa de estoque no momento de aprovação;
- histórico de status.

## Como rodar localmente

### 1. Criar `.env`

Linux/macOS:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

### 2. Subir o banco

```bash
docker compose up -d postgres
```

### 3. Rodar a aplicação

Com Java 25 e Maven instalados:

```bash
mvn spring-boot:run
```

Ou subir tudo com Docker:

```bash
docker compose up --build
```

A API sobe em:

```txt
http://localhost:3000
```

Swagger:

```txt
http://localhost:3000/swagger-ui.html
```

Health:

```txt
http://localhost:3000/api/health
```

## Usuário admin inicial

Ao iniciar, o sistema cria automaticamente um usuário admin caso não exista:

```txt
email: admin@oficina.com
senha: Admin@123
```

Esses valores podem ser alterados no `.env`.

## Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@oficina.com",
  "password": "Admin@123"
}
```

Use o token retornado nas rotas administrativas:

```http
Authorization: Bearer <token>
```

## Principais endpoints

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

### Serviços

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

### Ordens de serviço

```http
GET   /api/service-orders
POST  /api/service-orders
GET   /api/service-orders/{id}
PATCH /api/service-orders/{id}/approve
PATCH /api/service-orders/{id}/status
```

### Cliente acompanhando OS

```http
GET  /api/public/service-orders/{code}?document=12345678909
POST /api/public/service-orders/{code}/approve
```

### Relatórios

```http
GET /api/reports/average-execution-time
```

## Payload de criação de OS

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
    "year": 2022
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

## Fluxo de status da OS

```txt
RECEBIDA -> EM_DIAGNOSTICO -> AGUARDANDO_APROVACAO -> EM_EXECUCAO -> FINALIZADA -> ENTREGUE
```

Também é permitido criar a OS já com orçamento calculado e status `AGUARDANDO_APROVACAO`, como no projeto TypeScript anexado.

## Testes

```bash
mvn test
```

Com cobertura:

```bash
mvn verify
```

O relatório do JaCoCo fica em:

```txt
target/site/jacoco/index.html
```

## Observação importante

O projeto foi estruturado para Java 25 e Spring Boot 4. Para rodar fora do Docker, confirme que seu `JAVA_HOME` 
aponta para um JDK 25.
