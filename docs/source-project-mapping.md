# Mapeamento do projeto TypeScript/Node para Java/Spring

Este documento registra como a estrutura esperada de um backend TypeScript/Node foi representada neste projeto
Java/Spring Boot.

Atenção importante: no estado atual do código, o backend Java **não está separado fisicamente por módulos de domínio**.
A implementação está organizada por camadas técnicas:

```txt
controllers/  -> entrada HTTP
services/     -> casos de uso e orquestração
domains/      -> entidades JPA e regras de domínio
infra/        -> repositórios Spring Data JPA
dtos/         -> contratos de entrada/saída
shared/       -> segurança, config, validação e erros
```

Portanto, quando este documento citar `auth`, `customers`, `vehicles`, `catalog`, `parts`, `serviceorders` ou `reports`,
o nome representa o **contexto funcional**, não uma pasta física existente no projeto Java.

## 1. Mapeamento geral de arquitetura

| Projeto Java/Spring atual                                      |
|----------------------------------------------------------------|
| `OficinaMvpApplication.java`                                   |
| `controllers/*Controller.java`                                 |
| `services/*Service.java`                                       |
| Entidades JPA em `domains/` + Flyway `V1__init.sql`            |
| Interfaces `JpaRepository` em `infra/`                         |
| Records Java em `dtos/`                                        |
| Enums Java em `dtos/enums/`                                    |
| Jakarta Bean Validation nos DTOs + validadores de domínio      |
| `SecurityConfig` + `JwtAuthenticationFilter` + `JwtService`    |
| Bean `CorsConfigurationSource` em `SecurityConfig`             |
| `GlobalExceptionHandler`                                       |
| `BusinessException`                                            |
| `DataSeeder`                                                   |
| SpringDoc OpenAPI + `OpenApiConfig`                            |
| Flyway em `src/main/resources/db/migration`                    |
| JUnit 5, Mockito, MockMvc e H2                                 |
| JaCoCo                                                         |
| `.env`, `.env.example`, `application.yml` e variáveis `${...}` |
| Dockerfile multi-stage Maven + JRE                             |
| `docker-compose.yml` com `api` e `postgres`                    |

## 2. Mapeamento por contexto funcional

| Contexto funcional | Equivalente Java/Spring                                    | Principais arquivos                                                                                                                                                                                      |
|--------------------|------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Auth               | Login, JWT e segurança stateless                           | `AuthController`, `AuthService`, `User`, `UserRepository`, `LoginRequestDto`, `AuthResponseDto`, `SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`                                               |
| Customers          | Cadastro de clientes e validação de documento              | `CustomerController`, `CustomerService`, `Customer`, `CustomerRepository`, `CustomerRequestDto`, `CustomerResponseDto`, `DocumentValidator`                                                              |
| Vehicles           | Cadastro de veículos e validação de placa                  | `VehicleController`, `VehicleService`, `Vehicle`, `VehicleRepository`, `VehicleRequestDto`, `VehicleResponseDto`, `PlateValidator`                                                                       |
| Catalog/services   | Catálogo de serviços da oficina                            | `CatalogController`, `CatalogService`, `ServiceCatalogItem`, `ServiceCatalogItemRepository`, `ServiceCatalogItemRequestDto`, `ServiceCatalogItemResponseDto`                                             |
| Parts              | Peças/insumos e estoque                                    | `PartController`, `PartService`, `Part`, `PartRepository`, `PartRequestDto`, `PartResponseDto`                                                                                                           |
| Service Orders     | Ordem de serviço, orçamento, aprovação, status e histórico | `ServiceOrderController`, `PublicServiceOrderController`, `ServiceOrderApplicationService`, `ServiceOrder`, `WorkOrderService`, `WorkOrderPart`, `ServiceOrderStatusHistory`, `ServiceOrderStatusPolicy` |
| Reports            | Tempo médio de execução                                    | `ReportController`, `ReportService`, `AverageExecutionTimeResponseDto`                                                                                                                                   |
| Health             | Verificação simples da API                                 | `HealthController`                                                                                                                                                                                       |
| Shared errors      | Padrão de erro HTTP                                        | `GlobalExceptionHandler`, `ApiError`, `BusinessException`                                                                                                                                                |
| Shared config      | CORS, OpenAPI, seed e properties                           | `SecurityConfig`, `OpenApiConfig`, `DataSeeder`, `JwtProperties`, `AppCorsProperties`                                                                                                                    |

