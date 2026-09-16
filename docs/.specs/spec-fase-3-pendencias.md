# Spec — Tarefas pendentes da Fase 3 (Tech Challenge)

Baseado em `13SOAT - Fase 3 - Tech Challenge.pdf` e na análise dos três repositórios existentes:
[`oficina-mvp-java`](https://github.com/lukebria/oficina-mvp-java-backend),
[`oficina-auth-function`](https://github.com/lukebria/oficina-auth-function) e
[`oficina-mvp-infra-iac`](https://github.com/lukebria/oficina-mvp-infra-iac).

Este documento **não decide** os pontos que o enunciado deixa em aberto — cada um deles está marcado como
**decisão em aberto** e listado de novo, consolidado, na última seção. Já decididas pelo grupo: **nuvem = AWS**
(Academy Learner Lab), **API Gateway = Kong**, e **banco = PostgreSQL via Amazon RDS** (provável — falta só
confirmar na prática que o custo cabe no crédito do lab). Os três repositórios já são públicos e com nomes de
branch alinhados (`master`), então a proteção de branch já pode ser configurada em todos — só falta fazer isso
(ver 2.6). Seguem em aberto: ferramenta de observabilidade, estratégia de homologação/produção, backend do
state do Terraform, e autoscaling de nós.

## Repositórios exigidos vs. existentes

| # | Repositório exigido pelo enunciado          | Repositório atual                                                              | Situação |
|---|----------------------------------------------|----------------------------------------------------------------------------------|----------|
| 1 | Lambda (Function Serverless)                 | [`oficina-auth-function`](https://github.com/lukebria/oficina-auth-function)     | Existe, código completo, Terraform de deploy pronto |
| 2 | Infraestrutura Kubernetes (Terraform)        | [`oficina-mvp-infra-iac`](https://github.com/lukebria/oficina-mvp-infra-iac)     | Existe, provisiona EKS + ECR |
| 3 | Infraestrutura do Banco de Dados Gerenciado (Terraform) | **não existe**                                                        | Banco hoje é um `Deployment` comum de Postgres dentro do mesmo cluster EKS (`k8s/banco.yaml`, no repo do app) |
| 4 | Aplicação principal em Kubernetes            | [`oficina-mvp-java`](https://github.com/lukebria/oficina-mvp-java-backend)       | Existe, código + Dockerfile + manifests + pipeline |

---

## 1. Autenticação e API Gateway

- [x] Function Serverless que valida CPF/CNPJ, consulta existência/status do cliente e gera/devolve um JWT —
  feito (`oficina-auth-function` chamando `GET /api/internal/customers/{document}` no backend).
- [x] Rotas sensíveis (`/api/public/service-orders/**`) protegidas por esse JWT — feito
  (`JwtAuthenticationFilter` no `oficina-mvp-java`).
- [ ] **API Gateway "para controle e roteamento"** da aplicação principal — **decidido: Kong**, rodando dentro
  do próprio cluster EKS (`oficina-mvp-infra-iac`/`oficina-mvp-java`, a definir em qual dos dois repositórios
  o manifesto/Helm chart do Kong entra). Ainda não implementado: hoje a aplicação principal continua exposta
  direto por um `Service` `LoadBalancer` (`k8s/app.yaml`), sem nenhum gateway na frente. A Lambda mantém o
  API Gateway (AWS HTTP API) que já tem hoje (`oficina-auth-function/terraform/api_gateway.tf`) — são dois
  gateways distintos, um por serviço, e isso não muda.

---

## 2. Estrutura de repositórios e CI/CD

### 2.1 Repo 1 — Lambda (`oficina-auth-function`)

- [x] Código da function + Terraform de deploy (Lambda, IAM, log group, HTTP API) — feito.
- [ ] **Pipeline de CI/CD** — não existe nenhum workflow neste repositório; hoje o deploy é `terraform apply`
  manual, executado localmente.
- [x] **Branch padrão corrigida para `master`** (estava como `homolog` por efeito colateral de termos
  empurrado essa branch antes da `master` num repositório recém-criado).
- [x] Branch `master` protegida contra commit direto + PR obrigatório para merge (ver 2.6).
- [ ] Deploy automático diferenciando branch de homologação e branch de produção — hoje não existe pipeline
  nenhuma, então também não existe essa distinção.
- [ ] **Usuário `soat-architecture` não está entre os colaboradores deste repositório** — adicionar (confirmado
  via API; está presente nos outros dois repositórios).
- [ ] README: falta um diagrama de arquitetura específico deste repositório e um link de Swagger/Postman (a
  function tem só um endpoint — avaliar se cabe uma collection Postman simples) e um link de deploy ativo,
  quando existir.

### 2.2 Repo 2 — Infraestrutura Kubernetes (`oficina-mvp-infra-iac`)

- [x] Terraform provisiona EKS + ECR — feito (rodando em conta de AWS Academy Learner Lab, com a `LabRole`).
- [x] Pipeline de CI/CD existe: `create_iac.yml` (fmt/validate → plan → apply) e `destroy_iac.yml` (manual).
- [x] **Repositório tornado público** e branch padrão renomeada de `main` para `master` (alinhado com os outros
  dois repositórios) — isso também desbloqueou a checagem/configuração de branch protection via API, que antes
  era recusada pedindo GitHub Pro (repositório era privado).
- [x] Sobra da branch `main` antiga verificada e removida do remoto.
- [x] PR da branch `homolog` (reescrita do README) mergeado em `master` —
  [#1](https://github.com/lukebria/oficina-mvp-infra-iac/pull/1).
- [ ] **Gatilhos de `pull_request`/`push` apontam para uma branch `main-disabled`**, que não existe mais (a
  branch real agora é `master`) — hoje o workflow só roda via disparo manual (`workflow_dispatch`). Se a
  intenção é ter deploy automático (exigido pelo enunciado), trocar `main-disabled` por `master` nos gatilhos.
- [x] Branch `master` protegida + PR obrigatório (ver 2.6).
- [ ] Deploy automático diferenciando homologação/produção — existe a branch `homolog`, mas nenhum workflow
  dispara automaticamente nela hoje.
- [ ] Lock de state via DynamoDB — não configurado no backend S3 (`backends.tf`); duas execuções simultâneas
  podem corromper o state.
- [x] `soat-architecture` já está como colaborador.
- [ ] O diagrama do repositório mostra "Terraform Cloud" como backend do state, mas o código usa S3.
  **Decisão em aberto**: atualizar o diagrama para refletir S3, ou migrar de fato para Terraform Cloud.

### 2.3 Repo 3 — Infraestrutura do Banco de Dados Gerenciado (Terraform)

- [ ] **O repositório não existe.** Hoje o Postgres roda como um `Deployment` comum de Kubernetes
  (`postgres:15-alpine`) dentro do mesmo cluster EKS, versionado em `k8s/banco.yaml` no repo da aplicação —
  sem backup, sem alta disponibilidade, e sem nenhuma linha de Terraform.
- [x] **Motor de banco: decidido — PostgreSQL** (mantém o que a aplicação já usa via JPA/Hibernate + Flyway),
  **condicionado a existir uma opção gratuita/barata o suficiente** dentro da restrição de custo do projeto.
- [x] **Serviço gerenciado: decidido (provável) — Amazon RDS PostgreSQL**, instância pequena (`db.t3.micro`/
  `db.t4g.micro`), single-AZ. Falta só confirmar/validar que o custo real cabe no crédito disponível do lab
  antes de considerar isso 100% fechado — se não couber, a alternativa seria reavaliar (ex: manter o Postgres
  em pod no cluster por mais tempo).
- [ ] Depois da decisão: criar o repositório com Terraform do banco gerenciado + rede necessária (subnets,
  security group liberando acesso do cluster EKS), pipeline de CI/CD própria, e então:
  - remover `k8s/banco.yaml` do `oficina-mvp-java`;
  - apontar `DB_HOST` (em `k8s/config-secret.yaml`/`app.yaml`) para o endpoint do banco gerenciado;
  - migrar os dados, se já houver um ambiente rodando com o Postgres em pod.

### 2.4 Repo 4 — Aplicação principal em Kubernetes (`oficina-mvp-java`)

- [x] Código + Dockerfile + manifests K8s (`app.yaml`, `hpa.yaml`, `config-secret.yaml`) + pipeline
  (`app-deploy.yml`: testes, SonarQube, build/push da imagem pro ECR, deploy no cluster) — feito.
- [ ] **O trabalho da Fase 3 em si (status do cliente, endpoint interno, filtro de API key, exigência de JWT
  nas rotas públicas) ainda não está commitado** — está todo modificado/novo no working tree da branch
  `feature/tech_chalange_fase_3` (só o `README.md` e o `docs/.specs/spec-init-fase-3.md` foram commitados até
  agora). Precisa revisar, commitar o restante e abrir PR para `master`.
- [ ] `k8s/banco.yaml` precisa ser removido quando o Repo 3 existir (ver 2.3).
- [x] Branch `master` protegida + PR obrigatório para merge (ver 2.6).
- [ ] Deploy automático diferenciando homologação/produção — existe uma branch remota `homolog`, mas
  `app-deploy.yml` só dispara em push/PR para `main`/`master`; não há distinção de ambiente hoje.
- [x] `soat-architecture` já está como colaborador.

### 2.5 Secrets e variáveis de CI/CD (transversal aos três repositórios)

Conferido via `gh secret list` / `gh variable list` o que está **realmente configurado** em cada repositório
hoje, comparado com o que os workflows esperam (`secrets.*`/`vars.*` usados em cada `.yml`):

**`oficina-mvp-java`** (`app-deploy.yml`):

| Nome                        | Tipo     | Configurado? |
|-----------------------------|----------|--------------|
| `SONAR_TOKEN`               | secret   | ✅ sim |
| `SONAR_ORGANIZATION`        | variable | ✅ sim |
| `SONAR_PROJECTS`            | variable | ✅ sim |
| `AWS_ACCESS_KEY_ID`         | secret   | ❌ não |
| `AWS_SECRET_ACCESS_KEY`     | secret   | ❌ não |
| `AWS_SESSION_TOKEN`         | secret   | ❌ não |
| `AWS_DEFAULT_REGION`        | variable | ❌ não |
| `DB_PASSWORD`               | secret   | ❌ não |
| `JWT_SECRET`                | secret   | ❌ não |
| `CUSTOMER_JWT_SECRET`       | secret   | ❌ não |
| `INTERNAL_API_KEY`          | secret   | ❌ não |
| `SEED_ADMIN_PASSWORD`       | secret   | ❌ não |
| `MAIL_USERNAME`/`MAIL_PASSWORD`/`MAIL_FROM` | secret | ❌ não |
| `MAIL_HOST`/`MAIL_PORT`     | variable | ❌ não |

⚠️ **Hoje o job de deploy desse pipeline falharia imediatamente** no passo "Configure AWS Credentials" — só o
job de testes (não precisa de segredo nenhum) e o de SonarQube (só precisa do `SONAR_TOKEN`, que já existe)
rodariam com sucesso.

**`oficina-mvp-infra-iac`** (`create_iac.yml`/`destroy_iac.yml`):

| Nome                    | Tipo     | Configurado? |
|--------------------------|----------|--------------|
| `AWS_ACCESS_KEY_ID`      | secret   | ❌ não |
| `AWS_SECRET_ACCESS_KEY`  | secret   | ❌ não |
| `AWS_SESSION_TOKEN`      | secret   | ❌ não |
| `AWS_DEFAULT_REGION`     | variable | ❌ não |

Nenhum secret/variable está configurado neste repositório — mesmo depois de corrigir o gatilho `main-disabled`
(ver 2.2), o workflow falharia no mesmo passo por falta de credenciais.

**`oficina-auth-function`** — ainda sem workflow (ver 2.1). Quando a pipeline for criada, vai precisar das
mesmas `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY`/`AWS_SESSION_TOKEN`/`AWS_DEFAULT_REGION`, além dos valores
de runtime da function (`BACKEND_BASE_URL`, `INTERNAL_API_KEY`, `CUSTOMER_JWT_SECRET`, `TOKEN_TTL_SECONDS`) que
hoje só existem no `terraform.tfvars` local de quem já aplicou manualmente.

⚠️ **Particularidade da AWS Academy Learner Lab**: as três credenciais AWS são **temporárias** e expiram em
poucas horas — mesmo depois de configuradas pela primeira vez, alguém do grupo vai precisar **atualizar esses
secrets manualmente em cada um dos três repositórios** toda vez que a sessão do lab for renovada. Não é uma
configuração única; é uma tarefa recorrente enquanto o projeto usar esse tipo de conta.

### 2.6 Proteção das branches principais (main/master) — exigência explícita do enunciado

O enunciado exige, na seção "Regras de proteção": branch `main`/`master` protegida (sem commit direto) e PR
obrigatório para merge. Status real hoje, conferido via API do GitHub:

| Repositório              | Branch principal                          | Visibilidade | Protegida hoje? |
|---------------------------|--------------------------------------------|--------------|------------------|
| `oficina-mvp-java`        | `master`                                   | pública      | ✅ sim |
| `oficina-auth-function`   | `master`                                   | pública      | ✅ sim |
| `oficina-mvp-infra-iac`   | `master`                                   | pública      | ✅ sim |

Configurado nos três (via API do GitHub, confirmado por leitura de volta): PR obrigatório antes de merge
(`required_pull_request_reviews`, sem exigir aprovação — `required_approving_review_count: 0`, já que o
enunciado só pede PR obrigatório, não aprovação), force-push bloqueado (`allow_force_pushes: false`), exclusão
da branch bloqueada (`allow_deletions: false`), e `enforce_admins: true` (a proteção vale até pra quem tem
acesso de admin no repositório, pra realmente não ter "commit direto" possível por ninguém).

- [x] `oficina-mvp-java` (`master`)
- [x] `oficina-auth-function` (`master`)
- [x] `oficina-mvp-infra-iac` (`master`)

---

## 3. Infraestrutura obrigatória (checklist geral)

- [x] Function Serverless para autenticação.
- [ ] API Gateway — ver seção 1 (hoje só cobre a Lambda).
- [ ] Banco de Dados Gerenciado — ver 2.3 (não existe).
- [~] Cluster Kubernetes **com escalabilidade** — o cluster (EKS) existe e o HPA de pods já está configurado
  (`k8s/hpa.yaml`, 2–5 réplicas, CPU 70%); o autoscaling de **nós** do cluster (Cluster Autoscaler/Karpenter)
  não está configurado no Terraform do EKS. **Decisão em aberto**: se o HPA de pods já atende ao requisito, ou
  se autoscaling de nós também é esperado.
- [x] Terraform para provisionamento — parcial: Lambda e EKS/ECR feitos; banco gerenciado pendente (2.3).

---

## 4. Monitoramento e Observabilidade

Nada disso existe ainda em nenhum dos três repositórios (busca no código não encontrou nenhuma referência a
ferramentas ou padrões de observabilidade):

- [ ] **Decisão em aberto — ferramenta**: Datadog ou New Relic (enunciado deixa livre escolha).
- [ ] Latência das APIs.
- [ ] Consumo de recursos do Kubernetes (CPU, memória).
- [ ] Healthchecks e uptime monitorados pela ferramenta escolhida — os endpoints `/api/health` e
  `/actuator/health` já existem na aplicação; falta só conectá-los a uma ferramenta externa.
- [ ] Alertas para falhas no processamento de ordens de serviço.
- [ ] Logs estruturados (JSON) com correlação entre requisições — nenhuma configuração de logging estruturado
  ou trace/correlation ID existe hoje em nenhum dos três repositórios (nem no backend Java, nem na function).
- [ ] Dashboards: volume diário de OS, tempo médio de execução por status, erros/falhas nas integrações. A API
  `GET /api/reports/average-execution-time` já calcula tempo médio de execução como **funcionalidade de
  negócio**, mas isso não é um dashboard de observabilidade — os dashboards em si (visuais, na ferramenta
  escolhida) ainda precisam ser criados.

---

## 5. Documentação da Arquitetura

- [~] **Diagrama de Componentes** — existe parcialmente: `oficina-mvp-java/docs/architecture.md` (seção 13) tem
  diagramas de contexto/componentes/camadas, e `oficina-mvp-infra-iac` tem um diagrama próprio (desatualizado,
  ver 2.2). Nenhum diagrama único cobre nuvem + APIs + banco + monitoramento juntos — monitoramento nem existe
  ainda para poder aparecer nele.
- [ ] **Diagrama de Sequência** especificamente para (a) o fluxo de autenticação via CPF (cliente → function →
  backend → function → cliente) e (b) abertura de ordem de serviço. O que existe hoje
  (`docs/architecture.md`, seção 13.4) é um diagrama genérico do módulo `customer` (CRUD), não cobre esses dois
  fluxos específicos.
- [ ] **RFCs** para decisões técnicas (escolha da nuvem, do banco, da estratégia de autenticação) — não existe
  nenhum RFC em nenhum repositório. A escolha de nuvem (AWS), a de banco (PostgreSQL via RDS) e a estratégia de
  autenticação (JWT assinado pela Lambda a partir do CPF) já foram feitas na prática; falta documentá-las
  formalmente.
- [ ] **ADRs** para decisões arquiteturais permanentes (ex: comunicação síncrona HTTP entre a function e o
  backend em vez de assíncrona/fila, uso de HPA) — não existe nenhum ADR ainda.
- [ ] **Justificativa formal da escolha do banco de dados** + ajustes no modelo relacional com diagramas ER — o
  MER já existe (`docs/MER.drawio`), mas não há documento de justificativa formal, e os "ajustes" dependem da
  decisão do banco gerenciado (2.3).

---

## 6. Entregáveis finais

- [ ] README de cada repositório com: descrição, tecnologias, passos de execução/deploy, diagrama específico
  daquele repositório, link para Swagger/Postman.
  - `oficina-mvp-java`: tem quase tudo; falta o diagrama de infraestrutura refletir os 4 repositórios e um
    link de collection Postman (hoje só aponta para o Swagger local).
  - `oficina-auth-function`: não tem diagrama de arquitetura próprio no README.
  - `oficina-mvp-infra-iac`: tem diagrama, mas desatualizado (ver 2.2).
  - Repositório do banco: não existe ainda.
- [ ] Links para os deploys ativos — nenhum README hoje tem uma URL de ambiente publicado, só instruções de
  execução local.
- [ ] Vídeo de demonstração (até 15 min) cobrindo: autenticação com CPF, execução da pipeline CI/CD, deploy
  automatizado, consumo das APIs protegidas, dashboard de monitoramento ao vivo, logs e traces em execução —
  depende de praticamente tudo acima estar pronto (principalmente observabilidade). É tarefa de gravação, não
  de código.
- [ ] Documento PDF único para o Portal do Aluno com os links dos 4 repositórios, do vídeo, das documentações,
  e confirmação do `soat-architecture` em todos os repositórios — falta adicionar esse usuário no
  `oficina-auth-function` antes de poder confirmar (ver 2.1).

---

## Decisões em aberto (consolidado)

Nenhuma delas foi definida por este documento. Pra cada uma, listei algumas opções possíveis pra discussão do
grupo — não é uma recomendação, é só o ponto de partida da conversa.

> ⚠️ **Restrição de custo**: todo serviço de cloud usado precisa caber em free tier ou em crédito de
> estudante/lab — isso é só para a entrega do trabalho, não é uma infra de produção de verdade. Anotei, em
> cada opção abaixo, o que isso implica em custo, mas a escolha final continua sendo do grupo.
>
> ✅ **Nuvem: decidido — AWS**, usando conta de **AWS Academy Learner Lab** (a mesma já usada em
> `oficina-mvp-infra-iac` e `oficina-auth-function`). Isso deixa de ser uma decisão em aberto; falta só
> documentar formalmente como RFC (ver seção 5).
>
> ✅ **API Gateway: decidido — Kong** (ver item 1 abaixo). Escopo ainda a confirmar.
>
> ✅ **Banco: decidido (provável) — PostgreSQL via Amazon RDS** (ver item 2 abaixo). Falta só confirmar na
> prática que o custo real cabe no crédito do lab antes de dar como 100% fechado.

### 1. API Gateway — Kong (ferramenta decidida); falta confirmar o escopo

**Ferramenta**: ✅ decidido — **Kong**, rodando dentro do próprio cluster EKS já existente (não adiciona nenhum
serviço gerenciado novo/billável, só consome recursos do cluster que já está provisionado — combina com a
restrição de custo). Falta decidir/implementar:

- Em qual repositório o manifesto/Helm chart do Kong entra — `oficina-mvp-infra-iac` (como parte da infra do
  cluster) ou `oficina-mvp-java` (como parte do deploy da aplicação).
- **Escopo**: (a) Kong só na frente da aplicação principal, mantendo a Lambda com o AWS API Gateway (HTTP API)
  que ela já tem hoje (`oficina-auth-function/terraform/api_gateway.tf`) — são dois gateways, um por serviço;
  ou (b) avaliar se faz sentido também colocar a Lambda atrás do Kong em algum momento (não é o desenho atual,
  e trocaria o que já está implementado e funcionando na function).

### 2. Banco de dados gerenciado — quase fechado

**Motor: ✅ decidido — PostgreSQL** (mantém o que a aplicação já usa via JPA/Hibernate + Flyway).

**Serviço gerenciado: ✅ decidido (provável) — Amazon RDS PostgreSQL**, instância pequena
(`db.t3.micro`/`db.t4g.micro`), single-AZ, storage mínimo — é o serviço gerenciado mais barato das opções
avaliadas.

⚠️ Único ponto a confirmar antes de considerar 100% fechado: contas de **AWS Academy Learner Lab** não são
elegíveis para o Free Tier "de 12 meses" das contas normais da AWS — o custo do RDS aqui sai do crédito fixo
do lab, não é literalmente gratuito, só barato o suficiente pra provavelmente não estourar o crédito numa
sessão curta de uso. Vale confirmar isso na prática (subir a instância e acompanhar o consumo de crédito) antes
de dar como certo.

Descartada: **Amazon Aurora** — custo bem mais alto que RDS mesmo no menor tamanho (inclusive o Aurora
Serverless v2 tem um consumo mínimo cobrado por hora enquanto a instância existe); não cabe na restrição de
"só free/crédito de estudante".

### 3. Ferramenta de observabilidade

- **Datadog** — tem um plano free permanente, mas limitado (poucos hosts, retenção de log curta, APM/infra
  restritos); costuma pedir cartão de crédito mesmo pro trial dos planos pagos, o que exige atenção pra não
  passar do free sem querer.
- **New Relic** — o free tier costuma ser mais generoso pra esse tipo de uso (ingestão mensal de dados
  gratuita, sem exigir cartão pra começar, com APM/logs/infra/dashboards incluídos no plano free) — tende a
  encaixar melhor numa restrição de "só o que for gratuito".
- O enunciado restringe a essas duas opções — vale o grupo conferir as condições atuais de cada free tier
  antes de decidir, porque esses planos mudam com frequência.

### 4. Estratégia de homologação/produção

- **Namespaces separados no mesmo cluster EKS** (`homolog`/`prod`), cada branch fazendo deploy no namespace
  correspondente — a única opção realista dentro da restrição de custo: usa o cluster único que já existe, sem
  nenhum recurso novo cobrando.
- **Clusters EKS separados por ambiente** — o **control plane do EKS já tem custo por hora mesmo fora de
  qualquer free tier** (não existe EKS "grátis"); ter dois clusters dobra esse custo fixo, além de exigir
  outro node group e potencialmente outro banco. Difícil de justificar sob a restrição de "só free/crédito de
  estudante".
- **Contas AWS separadas por ambiente** — isolamento máximo, mas overhead alto e provavelmente incompatível com
  a estrutura de uma única conta de AWS Academy Learner Lab por grupo/aluno.

### 5. Backend do state do Terraform (`oficina-mvp-infra-iac`)

- **Manter S3** + adicionar uma tabela DynamoDB para lock — ambos praticamente gratuitos nessa escala (o state
  é um arquivo pequeno no S3; o DynamoDB tem free tier permanente que cobre folgado uma tabela de lock). Essa é
  a opção com custo mais previsível/menor.
- **Migrar para Terraform Cloud** — o plano free do Terraform Cloud também cobre esse volume de recursos sem
  custo, então a restrição de orçamento não elimina essa opção; a diferença aqui é mais sobre esforço de
  configuração (criar workspace, ajustar os workflows de CI para autenticar com o Terraform Cloud) do que sobre
  custo.

### 6. Autoscaling de nós do cluster

- **Manter só o HPA de pods** já configurado (2–5 réplicas, CPU 70%) — nenhum recurso novo, custo zero
  adicional; a opção mais segura sob restrição de orçamento, já que não cria nós novos sozinha.
- **Adicionar o Cluster Autoscaler** — o controlador em si é gratuito, mas a função dele é subir **nós EC2
  novos automaticamente** quando falta capacidade — e instâncias EC2 (`t3.medium`, usadas no node group) não
  são o tipo elegível ao free tier tradicional da AWS. Num pico de carga (inclusive um teste de carga sem
  querer), isso pode consumir crédito de lab mais rápido do que o esperado.
- **Adicionar Karpenter** — mesma dinâmica de custo do Cluster Autoscaler (também sobe nós novos sob demanda),
  com a complexidade de configuração ainda maior — provavelmente o que menos se encaixa numa entrega com
  restrição de "só free/crédito de estudante".

### 7. Visibilidade do `oficina-mvp-infra-iac` — ✅ resolvido

Repositório tornado público e branch padrão renomeada para `master`. Branch protection já pode ser configurada
nos três repositórios sem custo nenhum (ver seção 2.6).
