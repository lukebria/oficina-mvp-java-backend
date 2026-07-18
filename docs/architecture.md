# Arquitetura — Oficina MVP Backend

## 1. Visão geral

O **Oficina MVP Backend** é uma API REST monolítica para gestão de uma oficina mecânica. O sistema cobre o fluxo básico
de um MVP: autenticação administrativa, cadastro de clientes, veículos, catálogo de serviços, peças/insumos com estoque,
criação de ordens de serviço, orçamento automático, aprovação pelo cliente, controle de status e relatório de tempo
médio de execução.

A arquitetura real do código é um **monolito em camadas**, com uma abordagem pragmática de DDD.
O projeto não está separado fisicamente em pacotes por domínio, como `customers/`, `vehicles/` ou `serviceorders/`. Em
vez disso, ele usa pacotes por responsabilidade técnica:

```txt
src/main/java/br/com/oficina/mvp/
  OficinaMvpApplication.java
  controllers/       entrada HTTP e endpoints REST
  domains/           entidades JPA e regras de domínio
  domains/base/      entidade base com id e timestamps
  dtos/              contratos de entrada e saída da API
  dtos/enums/        enums usados por domínio e DTOs
  infra/             repositórios Spring Data JPA
  services/          casos de uso e orquestração de aplicação
  shared/config/     CORS, Security, JWT properties, OpenAPI e seed
  shared/exception/  exceções e tratamento global de erro
  shared/security/   filtro JWT e serviço de geração/validação de token
  shared/validation/ validadores reutilizáveis de CPF/CNPJ e placa
```

Essa organização favorece simplicidade e velocidade de desenvolvimento, que combinam com o objetivo de MVP. Caso o
projeto cresça, uma evolução natural seria separar os pacotes por contexto de negócio, por exemplo `auth`, `customers`,
`vehicles`, `catalog`, `parts`, `serviceorders` e `reports`, cada um com suas próprias camadas internas.

## 2. Stack técnica

A stack está definida principalmente no `pom.xml`, nos arquivos `.yml`, no `Dockerfile` e no `docker-compose.yml`.

| Item                   | Tecnologia/versão             | Onde aparece                                                    |
|------------------------|-------------------------------|-----------------------------------------------------------------|
| Linguagem              | Java 25                       | `pom.xml`, `Dockerfile`                                         |
| Framework              | Spring Boot 4.0.6             | `pom.xml`                                                       |
| Build                  | Maven                         | `pom.xml`, `Dockerfile`                                         |
| API HTTP               | Spring WebMVC                 | `spring-boot-starter-webmvc`                                    |
| Persistência           | Spring Data JPA + Hibernate   | `spring-boot-starter-data-jpa`, entidades em `domains/`         |
| Banco principal        | PostgreSQL                    | `application.yml`, `docker-compose.yml`, `V1__init.sql`         |
| Migração               | Flyway                        | `spring-boot-starter-flyway`, `src/main/resources/db/migration` |
| Segurança              | Spring Security + JWT Bearer  | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtService`       |
| JWT                    | JJWT 0.12.6                   | `pom.xml`, `JwtService`                                         |
| Validação              | Jakarta Bean Validation       | DTOs em `dtos/`                                                 |
| Documentação API       | SpringDoc OpenAPI 3.0.3       | `OpenApiConfig`, `/swagger-ui.html`                             |
| Observabilidade básica | Spring Actuator               | `application.yml`, `/actuator/health`                           |
| Testes                 | JUnit 5, Mockito, MockMvc, H2 | `src/test/java`, `application-test.yml`                         |
| Cobertura              | JaCoCo 0.8.13                 | `pom.xml`                                                       |
| Container              | Docker multi-stage            | `Dockerfile`                                                    |

## 3. Configuração de execução

A aplicação sobe, por padrão, na porta `3000`:

```yaml
server:
  port: ${APP_PORT:3000}
```

O datasource usa PostgreSQL e variáveis de ambiente com fallback local:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:oficina_mvp}
    username: ${DB_USER:oficina}
    password: ${DB_PASSWORD:oficina}
```

