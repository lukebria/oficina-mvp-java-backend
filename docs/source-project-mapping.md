# Mapeamento do projeto TypeScript para Java/Spring

| Projeto TypeScript/Node | Projeto Java/Spring |
|---|---|
| `src/modules/auth` | `auth` |
| `src/modules/customers` | `customers` |
| `src/modules/vehicles` | `vehicles` |
| `src/modules/services` | `catalog` |
| `src/modules/parts` | `parts` |
| `src/modules/serviceOrders` | `serviceorders` |
| `src/modules/reports` | `serviceorders/application/ReportService` + `serviceorders/api/ReportController` |
| Prisma schema | JPA entities + Flyway `V1__init.sql` |
| Zod validation | Bean Validation (`jakarta.validation`) + validadores de domínio |
| Express middleware JWT | Spring Security + `JwtAuthenticationFilter` |
| Swagger manual | SpringDoc OpenAPI |
| Vitest/Supertest | JUnit 5 + MockMvc + JaCoCo |

## Rotas mantidas

- `/api/auth/login`
- `/api/customers`
- `/api/vehicles`
- `/api/services`
- `/api/parts`
- `/api/service-orders`
- `/api/public/service-orders/{code}`
- `/api/public/service-orders/{code}/approve`
- `/api/reports/average-execution-time`
