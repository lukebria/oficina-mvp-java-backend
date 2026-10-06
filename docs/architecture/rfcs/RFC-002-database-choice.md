# RFC-002 — Escolha do banco de dados

| | |
|---|---|
| **Status** | Aceito |
| **Data** | 2026-09-26 |
| **Autores** | Equipe do projeto (registrado por Claude a pedido de Lucas) |

## Contexto

O enunciado exige um Banco de Dados Gerenciado (PostgreSQL, MySQL, SQL Server, etc.) provisionado via Terraform,
em repositório próprio. Hoje (antes desta decisão), o PostgreSQL da aplicação roda como um `Deployment` comum
dentro do mesmo cluster EKS (`oficina-mvp-java-backend/k8s/banco.yaml`) — sem backup gerenciado, sem alta
disponibilidade nativa, e sem nenhuma linha de Terraform.

## Opções consideradas

1. **PostgreSQL via Amazon RDS** — mesmo motor já usado pela aplicação (JPA/Hibernate + Flyway).
2. **MySQL via Amazon RDS** — exigiria trocar dialeto SQL, revisar migrations Flyway e tipos de coluna.
3. **SQL Server via Amazon RDS** — licenciamento mais caro, sem vantagem técnica para este domínio.
4. **Amazon Aurora (PostgreSQL-compatible)** — compatível com o driver/dialeto atual, mas custo mais alto mesmo
   no menor tamanho (Aurora Serverless v2 tem consumo mínimo cobrado por hora enquanto a instância existe).
5. **Manter Postgres em pod no cluster** (status quo) — não atende ao requisito explícito de "Banco de Dados
   Gerenciado" do enunciado.

## Decisão

**PostgreSQL via Amazon RDS**, instância `db.t3.micro` (ou `db.t4g.micro`), single-AZ, `gp3`, sem Multi-AZ.

## Justificativa

- **Motor já em uso**: zero mudança de dialeto SQL, zero reescrita de migrations Flyway ou queries nativas —
  elimina o maior risco técnico de qualquer alternativa (MySQL/SQL Server).
- **Custo**: entre os serviços gerenciados equivalentes na AWS, RDS de instância pequena é o mais barato — Aurora
  foi descartado justamente por ter consumo mínimo cobrado por hora mesmo na variante Serverless v2, incompatível
  com a restrição de "só free tier/crédito de estudante" (ver RFC-001).
- **Maturidade operacional**: RDS já resolve backup automático, patching de segurança do motor e
  monitoramento básico (CloudWatch) sem esforço de configuração adicional — ganho direto em relação ao Postgres
  em pod atual, que não tem nenhuma dessas garantias.

## Ressalvas

- ⚠️ **Custo real no crédito do lab não validado formalmente até a data deste RFC** — contas AWS Academy
  Learner Lab não são elegíveis ao Free Tier de 12 meses; o plano de execução
  (`POST-TECH/FASE-3/plans/01-infra-db-novo-repo.md`) inclui, como passo explícito, acompanhar o consumo de
  crédito nas primeiras horas após o primeiro `apply` antes de considerar esta decisão 100% fechada na prática
  (não só no papel).
- **Sem Multi-AZ**: aceitável para um projeto de estudo; numa carga de produção real, seria o primeiro item a
  reconsiderar.

## Consequências

- Repositório novo `oficina-mvp-infra-db` criado especificamente para este Terraform (repo 3/4 exigido pelo
  enunciado).
- Migração da aplicação: remoção de `k8s/banco.yaml`, atualização de `DB_HOST`/`DB_PORT`/`DB_NAME` para o
  endpoint do RDS.
- Backend do state e security group do RDS dependem de outputs do repositório de infra Kubernetes
  (`vpc_id`, `subnet_ids`, `eks_cluster_security_group_id`) via `terraform_remote_state` — acopla a ordem de
  aplicação dos dois repositórios (infra K8s primeiro).

## Adendo — validação em ambiente real (2026-10-04/05)

- **Custo medido**: RDS `db.t3.micro` single-AZ + 20 GB ≈ **US$ 0,02/h** (ambiente completo dos 4 repos ≈ US$ 0,26/h).
  A ressalva de custo acima fica resolvida: cabe no crédito do lab, desde que o ambiente seja destruído depois de
  cada uso.
- **Versão**: `engine_version = "16"` (só a major). A `16.4` original foi retirada pela AWS e o apply falhava; com a
  major, a AWS escolhe a minor default (16.13 no teste).
- **Banco/usuário** alinhados com a aplicação (`oficina_mvp`/`oficina`). A aplicação conectou, rodou as migrations
  do Flyway e atendeu o fluxo completo.
- **Migração**: não foi preciso migrar dados (ambiente criado do zero). O `k8s/banco.yaml` segue como fallback,
  usado só quando `DB_HOST` não está configurada.
