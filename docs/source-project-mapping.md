# Mapeamento do projeto TypeScript/Node para Java/Spring

Este documento registra como a estrutura esperada de um backend TypeScript/Node foi representada neste projeto
Java/Spring Boot.

O backend Java está organizado como um **monolito modular por módulos de negócio**, seguindo **Arquitetura Hexagonal
(Ports & Adapters)**. Cada contexto funcional citado neste documento (`auth`, `customer`, `vehicle`, `catalog`,
`part`, `serviceorder`, `report`) **é, de fato, um pacote físico** em `src/main/java/br/com/oficina/mvp/<contexto>/`,
com o mesmo esqueleto interno em todos:

```txt
<contexto>/
  domain/                     entidade JPA e regras de domínio
  application/                caso de uso (implementa a porta de entrada)
  application/port/in/        porta de entrada (UseCase + Command/Result)
  application/port/out/       porta de saída (o que o módulo precisa de fora)
  adapter/in/web/              controller REST + DTOs de request/response
  adapter/out/persistence/     JpaRepository (package-private) + PersistenceAdapter
```

Detalhamento completo por módulo está em [`architecture.md`](architecture.md); este documento foca no mapeamento
conceito-a-conceito com um backend TS/Node equivalente.

## 1. Mapeamento geral de arquitetura

| Conceito TS/Node                          | Equivalente Java/Spring                                                     |
|--------------------------------------------|-------------------------------------------------------------------------------|
| Entry point (`main.ts`/`app.ts`)           | `OficinaMvpApplication.java`                                                  |
| Controller HTTP                            | `<contexto>/adapter/in/web/*Controller.java`                                  |
| Caso de uso / interactor                   | `<contexto>/application/*Service.java`, implementa `*UseCase` de `port/in`    |
| Interface de caso de uso (porta de entrada)| `<contexto>/application/port/in/*UseCase.java`                                |
| Command/DTO de entrada da aplicação        | `<contexto>/application/port/in/*Command.java`                                |
| Interface de repositório (porta de saída)  | `<contexto>/application/port/out/*RepositoryPort.java`                        |
| Implementação do repositório (ORM)         | `<contexto>/adapter/out/persistence/*JpaRepository.java` + `*PersistenceAdapter.java` (ambos package-private) |
| Entidade de domínio + mapeamento ORM       | `<contexto>/domain/*.java` (anotado com JPA `@Entity`) + Flyway `V1__init.sql`|
| DTO de request/response HTTP               | Records Java em `<contexto>/adapter/in/web/*RequestDto.java` / `*ResponseDto.java` |
| Enum compartilhado                         | `shared/domain/*.java` (`Role`, `ServiceOrderStatus`)                         |
| Validação de schema HTTP                   | Jakarta Bean Validation nos DTOs                                              |
| Validação de regra de negócio              | Validadores customizados (`shared/validation`) + regras nas entidades/serviços|
| Middleware de autenticação                 | `SecurityConfig` + `JwtAuthenticationFilter` + `JwtService` (`shared/security`)|
| Middleware de CORS                         | Bean `CorsConfigurationSource` em `SecurityConfig`                            |
| Error handler global                       | `GlobalExceptionHandler` (`shared/exception`)                                 |
| Exceção de negócio customizada             | `BusinessException` (`shared/exception`)                                      |
| Seed script                                | `DataSeeder` (`shared/config`)                                                |
| Geração de documentação da API             | SpringDoc OpenAPI + `OpenApiConfig` (`shared/config`)                         |
| Migrations                                 | Flyway em `src/main/resources/db/migration`                                   |
| Testes                                     | JUnit 5, Mockito, MockMvc e H2, espelhando o pacote da classe testada          |
| Cobertura de testes                        | JaCoCo                                                                        |
| Variáveis de ambiente                      | `.env`, `.env.example`, `application.yml` e placeholders `${...}`             |
| Build de produção                          | Dockerfile multi-stage Maven + JRE                                            |
| Orquestração local                         | `docker-compose.yml` com `api` e `postgres`                                   |

## 2. Mapeamento por módulo

