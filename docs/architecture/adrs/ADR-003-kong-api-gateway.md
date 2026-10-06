# ADR-003 — Kong como API Gateway da aplicação principal

| | |
|---|---|
| **Status** | Aceito (já implementado) |
| **Data** | 2026-09-26 (documentado retroativamente) |

## Contexto

O enunciado exige um API Gateway "para controle e roteamento" na frente da aplicação principal, protegendo
rotas sensíveis. O exemplo dado inclui AWS API Gateway, Kong, Traefik "ou outro".

## Decisão

**Kong**, rodando dentro do próprio cluster EKS via Helm (`oficina-mvp-infra-iac/modules/kong`), em modo
**DB-less** com **Ingress Controller** habilitado. A aplicação principal declara um `Ingress`
(`k8s/ingress.yaml`, `ingressClassName: kong`) que o Kong descobre automaticamente.

## Alternativas consideradas

- **AWS API Gateway** na frente do cluster (via VPC Link ou NLB): serviço gerenciado adicional, billável por
  requisição/hora — custo extra não comparado ao de usar um componente que já roda dentro do cluster existente.
- **Traefik**: alternativa também comum como Ingress Controller no Kubernetes, tecnicamente equivalente ao Kong
  para o escopo deste requisito.

## Justificativa

- **Custo zero adicional**: o Kong roda como pods dentro do cluster EKS já provisionado — não é um serviço
  gerenciado novo e billável, diferente do AWS API Gateway na frente do cluster. Consome só os recursos de
  computação que o cluster já tem.
- **Modo DB-less**: dispensa provisionar um banco próprio para o Kong (Postgres/Cassandra), reduzindo tanto
  custo quanto superfície operacional — a configuração de rotas vem inteiramente de recursos `Ingress` nativos
  do Kubernetes, sem estado próprio do Kong para gerenciar.
- **Ingress Controller é o padrão idiomático do Kubernetes**: usar Kong como Ingress Controller (em vez de, por
  exemplo, configurá-lo via API administrativa própria) mantém a configuração de roteamento como código
  Kubernetes versionável (`k8s/ingress.yaml`), consistente com o resto do projeto.

## Consequências

- **Dois API Gateways distintos no projeto**: Kong para a aplicação principal, AWS API Gateway (HTTP API) para a
  Lambda de autenticação — decisão deliberada (ver `oficina-auth-function`), não um gateway único cobrindo os
  dois serviços. Cada serviço mantém o gateway que já fazia mais sentido para ele individualmente.
- **Roteamento por host para múltiplos ambientes**: ao introduzir os namespaces `homolog`/`prod` no mesmo
  cluster (ADR-004), foi necessário diferenciar o `host` do `Ingress` de cada ambiente — um Kong único atendendo
  dois ambientes exige essa distinção para não haver colisão de rota.
- **Sem autenticação própria no Kong**: a autenticação (JWT administrativo e de cliente) continua sendo
  responsabilidade da aplicação (`JwtAuthenticationFilter`), não de um plugin do Kong — o Kong atua só como
  roteador/gateway de borda, não como camada de autenticação centralizada.
