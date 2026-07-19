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
| `VehicleJpaRepository` / `VehiclePersistenceAdapter`                 | adapter.out.persistence | O adapter inicializa (`Hibernate.initialize`) a associação `LAZY` `customer` antes de retornar, já que `VehicleResponseDto` a acessa fora da transação (`open-in-view: false`) |

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
| `ServiceOrderUseCase` / `CreateServiceOrderCommand`                                                                                                 | application.port.in   | Porta administrativa: listar, criar, aprovar, trocar status, preencher diagnóstico |
| `PublicServiceOrderUseCase`                                                                                                                         | application.port.in   | Porta pública: consulta e aprovação pelo cliente                        |
| `ServiceOrderRepositoryPort`                                                                                                                        | application.port.out  | Porta de saída (`findByCode`, `existsByCode`, `save`...)                 |
| `ServiceOrderService`                                                                                                                               | application           | Implementa as duas portas de entrada; depende de `CustomerRepositoryPort`, `VehicleRepositoryPort`, `CatalogRepositoryPort` e `PartRepositoryPort` (portas de saída dos outros módulos) para orquestrar a criação da OS |
| `ServiceOrderController`                                                                                                                            | adapter.in.web        | `/api/service-orders` — fluxo administrativo                             |
| `PublicServiceOrderController`                                                                                                                      | adapter.in.web        | `/api/public/service-orders` — consulta e aprovação pública              |
| `CreateServiceOrderRequestDto`, `ServiceOrderResponseDto`, `PublicServiceOrderResponseDto`, `UpdateStatusRequestDto`, `UpdateDiagnosisRequestDto`, `CustomerApprovalRequestDto` | adapter.in.web        | DTOs de request/response do módulo                                       |
| `ServiceOrderJpaRepository` / `ServiceOrderPersistenceAdapter`                                                                                      | adapter.out.persistence | Além de delegar ao Spring Data, o adapter inicializa (`Hibernate.initialize`) as associações `LAZY` da OS (cliente, veículo, serviços, peças, histórico) antes de retornar, já que `open-in-view` é `false` e o mapeamento para DTO acontece no controller, fora da transação |

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

O filtro `JwtAuthenticationFilter` transforma a role em authority `ROLE_<ROLE>`, o que permite usar
`hasRole`/`hasAnyRole` em `SecurityConfig`.

### Autorização por perfil

Além da autenticação, `SecurityConfig` aplica autorização granular por rota + método HTTP, usando os perfis do enum
`Role` (`ADMIN`, `MECHANIC`, `ATTENDANT`):

| Rota                                              | Método | Perfis permitidos          |
|----------------------------------------------------|--------|------------------------------|
| `/api/customers`, `/api/customers/{id}`             | GET    | ADMIN, MECHANIC, ATTENDANT |
| `/api/customers`                                    | POST   | ADMIN, MECHANIC, ATTENDANT |
| `/api/customers/{id}`                               | PUT    | ADMIN, MECHANIC, ATTENDANT |
| `/api/customers/{id}`                               | DELETE | ADMIN                      |
| `/api/vehicles`, `/api/vehicles/{id}`               | GET    | ADMIN, MECHANIC, ATTENDANT |
| `/api/vehicles`                                     | POST   | ADMIN, MECHANIC, ATTENDANT |
| `/api/vehicles/{id}`                                | PUT    | ADMIN, MECHANIC, ATTENDANT |
| `/api/vehicles/{id}`                                | DELETE | ADMIN                      |
| `/api/services`, `/api/services/{id}`               | GET    | ADMIN, MECHANIC, ATTENDANT |
| `/api/services`                                     | POST   | ADMIN                      |
| `/api/services/{id}`                                | PUT    | ADMIN                      |
| `/api/services/{id}`                                | DELETE | ADMIN                      |
| `/api/parts`, `/api/parts/{id}`                     | GET    | ADMIN, MECHANIC, ATTENDANT |
| `/api/parts`                                        | POST   | ADMIN                      |
| `/api/parts/{id}`                                   | PUT    | ADMIN                      |
| `/api/parts/{id}`                                   | DELETE | ADMIN                      |
| `/api/service-orders`, `/api/service-orders/{id}`   | GET    | ADMIN, MECHANIC, ATTENDANT |
| `/api/service-orders`                               | POST   | ADMIN, MECHANIC, ATTENDANT |
| `/api/service-orders/{id}/approval`                 | PATCH  | ADMIN, MECHANIC            |
| `/api/service-orders/{id}/status`                   | PATCH  | ADMIN, MECHANIC            |
| `/api/service-orders/{id}/diagnosis`                | PATCH  | ADMIN, MECHANIC            |
| `/api/reports/average-execution-time`               | GET    | ADMIN                      |

Rotas fora dessa lista exigem apenas autenticação (`anyRequest().authenticated()`). `GET /api/health` e
`GET /actuator/health` permanecem em `permitAll()` — exigir role para um probe de liveness/monitoramento quebraria a
convenção usada por orquestradores de infraestrutura.