| Módulo (pacote físico) | Responsabilidade                                            | Principais classes                                                                                                                                            |
|--------------------------|---------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `auth`                    | Login, JWT e segurança stateless                              | `AuthController`, `AuthUseCase`, `LoginCommand`, `AuthResult`, `AuthService`, `UserRepositoryPort`, `User`, `LoginRequestDto`, `AuthResponseDto`             |
| `customer`                | Cadastro de clientes e validação de documento                 | `CustomerController`, `CustomerUseCase`, `CustomerCommand`, `CustomerService`, `CustomerRepositoryPort`, `Customer`, `CustomerRequestDto`, `CustomerResponseDto` |
| `vehicle`                 | Cadastro de veículos e validação de placa                     | `VehicleController`, `VehicleUseCase`, `VehicleCommand`, `VehicleService`, `VehicleRepositoryPort`, `Vehicle`, `VehicleRequestDto`, `VehicleResponseDto`     |
| `catalog`                 | Catálogo de serviços da oficina                                | `CatalogController`, `CatalogUseCase`, `CatalogCommand`, `CatalogService`, `CatalogRepositoryPort`, `ServiceCatalogItem`, `ServiceCatalogItemRequestDto`, `ServiceCatalogItemResponseDto` |
| `part`                    | Peças/insumos e estoque                                       | `PartController`, `PartUseCase`, `PartCommand`, `PartService`, `PartRepositoryPort`, `Part`, `PartRequestDto`, `PartResponseDto`                             |
| `serviceorder`            | Ordem de serviço, orçamento, aprovação, status e histórico     | `ServiceOrderController`, `PublicServiceOrderController`, `ServiceOrderUseCase`, `PublicServiceOrderUseCase`, `CreateServiceOrderCommand`, `ServiceOrderService`, `ServiceOrderRepositoryPort`, `ServiceOrder`, `WorkOrderService`, `WorkOrderPart`, `WorkOrderLineItem`, `ServiceOrderStatusHistory`, `ServiceOrderStatusPolicy` |
| `report`                  | Tempo médio de execução (só leitura, sem `domain` próprio)     | `ReportController`, `ReportUseCase`, `AverageExecutionTimeResult`, `ReportService` (lê via `ServiceOrderRepositoryPort` do módulo `serviceorder`), `AverageExecutionTimeResponseDto` |
| `shared`                  | Vocabulário e infraestrutura transversal                       | `shared.domain.BaseEntity`, `shared.domain.Role`, `shared.domain.ServiceOrderStatus`, `SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`, `GlobalExceptionHandler`, `ApiError`, `BusinessException`, `DocumentValidator`, `PlateValidator`, `DataSeeder`, `OpenApiConfig`, `shared.api.HealthController` |

## 3. Mapeamento de rotas