O Hibernate está configurado com `ddl-auto: validate`, então a aplicação **não cria nem altera o schema automaticamente
**. O schema é versionado pelo Flyway em `src/main/resources/db/migration/V1__init.sql`.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
```

No ambiente de testes, o projeto usa H2 em memória com modo PostgreSQL e `ddl-auto: create-drop`; o Flyway fica
desabilitado em `src/test/resources/application-test.yml`.

## 4. Camadas e responsabilidades

### 4.1 Controllers

Pacote: `br.com.oficina.mvp.controllers`

Responsável por expor os endpoints REST e delegar os casos de uso para os serviços.

| Controller                     | Base path                    | Responsabilidade                                |
|--------------------------------|------------------------------|-------------------------------------------------|
| `AuthController`               | `/api/auth`                  | Login administrativo e emissão de JWT           |
| `CustomerController`           | `/api/customers`             | CRUD de clientes                                |
| `VehicleController`            | `/api/vehicles`              | CRUD de veículos                                |
| `CatalogController`            | `/api/services`              | CRUD do catálogo de serviços                    |
| `PartController`               | `/api/parts`                 | CRUD de peças/insumos                           |
| `ServiceOrderController`       | `/api/service-orders`        | Fluxo administrativo de OS                      |
| `PublicServiceOrderController` | `/api/public/service-orders` | Consulta e aprovação pública de OS pelo cliente |
| `ReportController`             | `/api/reports`               | Relatórios operacionais                         |
| `HealthController`             | `/api/health`                | Healthcheck simples da API                      |

### 4.2 Services

Pacote: `br.com.oficina.mvp.services`

Responsável por orquestrar regras de aplicação, transações, validações de negócio e integração entre repositórios.

| Service                          | Responsabilidade principal                                                                           |
|----------------------------------|------------------------------------------------------------------------------------------------------|
| `AuthService`                    | Valida credenciais com `PasswordEncoder` e retorna JWT via `JwtService`                              |
| `CustomerService`                | CRUD de cliente, normalização e validação de CPF/CNPJ                                                |
| `VehicleService`                 | CRUD de veículo, normalização e validação de placa brasileira                                        |
| `CatalogService`                 | CRUD de itens do catálogo de serviços                                                                |
| `PartService`                    | CRUD de peças/insumos e controle dos dados de estoque                                                |
| `ServiceOrderApplicationService` | Criação de OS, orçamento automático, aprovação, baixa de estoque, consulta pública e troca de status |
| `ReportService`                  | Cálculo do tempo médio entre início e finalização das OS                                             |

### 4.3 Domains

Pacote: `br.com.oficina.mvp.domains`

Responsável pelas entidades centrais, mapeamento JPA e regras de domínio próximas dos dados.

| Entidade/classe             | Tabela/uso                     | Regra relevante                                                                       |
|-----------------------------|--------------------------------|---------------------------------------------------------------------------------------|
| `User`                      | `users`                        | Representa usuário administrativo autenticável                                        |
| `Customer`                  | `customers`                    | Dados do cliente e documento normalizado                                              |
| `Vehicle`                   | `vehicles`                     | Veículo vinculado a cliente e placa normalizada                                       |
| `ServiceCatalogItem`        | `service_catalog_items`        | Serviço vendável com preço base e tempo estimado                                      |
| `Part`                      | `parts`                        | Peça/insumo com preço, estoque, estoque mínimo e flag ativo                           |
| `ServiceOrder`              | `service_orders`               | Agregado principal do fluxo de OS; calcula totais, registra histórico e altera status |
| `WorkOrderService`          | `work_order_services`          | Item de serviço da OS com preço congelado no momento da criação                       |
| `WorkOrderPart`             | `work_order_parts`             | Item de peça da OS com preço congelado no momento da criação                          |
| `ServiceOrderStatusHistory` | `service_order_status_history` | Registro histórico de mudança/status da OS                                            |
| `ServiceOrderStatusPolicy`  | classe de domínio              | Define transições de status permitidas                                                |
| `BaseEntity`                | superclass                     | Centraliza `id`, `createdAt` e `updatedAt`                                            |

### 4.4 DTOs

Pacote: `br.com.oficina.mvp.dtos`

Os DTOs são implementados como `record`, deixando claros os contratos de entrada e saída da API. Eles usam Bean
Validation, por exemplo `@NotBlank`, `@NotNull`, `@Size`, `@Email`, `@Positive`, `@Min`, `@Max` e `@DecimalMin`.

Principais contratos:

| DTO                                                              | Uso                                                  |
|------------------------------------------------------------------|------------------------------------------------------|
| `LoginRequestDto`                                                | Entrada do login                                     |
| `AuthResponseDto`                                                | Saída do login com token e dados do usuário          |
| `CustomerRequestDto` / `CustomerResponseDto`                     | CRUD de clientes                                     |
| `VehicleRequestDto` / `VehicleResponseDto`                       | CRUD de veículos                                     |
| `ServiceCatalogItemRequestDto` / `ServiceCatalogItemResponseDto` | CRUD do catálogo                                     |
| `PartRequestDto` / `PartResponseDto`                             | CRUD de peças/insumos                                |
| `CreateServiceOrderRequestDto`                                   | Criação de OS com cliente, veículo, serviços e peças |
| `ServiceOrderResponseDto`                                        | Detalhamento administrativo da OS                    |
| `PublicServiceOrderResponseDto`                                  | Visão pública da OS para o cliente                   |
| `CustomerApprovalRequestDto`                                     | Aprovação pública ou administrativa do orçamento     |
| `UpdateStatusRequestDto`                                         | Troca de status da OS                                |
| `AverageExecutionTimeResponseDto`                                | Resposta do relatório de tempo médio                 |

### 4.5 Infra

Pacote: `br.com.oficina.mvp.infra`

Contém repositórios Spring Data JPA:

| Repository                     | Entidade             | Métodos adicionais             |
|--------------------------------|----------------------|--------------------------------|
| `UserRepository`               | `User`               | `findByEmail`, `existsByEmail` |
| `CustomerRepository`           | `Customer`           | `findByDocument`               |
| `VehicleRepository`            | `Vehicle`            | `findByPlate`                  |
| `ServiceCatalogItemRepository` | `ServiceCatalogItem` | apenas `JpaRepository`         |
| `PartRepository`               | `Part`               | apenas `JpaRepository`         |
| `ServiceOrderRepository`       | `ServiceOrder`       | `findByCode`                   |

### 4.6 Shared

Pacote: `br.com.oficina.mvp.shared`

Concentra recursos transversais:

| Classe                    | Responsabilidade                                                               |
|---------------------------|--------------------------------------------------------------------------------|
| `SecurityConfig`          | Configuração de segurança, CORS, sessão stateless, rotas públicas e filtro JWT |
| `JwtAuthenticationFilter` | Lê `Authorization: Bearer`, valida token e preenche o `SecurityContext`        |
| `JwtService`              | Gera e faz parse do JWT                                                        |
| `JwtProperties`           | Propriedades `app.jwt.*`                                                       |
| `AppCorsProperties`       | Propriedade `app.cors.allowed-origins`                                         |
| `OpenApiConfig`           | Metadados do Swagger/OpenAPI e security scheme Bearer                          |
| `DataSeeder`              | Seed inicial de admin, serviços e peças fora do profile `test`                 |
| `DocumentValidator`       | Normalização e validação de CPF/CNPJ                                           |
| `PlateValidator`          | Normalização e validação de placa antiga e Mercosul                            |
| `BusinessException`       | Exceção de negócio com status HTTP e detalhes opcionais                        |
| `GlobalExceptionHandler`  | Padronização de erros HTTP                                                     |
| `ApiError`                | Contrato de erro retornado pela API                                            |

## 5. Segurança

A segurança é stateless e usa JWT Bearer.

### Rotas públicas

Configuradas em `SecurityConfig`:

```txt
POST /api/auth/login
/api/public/**
/api/health
/actuator/health
/swagger-ui.html
/swagger-ui/**
/v3/api-docs/**
```

### Rotas autenticadas

As demais rotas exigem header:

```http
Authorization: Bearer <token>
```

O JWT carrega os claims:

```txt
subject = id do usuário
role    = role do usuário
email   = email do usuário
```

O filtro `JwtAuthenticationFilter` transforma a role em authority `ROLE_<ROLE>`. Atualmente, a configuração exige
autenticação, mas não aplica regras finas por perfil em endpoints específicos.

### CORS

As origens permitidas vêm de:

```yaml
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://localhost:3000}
```

Métodos liberados: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.

Headers liberados: `Authorization`, `Content-Type`.

## 6. Banco de dados e MER

O modelo relacional está definido no Flyway em `src/main/resources/db/migration/V1__init.sql` e representado visualmente
em `docs/MER.drawio`.

### Tabelas principais

| Tabela                         | Finalidade                                 |
|--------------------------------|--------------------------------------------|
| `users`                        | Usuários administrativos para autenticação |
| `customers`                    | Clientes da oficina                        |
| `vehicles`                     | Veículos vinculados a clientes             |
| `service_catalog_items`        | Catálogo de serviços disponíveis           |
| `parts`                        | Peças/insumos com controle de estoque      |
| `service_orders`               | Ordem de serviço principal                 |
| `work_order_services`          | Serviços adicionados à OS                  |
| `work_order_parts`             | Peças adicionadas à OS                     |
| `service_order_status_history` | Histórico de status da OS                  |
| `flyway_schema_history`        | Controle interno do Flyway                 |

### Relacionamentos principais

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

### Regras de integridade no schema

O SQL inicial define:

- chaves primárias `BIGSERIAL`;
- unicidade para `users.email`, `customers.document`, `vehicles.plate`, `service_catalog_items.name`, `parts.sku` e
  `service_orders.code`;
- índices para FKs e status de OS;
- `CHECK` para status de OS;
- `CHECK` para role de usuário;
- `ON DELETE CASCADE` para veículos de cliente e itens/histórico dependentes da OS.

## 7. Fluxos de negócio

### 7.1 Login administrativo

```txt
POST /api/auth/login
  -> AuthController
  -> AuthService
  -> UserRepository.findByEmail
  -> PasswordEncoder.matches
  -> JwtService.generate
  -> AuthResponseDto(token, user)
```

### 7.2 Criação de ordem de serviço

```txt
POST /api/service-orders
  -> ServiceOrderController.create
  -> ServiceOrderApplicationService.create
  -> normaliza e valida CPF/CNPJ
  -> normaliza e valida placa
  -> busca ou cria cliente por documento
  -> busca ou cria veículo por placa
  -> cria ServiceOrder com status RECEBIDA
  -> adiciona serviços ativos do catálogo
  -> adiciona peças ativas, quando informadas
  -> calcula total de serviços, peças e total geral
  -> altera status para AGUARDANDO_APROVACAO
  -> registra histórico inicial e histórico de orçamento enviado
  -> persiste OS, itens e histórico por cascade
```

Observação: os preços de serviços e peças são copiados para `WorkOrderService` e `WorkOrderPart` no momento da criação.
Assim, alterações futuras no catálogo ou no preço da peça não mudam o orçamento já gerado.

### 7.3 Aprovação administrativa da OS

```txt
PATCH /api/service-orders/{id}/approve
  -> busca OS
  -> valida estoque das peças
  -> decrementa estoque
  -> transiciona AGUARDANDO_APROVACAO -> EM_EXECUCAO
  -> preenche approvedAt e startedAt
  -> registra histórico
```

### 7.4 Consulta e aprovação pública pelo cliente

Consulta pública:

```txt
GET /api/public/service-orders/{code}?document=<cpf-ou-cnpj>
  -> busca OS pelo code
  -> normaliza document
  -> confere se o documento pertence ao cliente da OS
  -> retorna visão pública da OS
```

Aprovação pública:

```txt
POST /api/public/service-orders/{code}/approve
  -> normaliza document
  -> valida se o documento pertence ao cliente
  -> exige status AGUARDANDO_APROVACAO
  -> valida estoque
  -> decrementa estoque
  -> muda status para EM_EXECUCAO
  -> registra histórico
```

### 7.5 Troca de status

```txt
PATCH /api/service-orders/{id}/status
  -> busca OS
  -> valida transição em ServiceOrderStatusPolicy
  -> atualiza status
  -> preenche timestamps quando aplicável
  -> registra histórico
```

Transições permitidas:

```txt
RECEBIDA -> EM_DIAGNOSTICO
RECEBIDA -> AGUARDANDO_APROVACAO
EM_DIAGNOSTICO -> AGUARDANDO_APROVACAO
AGUARDANDO_APROVACAO -> EM_EXECUCAO
EM_EXECUCAO -> FINALIZADA
FINALIZADA -> ENTREGUE
ENTREGUE -> sem próximas transições
```

Timestamps preenchidos no domínio:

| Status        | Campo preenchido                    |
|---------------|-------------------------------------|
| `EM_EXECUCAO` | `startedAt`, se ainda estiver vazio |
| `FINALIZADA`  | `finalizedAt`                       |
| `ENTREGUE`    | `deliveredAt`                       |

Na aprovação, `approvedAt` e `startedAt` são preenchidos juntos.

### 7.6 Relatório de tempo médio

```txt
GET /api/reports/average-execution-time
  -> ReportController
  -> ReportService
  -> busca todas as OS
  -> filtra OS com startedAt e finalizedAt preenchidos
  -> calcula média em minutos e horas
```

## 8. Tratamento de erros

O `GlobalExceptionHandler` centraliza as respostas de erro no formato `ApiError`:

```json
{
  "timestamp": "2026-01-01T10:00:00-03:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados inválidos.",
  "path": "/api/customers",
  "details": {
    "document": "must not be blank"
  }
}
```

Mapeamentos principais:

| Exceção                           | HTTP                                 |
|-----------------------------------|--------------------------------------|
| `BusinessException`               | Status definido pela própria exceção |
| `MethodArgumentNotValidException` | `400 Bad Request`                    |
| `ConstraintViolationException`    | `400 Bad Request`                    |
| `DataIntegrityViolationException` | `409 Conflict`                       |
| `Exception`                       | `500 Internal Server Error`          |

## 9. Seed inicial

A classe `DataSeeder` roda no start da aplicação, exceto no profile `test`.

Ela cria, caso ainda não existam:

- usuário administrador inicial;
- serviços iniciais: Troca de óleo, Alinhamento e Diagnóstico eletrônico;
- peças iniciais: Filtro de óleo, Óleo 5W30 e Pastilha de freio.

Credenciais padrão configuráveis via `.env`:

```txt
SEED_ADMIN_EMAIL=admin@oficina.com
SEED_ADMIN_PASSWORD=Admin@123
```

## 10. Testes e qualidade

O projeto possui testes unitários e de integração em `src/test/java`, cobrindo:

- entidades de domínio;
- política de transição de status;
- serviços de aplicação;
- autenticação;
- JWT;
- validadores de CPF/CNPJ e placa;
- healthcheck com MockMvc.

O `pom.xml` configura JaCoCo com relatório na fase `verify` e regra mínima de **90% de cobertura de linha** para os
pacotes/classes incluídos na configuração do plugin.

Comandos:

```bash
mvn test
mvn verify
```

Relatório:

```txt
target/site/jacoco/index.html
```

## 11. Deploy/container

O `Dockerfile` usa build multi-stage:

1. `maven:3.9.11-eclipse-temurin-25` para baixar dependências e empacotar a aplicação;
2. `eclipse-temurin:25-jre` para executar o `app.jar`.

O `docker-compose.yml` sobe:

- `postgres`, usando `postgres:16-alpine`;
- `api`, construída a partir do `Dockerfile` e dependente do healthcheck do Postgres.

## 12. Pontos de atenção atuais

Estes pontos refletem o estado atual do código e podem ser úteis para manutenção futura:

1. O código está organizado por camadas técnicas, não por módulos de domínio. A documentação deve evitar sugerir que
   existem pacotes físicos como `auth/`, `customers/` ou `serviceorders/`.
2. O campo `diagnosis` existe na entidade e na tabela `service_orders`, mas atualmente não há endpoint dedicado para
   preenchê-lo.
3. O código da OS é gerado com data + número aleatório e possui unicidade no banco. Não há retry explícito caso ocorra
   colisão de código.
4. A segurança já carrega role no JWT e no `SecurityContext`, mas os endpoints ainda não usam autorização granular por
   perfil.
