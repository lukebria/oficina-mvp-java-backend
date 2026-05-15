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

O projeto foi estruturado como um **monolito em camadas**, adotando uma abordagem pragmática de **DDD (Domain-Driven Design)**.

Na prática, isso significa que a aplicação está concentrada em um único deploy/backend, mas organizada em camadas com 
responsabilidades bem definidas, separando entrada HTTP, regras de aplicação, domínio, persistência e recursos compartilhados.

A abordagem de DDD é pragmática porque o projeto não aplica todos os padrões de DDD de forma rígida, como agregados complexos, 
bounded contexts isolados ou módulos separados por domínio. Ainda assim, ele preserva conceitos importantes, como entidades
de domínio, regras encapsuladas nas entidades, políticas de domínio e serviços de aplicação responsáveis por orquestrar os casos de uso.

A separação atual do código é feita por responsabilidade técnica, não por módulos de domínio isolados. Ou seja:
em vez de existir uma pasta para cada contexto de negócio, como `customers/`, `vehicles/` ou `serviceorders/`, 
o projeto concentra as classes nas camadas abaixo:

```txt
src/main/java/br/com/oficina/mvp/
  controllers/     endpoints REST da API
  domains/         entidades centrais do dominio do négocio e regras de domínio acopladas às entidades
  dtos/            objetos de entrada e saída da API - Não mutaveis.
  infra/           repositórios Spring Data JPA
  services/        casos de uso, orquestração e regras de aplicação
  shared/          configurações, segurança, exceções e validações reutilizáveis
```

As principais regras de negócio ficam centralizadas nas entidades de domínio e nos serviços de aplicação.

Detalhamento de camadas:

- `controllers`: camada de entrada da API. Recebe as requisições HTTP, valida os DTOs recebidos e encaminha a execução para os serviços responsáveis;
- `services`: camada de aplicação. Orquestra os principais fluxos, como autenticação, cadastros, criação e 
- aprovação de ordens de serviço, mudanças de status, baixa de estoque e geração de relatórios;
- `dtos`: camada para representação de objetos de entrada e saida de apis, objetos não mutaveis e enumerators.
- `domains`: camada de domínio. Contém as entidades centrais do negócio, como cliente, veículo, peça, serviço de catálogo, 
- usuário, ordem de serviço e histórico de status. Também concentra comportamentos próprios dessas entidades, como cálculo 
- de totais, inclusão de itens e validação de transições de status da OS;
- `infra`: camada de infraestrutura. Centraliza os contratos de persistência e comunicação com o banco de dados, utilizando Spring Data JPA;
- `shared`: camada de recursos compartilhados. Reúne funcionalidades transversais da aplicação, como segurança JWT, CORS,
- tratamento global de erros, seed inicial, documentação OpenAPI, exceções de negócio e validadores reutilizáveis.

As regras de domínio mais relevantes estão concentradas principalmente no fluxo de Ordem de Serviço:

- validação de CPF/CNPJ;
- validação de placa brasileira;
- política de transição de status da OS;
- cálculo automático de orçamento com serviços e peças;
- validação de estoque antes da aprovação;
- baixa de estoque no momento da aprovação;
- registro de histórico de status.

Essa estrutura favorece legibilidade e velocidade de desenvolvimento para o MVP. Caso o sistema cresça, 
uma evolução natural seria reorganizar o código por domínio/contexto, separando pacotes como 
`customers`, `vehicles`, `catalog`, `parts`, `serviceorders` e `auth`, cada um com suas próprias camadas internas.

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

Também é permitido criar a OS já com orçamento calculado e status `AGUARDANDO_APROVACAO`.

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

Projeto foi estruturado para Java 25 e Spring Boot 4.