| Rota                                                    | Controller Java                                            | Application service Java                              | Autenticação |
|-----------------------------------------------------------|--------------------------------------------------------------|-----------------------------------------------------------|--------------|
| `POST /api/auth/login`                                  | `auth.adapter.in.web.AuthController`                        | `auth.application.AuthService`                            | Pública      |
| `GET /api/customers`                                    | `customer.adapter.in.web.CustomerController`                 | `customer.application.CustomerService`                    | JWT          |
| `POST /api/customers`                                   | `customer.adapter.in.web.CustomerController`                 | `customer.application.CustomerService`                    | JWT          |
| `GET /api/customers/{id}`                                | `customer.adapter.in.web.CustomerController`                 | `customer.application.CustomerService`                    | JWT          |
| `PUT /api/customers/{id}`                                | `customer.adapter.in.web.CustomerController`                 | `customer.application.CustomerService`                    | JWT          |
| `DELETE /api/customers/{id}`                             | `customer.adapter.in.web.CustomerController`                 | `customer.application.CustomerService`                    | JWT          |
| `GET /api/vehicles`                                      | `vehicle.adapter.in.web.VehicleController`                   | `vehicle.application.VehicleService`                       | JWT          |
| `POST /api/vehicles`                                     | `vehicle.adapter.in.web.VehicleController`                   | `vehicle.application.VehicleService`                       | JWT          |
| `GET /api/vehicles/{id}`                                 | `vehicle.adapter.in.web.VehicleController`                   | `vehicle.application.VehicleService`                       | JWT          |
| `PUT /api/vehicles/{id}`                                 | `vehicle.adapter.in.web.VehicleController`                   | `vehicle.application.VehicleService`                       | JWT          |
| `DELETE /api/vehicles/{id}`                              | `vehicle.adapter.in.web.VehicleController`                   | `vehicle.application.VehicleService`                       | JWT          |
| `GET /api/services`                                      | `catalog.adapter.in.web.CatalogController`                   | `catalog.application.CatalogService`                        | JWT          |
| `POST /api/services`                                     | `catalog.adapter.in.web.CatalogController`                   | `catalog.application.CatalogService`                        | JWT          |
| `GET /api/services/{id}`                                 | `catalog.adapter.in.web.CatalogController`                   | `catalog.application.CatalogService`                        | JWT          |
| `PUT /api/services/{id}`                                 | `catalog.adapter.in.web.CatalogController`                   | `catalog.application.CatalogService`                        | JWT          |
| `DELETE /api/services/{id}`                              | `catalog.adapter.in.web.CatalogController`                   | `catalog.application.CatalogService`                        | JWT          |
| `GET /api/parts`                                         | `part.adapter.in.web.PartController`                         | `part.application.PartService`                              | JWT          |
| `POST /api/parts`                                        | `part.adapter.in.web.PartController`                         | `part.application.PartService`                              | JWT          |
| `GET /api/parts/{id}`                                    | `part.adapter.in.web.PartController`                         | `part.application.PartService`                              | JWT          |
| `PUT /api/parts/{id}`                                    | `part.adapter.in.web.PartController`                         | `part.application.PartService`                              | JWT          |
| `DELETE /api/parts/{id}`                                 | `part.adapter.in.web.PartController`                         | `part.application.PartService`                              | JWT          |
| `GET /api/service-orders`                                | `serviceorder.adapter.in.web.ServiceOrderController`          | `serviceorder.application.ServiceOrderService`               | JWT          |
| `POST /api/service-orders`                               | `serviceorder.adapter.in.web.ServiceOrderController`          | `serviceorder.application.ServiceOrderService`               | JWT          |
| `GET /api/service-orders/{id}`                           | `serviceorder.adapter.in.web.ServiceOrderController`          | `serviceorder.application.ServiceOrderService`               | JWT          |
| `PATCH /api/service-orders/{id}/approval`                | `serviceorder.adapter.in.web.ServiceOrderController`          | `serviceorder.application.ServiceOrderService`               | JWT          |
| `PATCH /api/service-orders/{id}/status`                  | `serviceorder.adapter.in.web.ServiceOrderController`          | `serviceorder.application.ServiceOrderService`               | JWT          |
| `PATCH /api/service-orders/{id}/diagnosis`               | `serviceorder.adapter.in.web.ServiceOrderController`          | `serviceorder.application.ServiceOrderService`               | JWT          |
| `GET /api/public/service-orders/{code}?document=...`     | `serviceorder.adapter.in.web.PublicServiceOrderController`    | `serviceorder.application.ServiceOrderService`               | Pública      |
| `POST /api/public/service-orders/{code}/approval`        | `serviceorder.adapter.in.web.PublicServiceOrderController`    | `serviceorder.application.ServiceOrderService`               | Pública      |
| `GET /api/reports/average-execution-time`                | `report.adapter.in.web.ReportController`                      | `report.application.ReportService`                           | JWT          |
| `GET /api/health`                                        | `shared.api.HealthController`                                 | —                                                             | Pública      |
| `GET /actuator/health`                                   | Actuator                                                       | —                                                             | Pública      |
| `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`  | SpringDoc                                                       | —                                                             | Pública      |

`ServiceOrderService` implementa duas portas de entrada — `ServiceOrderUseCase` (fluxo administrativo) e
`PublicServiceOrderUseCase` (fluxo público) — refletindo que são dois atores/perfis de segurança distintos, mesmo
sendo uma única classe de aplicação por trás dos dois controllers.

## 4. Mapeamento de banco de dados

| Entidade Java (`<módulo>/domain`)  | Tabela SQL/Flyway                | Porta de saída (`application/port/out`) | Adapter (`adapter/out/persistence`)     |
|--------------------------------------|-------------------------------------|--------------------------------------------|---------------------------------------------|
| `auth.domain.User`                    | `users`                             | `UserRepositoryPort`                        | `UserJpaRepository` + `UserPersistenceAdapter` |
| `customer.domain.Customer`            | `customers`                         | `CustomerRepositoryPort`                    | `CustomerJpaRepository` + `CustomerPersistenceAdapter` |
| `vehicle.domain.Vehicle`              | `vehicles`                          | `VehicleRepositoryPort`                     | `VehicleJpaRepository` + `VehiclePersistenceAdapter` |
| `catalog.domain.ServiceCatalogItem`   | `service_catalog_items`             | `CatalogRepositoryPort`                     | `ServiceCatalogItemJpaRepository` + `CatalogPersistenceAdapter` |
| `part.domain.Part`                    | `parts`                             | `PartRepositoryPort`                        | `PartJpaRepository` + `PartPersistenceAdapter` |
| `serviceorder.domain.ServiceOrder`    | `service_orders`                    | `ServiceOrderRepositoryPort`                | `ServiceOrderJpaRepository` + `ServiceOrderPersistenceAdapter` |
| `serviceorder.domain.WorkOrderService`| `work_order_services`               | via cascade de `ServiceOrder`               | —                                             |
| `serviceorder.domain.WorkOrderPart`   | `work_order_parts`                  | via cascade de `ServiceOrder`               | —                                             |
| `serviceorder.domain.ServiceOrderStatusHistory` | `service_order_status_history` | via cascade de `ServiceOrder`         | —                                             |
| —                                     | `flyway_schema_history`             | gerenciado pelo Flyway                      | —                                             |