## 3. Mapeamento de rotas

| Rota                                                    | Controller Java                | Service Java                     | Autenticação |
|---------------------------------------------------------|--------------------------------|----------------------------------|--------------|
| `POST /api/auth/login`                                  | `AuthController`               | `AuthService`                    | Pública      |
| `GET /api/customers`                                    | `CustomerController`           | `CustomerService`                | JWT          |
| `POST /api/customers`                                   | `CustomerController`           | `CustomerService`                | JWT          |
| `GET /api/customers/{id}`                               | `CustomerController`           | `CustomerService`                | JWT          |
| `PUT /api/customers/{id}`                               | `CustomerController`           | `CustomerService`                | JWT          |
| `DELETE /api/customers/{id}`                            | `CustomerController`           | `CustomerService`                | JWT          |
| `GET /api/vehicles`                                     | `VehicleController`            | `VehicleService`                 | JWT          |
| `POST /api/vehicles`                                    | `VehicleController`            | `VehicleService`                 | JWT          |
| `GET /api/vehicles/{id}`                                | `VehicleController`            | `VehicleService`                 | JWT          |
| `PUT /api/vehicles/{id}`                                | `VehicleController`            | `VehicleService`                 | JWT          |
| `DELETE /api/vehicles/{id}`                             | `VehicleController`            | `VehicleService`                 | JWT          |
| `GET /api/services`                                     | `CatalogController`            | `CatalogService`                 | JWT          |
| `POST /api/services`                                    | `CatalogController`            | `CatalogService`                 | JWT          |
| `GET /api/services/{id}`                                | `CatalogController`            | `CatalogService`                 | JWT          |
| `PUT /api/services/{id}`                                | `CatalogController`            | `CatalogService`                 | JWT          |
| `DELETE /api/services/{id}`                             | `CatalogController`            | `CatalogService`                 | JWT          |
| `GET /api/parts`                                        | `PartController`               | `PartService`                    | JWT          |
| `POST /api/parts`                                       | `PartController`               | `PartService`                    | JWT          |
| `GET /api/parts/{id}`                                   | `PartController`               | `PartService`                    | JWT          |
| `PUT /api/parts/{id}`                                   | `PartController`               | `PartService`                    | JWT          |
| `DELETE /api/parts/{id}`                                | `PartController`               | `PartService`                    | JWT          |
| `GET /api/service-orders`                               | `ServiceOrderController`       | `ServiceOrderApplicationService` | JWT          |
| `POST /api/service-orders`                              | `ServiceOrderController`       | `ServiceOrderApplicationService` | JWT          |
| `GET /api/service-orders/{id}`                          | `ServiceOrderController`       | `ServiceOrderApplicationService` | JWT          |
| `PATCH /api/service-orders/{id}/approve`                | `ServiceOrderController`       | `ServiceOrderApplicationService` | JWT          |
| `PATCH /api/service-orders/{id}/status`                 | `ServiceOrderController`       | `ServiceOrderApplicationService` | JWT          |
| `GET /api/public/service-orders/{code}?document=...`    | `PublicServiceOrderController` | `ServiceOrderApplicationService` | Pública      |
| `POST /api/public/service-orders/{code}/approve`        | `PublicServiceOrderController` | `ServiceOrderApplicationService` | Pública      |
| `GET /api/reports/average-execution-time`               | `ReportController`             | `ReportService`                  | JWT          |
| `GET /api/health`                                       | `HealthController`             | —                                | Pública      |
| `GET /actuator/health`                                  | Actuator                       | —                                | Pública      |
| `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**` | SpringDoc                      | —                                | Pública      |

