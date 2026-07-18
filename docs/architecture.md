# Arquitetura — Oficina MVP Backend

## 1. Visão geral

O **Oficina MVP Backend** é uma API REST monolítica para gestão de uma oficina mecânica. O sistema cobre o fluxo básico
de um MVP: autenticação administrativa, cadastro de clientes, veículos, catálogo de serviços, peças/insumos com estoque,
criação de ordens de serviço, orçamento automático, aprovação pelo cliente, controle de status e relatório de tempo
médio de execução.

A arquitetura real do código é um **monolito modular organizado por módulos de negócio**, seguindo os princípios de
**Arquitetura Hexagonal (Ports & Adapters)**. Cada módulo (`customer`, `vehicle`, `catalog`, `part`, `auth`,
`serviceorder`, `report`) é um pacote físico próprio com três camadas internas:

```txt
src/main/java/br/com/oficina/mvp/
  OficinaMvpApplication.java
  <modulo>/
    domain/                     entidade JPA e regras de domínio do módulo
    application/                caso de uso (implementa as portas de entrada)
    application/port/in/        portas de entrada — interface de caso de uso + Command/Result
    application/port/out/       portas de saída — o que o módulo precisa de fora (ex: repositório)
    adapter/in/web/             adaptador de entrada — controller REST + DTOs de request/response
    adapter/out/persistence/    adaptador de saída — repositório Spring Data (package-private) + adapter da porta
  shared/
    domain/     vocabulário compartilhado entre módulos (BaseEntity, enums Role e ServiceOrderStatus)
    config/     CORS, Security, JWT properties, OpenAPI e seed
    exception/  exceções e tratamento global de erro
    security/   filtro JWT e serviço de geração/validação de token
    validation/ validadores reutilizáveis de CPF/CNPJ e placa
    api/        endpoints técnicos que não pertencem a um módulo de negócio (healthcheck)
```

Módulos existentes hoje: `customer`, `vehicle`, `catalog` (entidade `ServiceCatalogItem`), `part`, `auth` (entidade
`User`), `serviceorder` (o agregado de OS, incluindo `WorkOrderService`, `WorkOrderPart`, `ServiceOrderStatusHistory`
e `ServiceOrderStatusPolicy`) e `report`.

Regras de dependência entre camadas:

- **Domain** não depende de `application` nem de `adapter`. Continua anotado com JPA (`@Entity`) — pureza total de
  domínio (sem framework) foi conscientemente deixada de fora do escopo, para não multiplicar o número de classes com
  mapeamento manual entre entidade JPA e modelo de domínio.
- **Application** depende apenas de `domain` e das *portas* (`port.in`, `port.out`) — nunca de um `adapter` diretamente,
  nem do `adapter` de outro módulo.
- **Adapter** depende de `application` (via porta) e do próprio `domain`. Os adaptadores `in.web` fazem a conversão
  DTO ↔ Command/domínio; os adaptadores `out.persistence` implementam a porta de saída delegando para uma interface
  Spring Data `JpaRepository` package-private (não exposta fora do pacote de persistência).
- Quando um módulo precisa de outro (ex: `serviceorder` precisa buscar `Customer`, `Vehicle`, `ServiceCatalogItem` e
  `Part`), ele depende da **porta** do módulo alheio (`CustomerRepositoryPort`, `VehicleUseCase` etc.), nunca da
  implementação concreta ou do repositório Spring Data do outro módulo. Isso é o que garante que trocar a persistência
  de um módulo não obriga a mudar nada nos módulos que dependem dele.
- `Role` e `ServiceOrderStatus` (enums) e `BaseEntity` (superclasse JPA com `id`/`createdAt`/`updatedAt`) são
  vocabulário/infraestrutura genuinamente compartilhados entre módulos e ficam em `shared.domain`, não dentro de um
  módulo específico.

## 2. Stack técnica