Coberto por `AuthorizationIntegrationTest` (`src/test/java/br/com/oficina/mvp/shared/api`), que sobe o contexto
Spring real (Security incluído, sem mocks) e verifica 403 para perfis não permitidos e sucesso para os permitidos,
para cada formato de regra da matriz (todos os perfis, só ADMIN, ADMIN+MECHANIC e rota pública).

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
  -> notifica cliente por e-mail (ServiceOrderNotificationPort) sobre o status AGUARDANDO_APROVACAO
```

Observação: os preços de serviços e peças são copiados para `WorkOrderService` e `WorkOrderPart` no momento da criação.
Assim, alterações futuras no catálogo ou no preço da peça não mudam o orçamento já gerado.

### 7.2b Listagem de OS

```txt
GET /api/service-orders?all={boolean, default false}
  -> ServiceOrderController.list
  -> ServiceOrderService.list(all) (implementa ServiceOrderUseCase)
  -> se all=false: ServiceOrderRepositoryPort.findActiveOrderedByStatusPriority
       -> ServiceOrderJpaRepository.findActiveOrderedByStatusPriority (@Query JPQL com CASE por status)
  -> se all=true: ServiceOrderRepositoryPort.findAll (sem filtro nem ordenação especial)
```

Com `all=false` (padrão), filtra `status NOT IN (FINALIZADA, ENTREGUE, RECUSADA)` — exclusão apenas da listagem (não
física; essas OS continuam acessíveis via `GET /api/service-orders/{id}` ou via `all=true`) — e ordena por:

```txt
EM_EXECUCAO > AGUARDANDO_APROVACAO > EM_DIAGNOSTICO > RECEBIDA, created_at ASC dentro de cada status
```

`ServiceOrderRepositoryPort.findAll()` também é usado separadamente por `ReportService` para o relatório de tempo
médio de execução, que precisa enxergar OS finalizadas independente do parâmetro `all` da listagem.

### 7.3 Decisão administrativa sobre o orçamento da OS

```txt
PATCH /api/service-orders/{id}/approval
  -> busca OS
  -> se approved=true:
       -> valida estoque das peças
       -> decrementa estoque
       -> transiciona AGUARDANDO_APROVACAO -> EM_EXECUCAO
       -> preenche approvedAt e startedAt
       -> registra histórico
       -> notifica cliente por e-mail (ServiceOrderNotificationPort)
  -> se approved=false:
       -> transiciona AGUARDANDO_APROVACAO -> RECUSADA
       -> registra histórico
       -> não notifica (RECUSADA é a única transição que não dispara notificação)
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

Decisão pública sobre o orçamento:

```txt
POST /api/public/service-orders/{code}/approval
  -> PublicServiceOrderController.decideApproval
  -> ServiceOrderService.decideApprovalByCustomer
  -> normaliza document
  -> valida se o documento pertence ao cliente
  -> exige status AGUARDANDO_APROVACAO
  -> se approved=true: valida estoque, decrementa estoque, muda status para EM_EXECUCAO, notifica cliente
  -> se approved=false: muda status para RECUSADA (não notifica)
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
  -> se o novo status != RECUSADA: notifica cliente por e-mail (ServiceOrderNotificationPort)
```

Transições permitidas:

```txt
RECEBIDA -> EM_DIAGNOSTICO
RECEBIDA -> AGUARDANDO_APROVACAO
EM_DIAGNOSTICO -> AGUARDANDO_APROVACAO
AGUARDANDO_APROVACAO -> EM_EXECUCAO
AGUARDANDO_APROVACAO -> RECUSADA
EM_EXECUCAO -> FINALIZADA
FINALIZADA -> ENTREGUE
ENTREGUE -> sem próximas transições
RECUSADA -> sem próximas transições
```

Timestamps preenchidos no domínio:

| Status        | Campo preenchido                    |
|---------------|---------------------------------------|
| `EM_EXECUCAO` | `startedAt`, se ainda estiver vazio |
| `FINALIZADA`  | `finalizedAt`                       |
| `ENTREGUE`    | `deliveredAt`                       |

Na aprovação, `approvedAt` e `startedAt` são preenchidos juntos.

### 7.6 Preenchimento de diagnóstico

```txt
PATCH /api/service-orders/{id}/diagnosis
  -> ServiceOrderController.updateDiagnosis
  -> ServiceOrderService.updateDiagnosis (implementa ServiceOrderUseCase)
  -> busca OS
  -> ServiceOrder.updateDiagnosis
```

Não há restrição de status para preencher ou atualizar o diagnóstico — pode ser feito em qualquer etapa do fluxo,
assim como `customerNotes`. O campo é exposto em `ServiceOrderResponseDto` (fluxo administrativo); a visão pública
(`PublicServiceOrderResponseDto`) não o inclui.

### 7.7 Notificação de mudança de status

