# Diagramas de Arquitetura — Tech Challenge Fase 3

PNGs prontos para o documento de entrega (PDF), gerados a partir das fontes Mermaid em [`fonte-mmd/`](fonte-mmd/), que refletem
o código dos 4 repositórios. Clique no PNG e use *Download raw file* para baixar.

Última revisão: **2026-10-12** — 02, 09 e 10 atualizados (Kong barra o token, bucket de state por conta, deploy lê
o RDS da AWS); 14 (CI/CD) e 15 (observabilidade) novos.

## Legendas prontas para o documento

| Figura | Arquivo | Legenda sugerida |
|---|---|---|
| 1 | `01-componentes-visao-nuvem.png` | **Visão de componentes na nuvem.** Os 4 repositórios na AWS: Lambda de login por CPF atrás do API Gateway da AWS; aplicação no EKS atrás do Kong; banco RDS PostgreSQL; imagens no ECR; observabilidade no New Relic. |
| 2 | `02-sequencia-autenticacao-cpf.png` | **Autenticação por CPF.** O Kong barra a rota do cliente sem token; a Lambda valida o CPF, consulta o cliente no backend por uma API interna e só assina o JWT para cliente ativo; o Kong valida o JWT antes de encaminhar. |
| 3 | `03-sequencia-abertura-os.png` | **Abertura de ordem de serviço.** Fluxo administrativo: validações, criação de cliente/veículo, cálculo do orçamento e registro de histórico. |
| 4 | `04-er-diagrama.png` | **Modelo entidade-relacionamento** do banco PostgreSQL (versionado com Flyway). |
| 5 | `14-cicd-fluxo.png` | **CI/CD.** Git flow `feat → homolog → master` (master protegida, PR obrigatório); testes e análise estática em todo PR; deploy na AWS na ordem infra → banco e function → aplicação, `homolog` e `prod` em namespaces separados. |
| 6 | `15-observabilidade.png` | **Observabilidade.** Traces e métricas de negócio por OpenTelemetry (OTLP), infraestrutura e logs JSON pelo agente do cluster, dashboard de negócio/operação e alertas por e-mail no New Relic. |
| 7 | `09-infra-k8s-componentes.png` | **Infraestrutura Kubernetes** (`oficina-mvp-infra-iac`): EKS, Kong com LoadBalancer e plugin JWT, namespaces `homolog`/`prod`, metrics-server para o HPA, ECR e state do Terraform em S3 + DynamoDB. |
| 8 | `10-infra-db-componentes.png` | **Banco de dados gerenciado** (`oficina-mvp-infra-db`): RDS PostgreSQL acessível só a partir do cluster; senha no Secrets Manager, lida pelo deploy da aplicação. |
| 9 | `11-auth-function-sequencia.png` | **Function serverless** (`oficina-auth-function`): passos internos da Lambda de autenticação. |

## Complementares (arquitetura interna da aplicação)

| Arquivo | Conteúdo |
|---|---|
| `05-contexto-runtime.png` | Quem fala com a API e o que fica fora do processo |
| `06-componentes-bounded-contexts.png` | Módulos do backend e dependências entre eles (via portas) |
| `07-camadas-hexagonais.png` | Fluxo de uma request pelas camadas hexagonais |
| `08-sequencia-exemplo-customer.png` | Exemplo de sequência do padrão hexagonal (módulo `customer`) |

## Legados (histórico)

| Arquivo | O que é |
|---|---|
| `../MER.drawio` | MER original em draw.io — a versão Mermaid (04) é a atualizada |

## Regenerar

```bash
npx -y @mermaid-js/mermaid-cli@11.4.2 -i fonte-mmd/NOME.mmd -o NOME.png -b white --width 1800 -s 2 -p puppeteer.json
```
No Windows, `puppeteer.json` = `{"executablePath":"C:/Program Files/Google/Chrome/Application/chrome.exe"}` (o
navegador que o mermaid-cli tenta baixar sozinho não abre aqui).
