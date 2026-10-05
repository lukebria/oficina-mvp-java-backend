# ADR-002 — Somente HPA de pods, sem autoscaling de nós do cluster

| | |
|---|---|
| **Status** | Aceito |
| **Data** | 2026-09-26 |

## Contexto

O enunciado exige "Cluster Kubernetes com escalabilidade". O cluster EKS já tem um `HorizontalPodAutoscaler`
configurado (`k8s/hpa.yaml`, 1–5 réplicas, CPU alvo 20%), mas o número de **nós** do cluster (o node group EKS,
`oficina-mvp-infra-iac/modules/eks`) tem um tamanho fixo dentro de um range pequeno (`desired_size = 2`,
`max_size = 3`), sem nenhum componente (Cluster Autoscaler, Karpenter) subindo nós novos automaticamente sob
demanda.

## Decisão

Manter **somente o HPA de pods** como mecanismo de escalabilidade — não adicionar Cluster Autoscaler nem
Karpenter neste projeto.

## Alternativas consideradas

- **Cluster Autoscaler**: sobe/desce nós EC2 automaticamente conforme a demanda de pods não cabe nos nós
  existentes.
- **Karpenter**: alternativa mais moderna ao Cluster Autoscaler, com provisionamento mais rápido e granular.

## Justificativa

- **Restrição de custo do AWS Academy Learner Lab**: instâncias EC2 (`t3.medium`, usadas no node group) não são
  elegíveis ao free tier tradicional da AWS. Cluster Autoscaler/Karpenter existem justamente para subir nós
  novos sob demanda — em um pico de carga (inclusive um teste de carga acidental durante o desenvolvimento),
  isso poderia consumir o crédito do lab mais rápido do que o esperado, sem um teto previsível.
- **HPA de pods já demonstra o requisito**: o enunciado pede "escalabilidade" do cluster; o HPA já demonstra na
  prática o comportamento de autoscaling (mais réplicas sob carga de CPU), que é o aspecto observável/testável
  em uma demonstração (inclusive no vídeo de entrega) — sem exigir nós novos, que têm custo menos previsível.
- **Custo zero adicional**: esta opção não introduz nenhum componente novo a operar/monitorar.

## Consequências

- **Limite real de capacidade**: com no máximo 3 nós `t3.medium`, existe um teto de pods que o cluster consegue
  agendar — se o HPA tentar escalar além da capacidade dos nós existentes, novos pods ficam `Pending` até haver
  espaço (não há criação automática de nós para acomodá-los).
- **Pré-requisito: metrics-server** (adendo de 2026-10-04). O EKS não vem com o metrics-server, e sem ele o HPA
  fica com `cpu: <unknown>` e nunca escala — visto no primeiro deploy real. O `oficina-mvp-infra-iac` passou a
  instalá-lo (`modules/metrics-server`, chart Helm oficial; o add-on gerenciado do EKS é negado pelo Learner Lab).
  Validação: `kubectl top pods` e `kubectl get hpa` mostrando a porcentagem real + teste de carga escalando de 1
  para 2+ réplicas.
- **Probes de saúde** (adendo de 2026-10-05, plano 14). Sem `readinessProbe`, os pods novos recebiam tráfego antes
  de o Spring subir (`502`), e o pico de CPU da inicialização da JVM entrava na conta do HPA, que ia a 5 réplicas a
  cada deploy. Com as probes (`startup`/`readiness`/`liveness` no Actuator), o HPA descarta a CPU de pods ainda não
  prontos e o Kong só recebe tráfego de pods prontos.
- Esta decisão é adequada para o volume de um projeto de estudo/demonstração; numa carga de produção real com
  picos imprevisíveis, valeria reconsiderar Cluster Autoscaler com limites de custo bem definidos (ex: teto
  máximo de nós, alertas de billing).