Os repositórios Spring Data (`*JpaRepository`) e os adapters (`*PersistenceAdapter`) são **package-private** —
visíveis só dentro do próprio pacote `adapter/out/persistence`. Nenhum outro módulo enxerga a interface `JpaRepository`
diretamente; tudo passa pela porta (`*RepositoryPort`).

## 5. Mapeamento de validações

| Implementação Java atual                              | Onde                                                   |
|-----------------------------------------------------------|-----------------------------------------------------------|
| `@Email`, `@NotBlank`                                    | DTOs em `<módulo>/adapter/in/web`                         |
| `@Size(min = ...)`                                       | DTOs em `<módulo>/adapter/in/web`                         |
| `@Positive`                                               | DTOs em `<módulo>/adapter/in/web`                         |
| `@Min`                                                    | DTOs em `<módulo>/adapter/in/web`                         |
| `@DecimalMin("0.00")`                                    | DTOs em `<módulo>/adapter/in/web`                         |
| `@NotNull` + `@Valid`                                    | DTOs em `<módulo>/adapter/in/web`                         |
| `@NotEmpty` + `@Valid`                                   | DTOs em `<módulo>/adapter/in/web`                         |
| `DocumentValidator.requireValid` / `.isValidCpfOrCnpj`   | `shared/validation` — centraliza normalize + validate + throw |
| `PlateValidator.requireValid` / `.isValidBrazilianPlate` | `shared/validation` — centraliza normalize + validate + throw |
| `ServiceOrderStatusPolicy.assertTransition`              | `serviceorder/domain`                                     |
| `ServiceOrderService.validateStock`                      | `serviceorder/application` (privado)                       |
| `Part.decrementStock`                                    | `part/domain` — valida estoque insuficiente antes de baixar |

## 6. Mapeamento dos fluxos principais

### 6.1 Login

| Etapa                    | Implementação Java                                   |
|--------------------------|--------------------------------------------------------|
| Route `POST /auth/login` | `auth.adapter.in.web.AuthController.login`             |
| Converter DTO em comando | `LoginRequestDto.toCommand()` → `LoginCommand`         |
| Buscar usuário por email | `UserRepositoryPort.findByEmail` (via `AuthService`)   |
| Comparar senha com hash  | `PasswordEncoder.matches`                              |
| Gerar JWT                | `JwtService.generate` (`shared.security`)              |
| Retornar usuário + token | `AuthResult` (porta) → `AuthResponseDto.from` (adapter) |

### 6.2 Criação de OS

| Etapa                  | Implementação Java                                                          |
|-------------------------|--------------------------------------------------------------------------------|
| Receber payload da OS   | `CreateServiceOrderRequestDto` → `.toCommand()` → `CreateServiceOrderCommand` |
| Validar documento       | `DocumentValidator.requireValid`                                              |
| Validar placa           | `PlateValidator.requireValid`                                                 |
| Buscar/criar cliente    | `CustomerRepositoryPort.findByDocument` + `Customer` (porta do módulo `customer`) |
| Buscar/criar veículo    | `VehicleRepositoryPort.findByPlate` + `Vehicle` (porta do módulo `vehicle`)   |
| Buscar serviços ativos  | `CatalogRepositoryPort.findById` + `active` (porta do módulo `catalog`)       |
| Buscar peças ativas     | `PartRepositoryPort.findById` + `active` (porta do módulo `part`)             |
| Montar itens da OS      | `WorkOrderService`, `WorkOrderPart` (`serviceorder/domain`)                   |
| Calcular orçamento      | `ServiceOrder.recalculateTotals`                                              |
| Enviar para aprovação   | `ServiceOrder.markBudgetWaitingApproval`                                      |
| Persistir agregado      | `ServiceOrderRepositoryPort.save` com cascade                                 |

### 6.3 Aprovação e estoque

