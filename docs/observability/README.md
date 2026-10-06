# Observabilidade — New Relic (Fase 3, plano 05)

Dashboards e alertas exigidos pelo enunciado, criados na conta New Relic do projeto (conta `8558465`, região
US) via API NerdGraph em 2026-10-04. Os arquivos desta pasta são a **definição versionada** — a conta New Relic
não é destruída junto com o ambiente AWS, então dashboards e alertas continuam existindo entre um deploy e outro.

| Arquivo | Conteúdo |
|---|---|
| `dashboard-oficina-mvp.json` | Dashboard "Oficina MVP - Tech Challenge Fase 3" (2 páginas, 14 widgets) |
| `alertas-oficina-mvp.json` | Policy "Oficina MVP - Alertas" (3 condições NRQL) + notificação por e-mail |

## Dashboard

**Página "Negócio"** (os 3 dashboards pedidos no enunciado):
- **Volume diário de ordens de serviço** — `service_order.created` (`TIMESERIES 1 day`) + total do dia.
- **Tempo médio de execução por status** (Diagnóstico, Execução, Finalização) — `service_order.status.duration`
  com `FACET status` (ms; timer do Micrometer exportado via OTLP).
- **Erros e falhas nas integrações** — `integration.failure` por tipo + erros 5xx por endpoint (spans).

**Página "Operação"**: latência p50/p95 e por endpoint, throughput, CPU e memória por pod
(`K8sContainerSample`), pods disponíveis por deployment (healthcheck/uptime), réplicas do HPA e logs JSON da
aplicação com `trace.id` (correlação entre requisições).

As métricas de negócio são emitidas por `shared/observability/BusinessMetrics` e chegam ao New Relic via OTLP;
CPU/memória/pods vêm do `nri-bundle` instalado pelo `oficina-mvp-infra-iac` (`modules/newrelic`).

## Alertas

| Condição | NRQL (resumo) | Dispara quando |
|---|---|---|
| Falha no processamento de ordens de serviço | `Span` com `outcome = 'SERVER_ERROR'` em `%service-orders%` | ≥ 1 erro em 5 min |
| Falha em integração externa | `sum(integration.failure)` | ≥ 1 falha em 5 min |
| Aplicação indisponível (healthcheck/uptime) | `latest(podsAvailable)` do `oficina-app-deployment` | < 1 por 5 min |

Notificação por e-mail (`techpos2026@gmail.com`) na abertura e no fechamento do incidente. Sem dados (cluster
desligado) a condição de uptime **não** alerta — de propósito, o ambiente do lab fica desligado entre gravações.

## Recriar numa conta nova

- **Dashboard**: New Relic → *Dashboards* → *Import dashboard* → colar `dashboard-oficina-mvp.json`
  (trocar `8558465` pelo Account ID da conta nova).
- **Alertas**: *Alerts* → *Alert policies* → criar a policy e uma *NRQL condition* por item de
  `alertas-oficina-mvp.json` (mesmos NRQL/thresholds); depois *Workflows* → e-mail filtrando pela policy.

## Como a aplicação envia traces/métricas (Spring Boot 4)

No Spring Boot 4 a auto-configuração de tracing/OpenTelemetry fica em módulos próprios: o `pom.xml` usa o
**`spring-boot-starter-opentelemetry`** (antes havia só as bibliotecas soltas, que não se ligam sozinhas, e a app
não exportava nada). Propriedades em `application.yml`: `management.opentelemetry.tracing.export.otlp.*` (traces),
`management.otlp.metrics.export.*` (métricas), `management.opentelemetry.resource-attributes.service.name`
(`oficina-mvp-backend`). Os logs JSON renomeiam `traceId`/`spanId` para **`trace.id`/`span.id`**
(`logging.structured.json.rename`), que é o que o New Relic usa para ligar log a trace. O teste
`ObservabilityAutoConfigurationIntegrationTest` quebra se esses módulos sumirem do build.

## Variáveis que ligam o envio de dados

| Repositório | Secret / Variable |
|---|---|
| `oficina-mvp-java-backend` | `NEW_RELIC_LICENSE_KEY`, `NEW_RELIC_OTLP_ENDPOINT`, `NEW_RELIC_OTLP_METRICS_ENDPOINT`, `TRACING_SAMPLING_PROBABILITY`, `OTLP_METRICS_EXPORT_ENABLED` |
| `oficina-mvp-infra-iac` | `NEW_RELIC_LICENSE_KEY` (instala o `nri-bundle` no cluster) |
| `oficina-auth-function` | `NEW_RELIC_LICENSE_KEY`, `NEW_RELIC_ACCOUNT_ID`, `NEW_RELIC_LAMBDA_LAYER_ARN` |
