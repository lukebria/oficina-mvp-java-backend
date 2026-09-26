# RFC-001 — Escolha da nuvem

| | |
|---|---|
| **Status** | Aceito |
| **Data** | 2026-09-26 |
| **Autores** | Equipe do projeto (registrado por Claude a pedido de Lucas) |

## Contexto

O Tech Challenge Fase 3 exige provisionar, via Infraestrutura como Código, um cluster Kubernetes escalável, um
banco de dados gerenciado, uma function serverless e um API Gateway — de livre escolha de provedor de nuvem. O
projeto já tinha, antes desta fase, dois dos quatro repositórios (`oficina-mvp-infra-iac`,
`oficina-auth-function`) usando AWS via conta de **AWS Academy Learner Lab**.

Restrição relevante para qualquer opção: todo serviço de nuvem usado precisa caber em free tier ou no crédito
fixo de uma conta de laboratório de estudante — esta é uma entrega acadêmica, não uma infraestrutura de
produção real orçada.

## Opções consideradas

1. **AWS (via AWS Academy Learner Lab)** — já em uso nos repositórios existentes.
2. **Google Cloud Platform** — GKE, Cloud SQL, Cloud Functions, API Gateway.
3. **Microsoft Azure** — AKS, Azure Database, Azure Functions, API Management.

## Decisão

**AWS**, usando a conta de AWS Academy Learner Lab já em uso.

## Justificativa

- **Continuidade**: dois dos quatro repositórios já usavam AWS antes desta fase começar — trocar de provedor
  significaria reescrever Terraform e código já funcionais sem ganho correspondente.
- **Familiaridade da equipe**: reduz risco de atraso na entrega por curva de aprendizado de um provedor novo.
- **Crédito já disponível**: a conta de laboratório já está ativa e configurada (LabRole, VPC default), evitando
  o esforço de configurar do zero uma conta gratuita em outro provedor.

## Consequências

- **Credenciais temporárias**: contas do AWS Academy Learner Lab expiram em poucas horas — GitHub Secrets
  precisam ser atualizados manualmente a cada renovação de sessão, em todos os repositórios (tarefa recorrente,
  não uma configuração única).
- **Sem IAM próprio**: a `LabRole` fornecida pelo ambiente não permite criar roles/policies IAM dedicadas —
  todos os recursos (EKS, Lambda) reusam essa role fixa, abaixo do princípio de menor privilégio ideal.
- **Sem Free Tier de 12 meses**: contas de laboratório não são elegíveis ao Free Tier tradicional da AWS — o
  custo de RDS/EKS sai do crédito fixo do lab, exigindo escolhas de menor custo (ex: `db.t3.micro`, sem
  Multi-AZ, sem autoscaling de nós — ver ADR-002).
