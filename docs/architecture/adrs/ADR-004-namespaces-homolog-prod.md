# ADR-004 — Namespaces homolog/prod no mesmo cluster

| | |
|---|---|
| **Status** | Aceito |
| **Data** | 2026-09-26 |

## Contexto

O enunciado exige "deploy automático das branches de homologação e produção" — o projeto precisa de alguma
forma de separar os dois ambientes. O cluster EKS já existe como um único cluster (`oficina-mecnica-lab-cluster`,
`oficina-mvp-infra-iac`).

## Decisão

**Namespaces separados no mesmo cluster EKS** (`homolog` e `prod`, provisionados via Terraform em
`namespaces.tf`) — o pipeline de deploy (`oficina-mvp-java-backend/.github/workflows/app-deploy.yml`) escolhe o
namespace de destino a partir da branch de origem do push, e o `Ingress` de cada ambiente usa um `host`
diferente para o Kong compartilhado não colidir entre os dois.

## Alternativas consideradas

- **Clusters EKS separados por ambiente** (um cluster `homolog`, outro `prod`): isolamento maior entre
  ambientes, mas o control plane do EKS já tem custo por hora mesmo fora de qualquer free tier — não existe "EKS
  grátis". Ter dois clusters dobra esse custo fixo, além de exigir outro node group e potencialmente outra
  instância de banco.
- **Contas AWS separadas por ambiente**: isolamento máximo, mas inviável na prática dentro de uma única conta de
  AWS Academy Learner Lab por grupo/aluno — o ambiente de laboratório não foi desenhado para múltiplas contas
  por projeto.

## Justificativa

- **Custo zero adicional**: reaproveita o cluster único já provisionado — nenhum recurso novo billable por hora
  é criado só para separar ambientes.
- **Suficiente para o objetivo do requisito**: o enunciado pede que o deploy automático diferencie
  homologação de produção — namespaces já entregam isolamento de recursos Kubernetes (Deployments, Services,
  ConfigMaps, Secrets, Ingress) suficiente para esse propósito, sem exigir duplicar infraestrutura de nuvem.

## Consequências

- **Kong compartilhado entre ambientes**: como os dois namespaces usam o mesmo Kong (mesmo `LoadBalancer`,
  mesmo IP/DNS), os `Ingress` de cada ambiente precisam de um `host` diferente
  (`homolog.oficina-mvp.local`/`prod.oficina-mvp.local`) para o roteamento não colidir — sem um domínio real
  configurado ainda, testar cada ambiente exige forçar o header `Host` manualmente (`curl -H "Host: ..."`) em vez
  de acessar por uma URL amigável.
- **Isolamento parcial, não total**: os dois ambientes compartilham o mesmo cluster (mesmos nós, mesmo Kong,
  mesma capacidade de CPU/memória do node group) — um ambiente sob carga alta pode competir por recursos com o
  outro. Aceitável para um projeto de estudo; não seria a escolha recomendada para uma separação real de
  produção com SLA.
- **Banco de dados único**: o RDS provisionado em `oficina-mvp-infra-db` não tem, no momento desta decisão,
  instâncias/schemas separados por ambiente — os dois ambientes compartilhariam o mesmo banco a menos que essa
  decisão seja revisitada.