Toda vez que o status da OS muda — na criação (RECEBIDA -> AGUARDANDO_APROVACAO automático), na aprovação
(-> EM_EXECUCAO), e em qualquer transição feita via `PATCH /{id}/status` — o cliente é notificado, **exceto** quando
o novo status é `RECUSADA`.

```txt
ServiceOrderNotificationPort.notifyStatusChanged(order)
  -> serviceorder.application.port.out (porta de saída)
  -> implementada por ServiceOrderStatusNotificationAdapter (serviceorder.adapter.out.notification)
```

O canal definido é e-mail (`order.getCustomer().getEmail()`). Nesta primeira etapa do MVP, o adapter apenas loga a
notificação (sem envio real); como é uma porta de saída, trocar por um envio real de e-mail (SMTP, provedor
transacional etc.) é uma questão de implementar um novo adapter para `ServiceOrderNotificationPort`, sem alterar
domínio nem application. Se o cliente não tiver e-mail cadastrado (campo opcional em `Customer`), o adapter loga um
aviso e não falha a operação.

Chamada apenas nos pontos que efetivamente mudam o status (não em `updateDiagnosis`), e condicionada ao status
resultante ser diferente de `RECUSADA`:

| Origem                                          | Notifica?                                  |
|--------------------------------------------------|---------------------------------------------|
| `create` (RECEBIDA -> AGUARDANDO_APROVACAO)      | Sempre                                       |
| `decideApproval` / `decideApprovalByCustomer`, approved=true  | Sim (-> EM_EXECUCAO)           |
| `decideApproval` / `decideApprovalByCustomer`, approved=false | Não (-> RECUSADA)              |
| `updateStatus`                                    | Sim, exceto se o status alvo for `RECUSADA` |

### 7.7 Relatório de tempo médio

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
| `HttpMessageNotReadableException` | `400 Bad Request` (corpo da requisição malformado/ilegível) |
| `DataIntegrityViolationException` | `409 Conflict`                       |
| `Exception`                       | `500 Internal Server Error` (logado via `GlobalExceptionHandler`, nível `ERROR`) |

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
- healthcheck com MockMvc;
- serialização ponta a ponta com Spring/H2 reais (`LazyAssociationSerializationIntegrationTest`).

Os testes de serviço de aplicação mockam a porta de saída, então nunca tocam o Hibernate de verdade — não pegam
`LazyInitializationException` causada por acesso a associação `LAZY` fora da transação (`open-in-view: false`).
`LazyAssociationSerializationIntegrationTest` existe justamente para isso: sobe o contexto Spring completo com H2 e
bate nos endpoints REST via MockMvc, sem mocks, então reproduz o mesmo comportamento de sessão do Hibernate que a
aplicação tem em produção. Ao adicionar um módulo novo com relação `LAZY` exposta em DTO, adicione um caso ali (ou
numa classe irmã) para o mesmo padrão continuar coberto.

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
2. A segurança carrega role no JWT e no `SecurityContext`, e os endpoints usam autorização granular por perfil via
   `hasRole`/`hasAnyRole` em `SecurityConfig` — matriz completa na seção 5 ("Autorização por perfil"), coberta por
   `AuthorizationIntegrationTest`.
3. O módulo `report` não tem `domain` nem `adapter.out.persistence` próprios — ele lê diretamente pela porta de saída
   do módulo `serviceorder` (`ServiceOrderRepositoryPort`). É uma exceção deliberada ao esqueleto padrão dos módulos,
   já que `report` é puramente um caso de uso de leitura sobre dados de outro módulo.
4. `open-in-view` é `false` e o mapeamento DTO acontece no adapter web, fora da transação da camada `application`.
   Por isso, os adapters de persistência que retornam entidades com associações `LAZY` acessadas pelo DTO
   (`serviceorder`, `vehicle` — as únicas duas hoje, confirmado por auditoria de todos os `@ManyToOne`/`@OneToMany`
   do projeto) precisam inicializá-las explicitamente (`Hibernate.initialize`) antes de retornar — ver
   `ServiceOrderPersistenceAdapter` e `VehiclePersistenceAdapter`. Um módulo novo com relações `LAZY` expostas no
   response precisa do mesmo cuidado, senão a chamada falha com `LazyInitializationException` (HTTP 500). O teste
   `LazyAssociationSerializationIntegrationTest` (seção 10) existe para pegar essa regressão automaticamente.
5. Geração do código da OS: `ServiceOrderService.generateUniqueOrderCode()` checa `existsByCode` antes de montar a OS
   e regenera o código em caso de colisão, até 5 tentativas (`MAX_CODE_GENERATION_ATTEMPTS`); esgotadas as tentativas,
   lança `BusinessException` (`409 Conflict`). O retry acontece **antes** do `save()`, não depois de uma falha real
   de constraint — no Postgres, um erro de statement aborta a transação inteira até um rollback, então capturar
   `DataIntegrityViolationException` e tentar salvar de novo na mesma transação não funcionaria.