| Etapa                     | Implementação Java                                                    |
|----------------------------|---------------------------------------------------------------------------|
| Decidir aprovação/recusa   | `ServiceOrderService.decideApproval` (admin) ou `.decideApprovalByCustomer` (público) |
| Verificar disponibilidade | `ServiceOrderService.validateStock`                                      |
| Baixar estoque             | `ServiceOrderService.decrementStock` + `Part.decrementStock`             |
| Mudar status               | `ServiceOrder.decideApproval`                                             |
| Registrar histórico        | `ServiceOrderStatusHistory`                                               |

### 6.4 Relatório

| Etapa                            | Implementação Java                                              |
|------------------------------------|---------------------------------------------------------------------|
| Buscar OS finalizadas/executadas  | `ServiceOrderRepositoryPort.findAll` (porta do módulo `serviceorder`, consumida pelo módulo `report`) |
| Filtrar com timestamps completos  | `startedAt != null && finalizedAt != null`                          |
| Calcular média                     | `Duration.between(...).toMinutes()`                                 |
| Retornar resultado                 | `AverageExecutionTimeResult` (porta) → `AverageExecutionTimeResponseDto.from` (adapter) |

## 7. Mapeamento dos arquivos de configuração

| Arquivo Java/Spring atual                             | Conteúdo                                           |
|-------------------------------------------------------|------------------------------------------------------|
| `.env.example`                                        | Variáveis locais de porta, banco, JWT, CORS e seed   |
| `application.yml`                                     | Configuração Spring com placeholders `${...}`        |
| `docker-compose.yml`                                  | Serviços `postgres` e `api`                          |
| `Dockerfile`                                           | Build multi-stage Maven + JRE                        |
| `pom.xml`                                              | Dependências, plugins, versão Java e build            |
| `pom.xml` + `src/test/resources/application-test.yml` | Testes, H2 e JaCoCo                                   |

## 8. Mapeamento de testes

Os testes espelham o pacote da classe testada (ex: `serviceorder.application.ServiceOrderServiceTest` testa
`serviceorder.application.ServiceOrderService`).

| Área                | Testes Java existentes                                                                                                    |
|-----------------------|-------------------------------------------------------------------------------------------------------------------------|
| Domínio de peças     | `part.domain.PartTest`                                                                                                    |
| Domínio de OS         | `serviceorder.domain.ServiceOrderTest`                                                                                     |
| Política de status    | `serviceorder.domain.ServiceOrderStatusPolicyTest`                                                                         |
| Autenticação           | `auth.application.AuthServiceTest`, `shared.security.JwtServiceTest`                                                       |
| Serviços CRUD          | `catalog.application.CatalogServiceTest`, `customer.application.CustomerServiceTest`, `part.application.PartServiceTest`, `vehicle.application.VehicleServiceTest` |
| Fluxo de OS            | `serviceorder.application.ServiceOrderServiceTest`                                                                          |
| Relatórios             | `report.application.ReportServiceTest`                                                                                     |
| API/health             | `shared.api.HealthControllerIntegrationTest`                                                                               |
| Validadores            | `shared.validation.DocumentValidatorTest`, `shared.validation.PlateValidatorTest`                                          |

O build Maven configura JaCoCo para executar relatório e checagem na fase `verify`.

## 9. Pontos importantes

1. Os DTOs Java `record` cumprem o papel de interfaces/types de request e response, e ficam em
   `<módulo>/adapter/in/web`, junto do controller que os usa.
2. O Bean Validation cobre validações sintáticas nos DTOs de entrada; regras de negócio ficam na camada `application`
   (casos de uso), nas entidades de `domain` e nos validadores customizados de `shared/validation`.
3. O banco é controlado por Flyway e não por sincronização automática do ORM, porque o Hibernate usa
   `ddl-auto: validate`.
4. O JWT é aplicado como filtro Spring Security antes do `UsernamePasswordAuthenticationFilter`; o filtro depende da
   porta `UserRepositoryPort` do módulo `auth`, não de uma implementação concreta.
5. O domínio de OS (`serviceorder`) funciona como o núcleo do MVP, concentrando orçamento, status, histórico e
   estoque — é o único módulo que depende das portas de saída de quatro outros módulos (`customer`, `vehicle`,
   `catalog`, `part`) para se completar.
6. O módulo `report` é uma exceção deliberada ao esqueleto padrão: não tem `domain` nem `adapter/out/persistence`
   próprios, porque é puramente um caso de uso de leitura sobre dados que pertencem ao módulo `serviceorder`.
7. O MER em `docs/MER.drawio` corresponde às tabelas declaradas em `V1__init.sql`, incluindo `flyway_schema_history`
   como tabela operacional do Flyway.