## 4. Mapeamento de banco de dados

| Entidade Java               | Tabela SQL/Flyway              | Repository                     |
|-----------------------------|--------------------------------|--------------------------------|
| `User`                      | `users`                        | `UserRepository`               |
| `Customer`                  | `customers`                    | `CustomerRepository`           |
| `Vehicle`                   | `vehicles`                     | `VehicleRepository`            |
| `ServiceCatalogItem`        | `service_catalog_items`        | `ServiceCatalogItemRepository` |
| `Part`                      | `parts`                        | `PartRepository`               |
| `ServiceOrder`              | `service_orders`               | `ServiceOrderRepository`       |
| `WorkOrderService`          | `work_order_services`          | via cascade de `ServiceOrder`  |
| `WorkOrderPart`             | `work_order_parts`             | via cascade de `ServiceOrder`  |
| `ServiceOrderStatusHistory` | `service_order_status_history` | via cascade de `ServiceOrder`  |
| `flyway_schema_history`     | gerenciado pelo Flyway         |

## 5. Mapeamento de validações

| Implementação Java atual                       |
|------------------------------------------------|
| `@Email`, `@NotBlank`                          |
| `@Size(min = ...)`                             |
| `@Positive`                                    |
| `@Min`                                         |
| `@DecimalMin("0.00")`                          |
| `@NotNull` + `@Valid`                          |
| `@NotEmpty` + `@Valid`                         |
| `DocumentValidator.isValidCpfOrCnpj`           |
| `DocumentValidator.normalize`                  |
| `PlateValidator.isValidBrazilianPlate`         |
| `PlateValidator.normalize`                     |
| `ServiceOrderStatusPolicy.assertTransition`    |
| `ServiceOrderApplicationService.validateStock` |

## 6. Mapeamento dos fluxos principais

### 6.1 Login

| Etapa                    | Implementação Java           |
|--------------------------|------------------------------|
| Route `POST /auth/login` | `AuthController.login`       |
| Buscar usuário por email | `UserRepository.findByEmail` |
| Comparar senha com hash  | `PasswordEncoder.matches`    |
| Gerar JWT                | `JwtService.generate`        |
| Retornar usuário + token | `AuthResponseDto`            |

### 6.2 Criação de OS

| Etapa                  | Implementação Java                                 |
|------------------------|----------------------------------------------------|
| Receber payload da OS  | `CreateServiceOrderRequestDto`                     |
| Validar documento      | `DocumentValidator`                                |
| Validar placa          | `PlateValidator`                                   |
| Buscar/criar cliente   | `CustomerRepository.findByDocument` + `Customer`   |
| Buscar/criar veículo   | `VehicleRepository.findByPlate` + `Vehicle`        |
| Buscar serviços ativos | `ServiceCatalogItemRepository.findById` + `active` |
| Buscar peças ativas    | `PartRepository.findById` + `active`               |
| Montar itens da OS     | `WorkOrderService`, `WorkOrderPart`                |
| Calcular orçamento     | `ServiceOrder.recalculateTotals`                   |
| Enviar para aprovação  | `ServiceOrder.markBudgetWaitingApproval`           |
| Persistir agregado     | `ServiceOrderRepository.save` com cascade          |

### 6.3 Aprovação e estoque

| Etapa                     | Implementação Java                                              |
|---------------------------|-----------------------------------------------------------------|
| Aprovar orçamento         | `ServiceOrderApplicationService.approve` ou `approveByCustomer` |
| Verificar disponibilidade | `validateStock`                                                 |
| Baixar estoque            | `decrementStock` + `Part.decrementStock`                        |
| Mudar status              | `ServiceOrder.approve`                                          |
| Registrar histórico       | `ServiceOrderStatusHistory`                                     |