A stack está definida principalmente no `pom.xml`, nos arquivos `.yml`, no `Dockerfile` e no `docker-compose.yml`.

| Item                   | Tecnologia/versão             | Onde aparece                                                     |
|------------------------|--------------------------------|-------------------------------------------------------------------|
| Linguagem              | Java 25                       | `pom.xml`, `Dockerfile`                                          |
| Framework              | Spring Boot 4.0.6             | `pom.xml`                                                        |
| Build                  | Maven                         | `pom.xml`, `Dockerfile`                                          |
| API HTTP               | Spring WebMVC                 | `spring-boot-starter-webmvc`                                     |
| Persistência           | Spring Data JPA + Hibernate   | `spring-boot-starter-data-jpa`, entidades em `<modulo>/domain/`  |
| Banco principal        | PostgreSQL                    | `application.yml`, `docker-compose.yml`, `V1__init.sql`          |
| Migração               | Flyway                        | `spring-boot-starter-flyway`, `src/main/resources/db/migration`  |
| Segurança              | Spring Security + JWT Bearer  | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtService`        |
| JWT                    | JJWT 0.12.6                   | `pom.xml`, `JwtService`                                          |
| Validação              | Jakarta Bean Validation       | DTOs em `<modulo>/adapter/in/web/`                               |
| Documentação API       | SpringDoc OpenAPI 3.0.3       | `OpenApiConfig`, `/swagger-ui.html`                               |
| Observabilidade básica | Spring Actuator               | `application.yml`, `/actuator/health`                             |
| Testes                 | JUnit 5, Mockito, MockMvc, H2 | `src/test/java`, `application-test.yml`                          |
| Cobertura              | JaCoCo 0.8.13                 | `pom.xml`                                                        |
| Container              | Docker multi-stage            | `Dockerfile`                                                     |

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

## 4. Módulos e responsabilidades

Cada módulo segue o mesmo esqueleto: `domain` → `application` (implementa `port.in`, depende de `port.out`) →
`adapter.in.web` (controller + DTOs) e `adapter.out.persistence` (repositório Spring Data + adapter da porta).
As tabelas abaixo listam só as classes com nome/responsabilidade específicos de cada módulo — a "receita" do adapter
de persistência (`XJpaRepository` + `XPersistenceAdapter`, ambos package-private) se repete em todos e não é listada
de novo em cada seção.

### 4.1 `customer`

| Classe                                                             | Camada               | Responsabilidade                                                     |
|----------------------------------------------------------------------|-----------------------|--------------------------------------------------------------------------|
| `Customer`                                                            | domain                | Entidade JPA (`customers`), documento normalizado                       |
| `CustomerUseCase` / `CustomerCommand`                                 | application.port.in   | Porta de entrada e comando de create/update                             |
| `CustomerRepositoryPort`                                              | application.port.out  | Porta de saída (`findByDocument`, `save`, `delete`...)                  |
| `CustomerService`                                                     | application           | Implementa `CustomerUseCase`; valida CPF/CNPJ                           |
| `CustomerController` / `CustomerRequestDto` / `CustomerResponseDto`   | adapter.in.web        | `/api/customers` — CRUD de clientes                                     |

### 4.2 `vehicle`

| Classe                                                           | Camada               | Responsabilidade                                                       |
|---------------------------------------------------------------------|-----------------------|------------------------------------------------------------------------|
| `Vehicle`                                                            | domain                | Entidade JPA (`vehicles`), vinculada a `Customer`, placa normalizada    |
| `VehicleUseCase` / `VehicleCommand`                                  | application.port.in   | Porta de entrada e comando de create/update                            |
| `VehicleRepositoryPort`                                              | application.port.out  | Porta de saída (`findByPlate`, `save`, `delete`...)                    |
| `VehicleService`                                                     | application           | Implementa `VehicleUseCase`; depende de `CustomerUseCase` (módulo `customer`) para achar o dono do veículo |
| `VehicleController` / `VehicleRequestDto` / `VehicleResponseDto`     | adapter.in.web        | `/api/vehicles` — CRUD de veículos                                      |

### 4.3 `catalog`

| Classe                                                                                  | Camada               | Responsabilidade                                          |
|-----------------------------------------------------------------------------------------|-----------------------|-------------------------------------------------------------|
| `ServiceCatalogItem`                                                                     | domain                | Entidade JPA (`service_catalog_items`)                      |
| `CatalogUseCase` / `CatalogCommand`                                                      | application.port.in   | Porta de entrada e comando de create/update                 |
| `CatalogRepositoryPort`                                                                  | application.port.out  | Porta de saída (inclui `count()`, usado pelo `DataSeeder`)   |
| `CatalogService`                                                                         | application           | Implementa `CatalogUseCase`                                 |
| `CatalogController` / `ServiceCatalogItemRequestDto` / `ServiceCatalogItemResponseDto`   | adapter.in.web        | `/api/services` — CRUD do catálogo                           |

### 4.4 `part`

| Classe                                                       | Camada               | Responsabilidade                                                       |
|-----------------------------------------------------------------|-----------------------|------------------------------------------------------------------------|
| `Part`                                                           | domain                | Entidade JPA (`parts`); `decrementStock` valida estoque insuficiente   |
| `PartUseCase` / `PartCommand`                                    | application.port.in   | Porta de entrada e comando de create/update                            |
| `PartRepositoryPort`                                             | application.port.out  | Porta de saída (inclui `count()`, usado pelo `DataSeeder`)              |
| `PartService`                                                    | application           | Implementa `PartUseCase`                                               |
| `PartController` / `PartRequestDto` / `PartResponseDto`          | adapter.in.web        | `/api/parts` — CRUD de peças/insumos                                    |

### 4.5 `auth`

| Classe                                                    | Camada               | Responsabilidade                                                          |
|----------------------------------------------------------|-----------------------|-------------------------------------------------------------------------------|
| `User`                                                    | domain                | Entidade JPA (`users`)                                                       |
| `AuthUseCase` / `LoginCommand` / `AuthResult`             | application.port.in   | Porta de entrada; `AuthResult` carrega token + usuário autenticado           |
| `UserRepositoryPort`                                      | application.port.out  | Porta de saída (`findByEmail`, `existsByEmail`, `findById`, `save`)          |
| `AuthService`                                              | application           | Valida credenciais com `PasswordEncoder` e gera JWT via `JwtService` (`shared.security`) |
| `AuthController` / `LoginRequestDto` / `AuthResponseDto`  | adapter.in.web        | `/api/auth/login` — login administrativo                                     |

`shared.security.JwtAuthenticationFilter` também depende de `UserRepositoryPort` (não do adapter concreto) para
resolver o usuário autenticado a cada requisição — um exemplo de componente `shared` consumindo a porta de um
módulo de negócio.

### 4.6 `serviceorder`

Módulo mais complexo: é o único agregado que orquestra os quatro módulos anteriores.

| Classe                                                                                                                                              | Camada               | Responsabilidade                                                        |
|--------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------|-------------------------------------------------------------------------|
| `ServiceOrder`                                                                                                                                      | domain                | Agregado principal; calcula totais, registra histórico e altera status  |
| `WorkOrderService` / `WorkOrderPart` / `WorkOrderLineItem`                                                                                          | domain                | Itens de serviço/peça da OS com preço congelado; `WorkOrderLineItem` é a superclasse comum (`@MappedSuperclass`) |
| `ServiceOrderStatusHistory`                                                                                                                         | domain                | Histórico de mudança de status                                          |
| `ServiceOrderStatusPolicy`                                                                                                                          | domain                | Define transições de status permitidas                                  |
| `ServiceOrderUseCase` / `CreateServiceOrderCommand`                                                                                                 | application.port.in   | Porta administrativa: listar, criar, aprovar, trocar status              |
| `PublicServiceOrderUseCase`                                                                                                                         | application.port.in   | Porta pública: consulta e aprovação pelo cliente                        |
| `ServiceOrderRepositoryPort`                                                                                                                        | application.port.out  | Porta de saída (`findByCode`, `save`...)                                 |
| `ServiceOrderService`                                                                                                                               | application           | Implementa as duas portas de entrada; depende de `CustomerRepositoryPort`, `VehicleRepositoryPort`, `CatalogRepositoryPort` e `PartRepositoryPort` (portas de saída dos outros módulos) para orquestrar a criação da OS |
| `ServiceOrderController`                                                                                                                            | adapter.in.web        | `/api/service-orders` — fluxo administrativo                             |
| `PublicServiceOrderController`                                                                                                                      | adapter.in.web        | `/api/public/service-orders` — consulta e aprovação pública              |
| `CreateServiceOrderRequestDto`, `ServiceOrderResponseDto`, `PublicServiceOrderResponseDto`, `UpdateStatusRequestDto`, `CustomerApprovalRequestDto` | adapter.in.web        | DTOs de request/response do módulo                                       |

### 4.7 `report`

Módulo só de leitura — não tem `domain` nem `adapter.out.persistence` próprios; lê os dados através da porta de
saída do módulo `serviceorder`.

| Classe                                                    | Camada               | Responsabilidade                                                              |
|------------------------------------------------------------|-----------------------|----------------------------------------------------------------------------------|
| `ReportUseCase` / `AverageExecutionTimeResult`              | application.port.in   | Porta de entrada e resultado do cálculo                                          |
| `ReportService`                                             | application           | Implementa `ReportUseCase`; depende de `ServiceOrderRepositoryPort` (módulo `serviceorder`) |
| `ReportController` / `AverageExecutionTimeResponseDto`      | adapter.in.web        | `/api/reports/average-execution-time`                                            |

### 4.8 `shared`

Pacote: `br.com.oficina.mvp.shared` — recursos transversais que não pertencem a um módulo de negócio específico:

| Classe/pacote                      | Responsabilidade                                                                 |
|--------------------------------------|------------------------------------------------------------------------------------|
| `shared.domain.BaseEntity`           | Superclasse JPA com `id`, `createdAt`, `updatedAt`, usada por todas as entidades   |
| `shared.domain.Role`                 | Enum de papel do usuário (`ADMIN`, `ATTENDANT`, `MECHANIC`)                        |
| `shared.domain.ServiceOrderStatus`   | Enum de status da OS, usado pelo módulo `serviceorder` e por quem consulta o status |
| `SecurityConfig`                     | Configuração de segurança, CORS, sessão stateless, rotas públicas e filtro JWT      |
| `JwtAuthenticationFilter`            | Lê `Authorization: Bearer`, valida token e preenche o `SecurityContext`             |
| `JwtService`                         | Gera e faz parse do JWT                                                             |
| `JwtProperties`                      | Propriedades `app.jwt.*`                                                            |
| `AppCorsProperties`                  | Propriedade `app.cors.allowed-origins`                                              |
| `OpenApiConfig`                      | Metadados do Swagger/OpenAPI e security scheme Bearer                               |
| `DataSeeder`                         | Seed inicial de admin, serviços e peças fora do profile `test`                      |
| `DocumentValidator`                  | Normalização e validação de CPF/CNPJ                                                |
| `PlateValidator`                     | Normalização e validação de placa antiga e Mercosul                                 |
| `BusinessException`                  | Exceção de negócio com status HTTP e detalhes opcionais                             |
| `GlobalExceptionHandler`             | Padronização de erros HTTP                                                          |
| `ApiError`                           | Contrato de erro retornado pela API                                                 |
| `shared.api.HealthController`        | `/api/health` — healthcheck simples da API                                          |

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
|---------------------------------|---------------------------------------------|
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
  -> AuthController (auth.adapter.in.web)
  -> AuthService.login (auth.application, implementa AuthUseCase)
  -> UserRepositoryPort.findByEmail
  -> PasswordEncoder.matches
  -> JwtService.generate
  -> AuthResponseDto.from(AuthResult)
```