### 6.4 Relatório

| Etapa                            | Implementação Java                         |
|----------------------------------|--------------------------------------------|
| Buscar OS finalizadas/executadas | `ServiceOrderRepository.findAll`           |
| Filtrar com timestamps completos | `startedAt != null && finalizedAt != null` |
| Calcular média                   | `Duration.between(...).toMinutes()`        |
| Retornar DTO                     | `AverageExecutionTimeResponseDto`          |

## 7. Mapeamento dos arquivos de configuração

| Arquivo Java/Spring atual                             | Conteúdo                                           |
|-------------------------------------------------------|----------------------------------------------------|
| `.env.example`                                        | Variáveis locais de porta, banco, JWT, CORS e seed |
| `application.yml`                                     | Configuração Spring com placeholders `${...}`      |
| `docker-compose.yml`                                  | Serviços `postgres` e `api`                        |
| `Dockerfile`                                          | Build multi-stage Maven + JRE                      |
| `pom.xml`                                             | Dependências, plugins, versão Java e build         |
| `pom.xml` + `src/test/resources/application-test.yml` | Testes, H2 e JaCoCo                                |

## 8. Mapeamento de testes

| Área               | Testes Java existentes                                                               |
|--------------------|--------------------------------------------------------------------------------------|
| Domínio de peças   | `PartTest`                                                                           |
| Domínio de OS      | `ServiceOrderTest`                                                                   |
| Política de status | `ServiceOrderStatusPolicyTest`                                                       |
| Autenticação       | `AuthServiceTest`, `JwtServiceTest`                                                  |
| Serviços CRUD      | `CatalogServiceTest`, `CustomerServiceTest`, `PartServiceTest`, `VehicleServiceTest` |
| Fluxo de OS        | `ServiceOrderApplicationServiceTest`                                                 |
| Relatórios         | `ReportServiceTest`                                                                  |
| API/health         | `HealthControllerIntegrationTest`                                                    |
| Validadores        | `DocumentValidatorTest`, `PlateValidatorTest`                                        |

O build Maven configura JaCoCo para executar relatório e checagem na fase `verify`.

## 9. Pontos importantes

1. Os DTOs Java `record` cumprem o papel de interfaces/types de request e response.
2. O Bean Validation cobre validações sintáticas; regras de negócio ficam em services, entidades e validadores
   customizados.
3. O banco é controlado por Flyway e não por sincronização automática do ORM, porque o Hibernate usa
   `ddl-auto: validate`.
4. O JWT é aplicado como filtro Spring Security antes do `UsernamePasswordAuthenticationFilter`.
5. O domínio de OS funciona como o núcleo do MVP, concentrando orçamento, status, histórico e estoque.
6. O MER em `docs/MER.drawio` corresponde às tabelas declaradas em `V1__init.sql`, incluindo `flyway_schema_history`
   como tabela operacional do Flyway.

## 10. Pontos de atenção para manutenção

- Não criar documentação ou novos imports assumindo pacotes físicos como `br.com.oficina.mvp.auth` ou
  `br.com.oficina.mvp.serviceorders`; eles não existem hoje.
- Ao evoluir o projeto, decidir se a organização continuará por camada técnica ou se será migrada para pacotes por
  domínio.
- Alinhar o enum `Role.DEFAULT` com o check constraint do banco, que atualmente usa `ATTENDANT`.
- Caso futuras rotas dependam de perfil, aproveitar as authorities `ROLE_ADMIN`, `ROLE_DEFAULT`/`ROLE_ATTENDANT` e
  `ROLE_MECHANIC` carregadas pelo JWT.
- Se o fluxo de diagnóstico ganhar endpoint próprio, reutilizar o campo `diagnosis` já existente em `service_orders` e
  em `ServiceOrder`.