### 7.2 Criação de ordem de serviço

```txt
POST /api/service-orders
  -> ServiceOrderController.create (serviceorder.adapter.in.web)
  -> ServiceOrderService.create (serviceorder.application, implementa ServiceOrderUseCase)
  -> normaliza e valida CPF/CNPJ
  -> normaliza e valida placa
  -> busca ou cria cliente por documento (via CustomerRepositoryPort)
  -> busca ou cria veículo por placa (via VehicleRepositoryPort)
  -> cria ServiceOrder com status RECEBIDA
  -> adiciona serviços ativos do catálogo (via CatalogRepositoryPort)
  -> adiciona peças ativas, quando informadas (via PartRepositoryPort)
  -> calcula total de serviços, peças e total geral
  -> altera status para AGUARDANDO_APROVACAO
  -> registra histórico inicial e histórico de orçamento enviado
  -> persiste OS, itens e histórico por cascade (via ServiceOrderRepositoryPort)
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
  -> PublicServiceOrderController.status
  -> ServiceOrderService.findByCode (implementa PublicServiceOrderUseCase)
  -> normaliza document
  -> confere se o documento pertence ao cliente da OS
  -> retorna visão pública da OS
```

Aprovação pública:

```txt
POST /api/public/service-orders/{code}/approve
  -> PublicServiceOrderController.approve
  -> ServiceOrderService.approveByCustomer
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
|---------------|---------------------------------------|
| `EM_EXECUCAO` | `startedAt`, se ainda estiver vazio |
| `FINALIZADA`  | `finalizedAt`                       |
| `ENTREGUE`    | `deliveredAt`                       |

Na aprovação, `approvedAt` e `startedAt` são preenchidos juntos.

### 7.6 Relatório de tempo médio

```txt
GET /api/reports/average-execution-time
  -> ReportController (report.adapter.in.web)
  -> ReportService.averageExecutionTime (report.application, implementa ReportUseCase)
  -> busca todas as OS via ServiceOrderRepositoryPort (módulo serviceorder)
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
|------------------------------------|----------------------------------------|
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

O projeto possui testes unitários e de integração em `src/test/java`, espelhando o pacote da classe testada
(ex: `serviceorder.application.ServiceOrderServiceTest` testa `serviceorder.application.ServiceOrderService`).
Cobrem:

- entidades de domínio;
- política de transição de status;
- serviços de aplicação (mockando as portas de saída, inclusive as de outros módulos);
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

1. O domínio permanece anotado com JPA (`@Entity`) dentro de cada módulo — não há separação entre entidade de
   persistência e modelo de domínio puro. Essa foi uma escolha pragmática para não multiplicar classes de mapeamento;
   se o projeto crescer a ponto de precisar de modelos de leitura/escrita distintos, essa é a primeira fronteira a
   revisar.
2. O campo `diagnosis` existe na entidade e na tabela `service_orders`, mas atualmente não há endpoint dedicado para
   preenchê-lo.
3. O código da OS é gerado com data + número aleatório e possui unicidade no banco. Não há retry explícito caso ocorra
   colisão de código.
4. A segurança já carrega role no JWT e no `SecurityContext`, mas os endpoints ainda não usam autorização granular por
   perfil.
5. O módulo `report` não tem `domain` nem `adapter.out.persistence` próprios — ele lê diretamente pela porta de saída
   do módulo `serviceorder` (`ServiceOrderRepositoryPort`). É uma exceção deliberada ao esqueleto padrão dos módulos,
   já que `report` é puramente um caso de uso de leitura sobre dados de outro módulo.
