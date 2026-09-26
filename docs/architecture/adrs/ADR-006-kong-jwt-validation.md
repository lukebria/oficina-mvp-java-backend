# ADR-006 — Validação do JWT de cliente no API Gateway (Kong), em complemento à validação da aplicação

| | |
|---|---|
| **Status** | Aceito |
| **Data** | 2026-09-26 |

## Contexto

O professor responsável pela disciplina esclareceu que o requisito do enunciado — "a validação desse token nas
rotas protegidas" — **pode ser feita pelo API Gateway**, não necessariamente só pela aplicação. Até então, a
aplicação (`JwtAuthenticationFilter`/`JwtService.parseCustomer`) era o único ponto que validava a assinatura do
JWT de cliente (emitido pela Lambda `oficina-auth-function`); o Kong só fazia roteamento.

## Decisão

O **Kong passa a validar a assinatura e a expiração do JWT de cliente** também, via o plugin nativo `jwt`,
configurado em `oficina-mvp-infra-iac` (`kong-jwt-auth.tf`) — antes da requisição chegar na aplicação. A
**aplicação continua validando o token e revalidando o status do cliente no banco a cada request, sem nenhuma
mudança** em `JwtAuthenticationFilter`/`JwtService` — nenhum código Java foi removido ou enfraquecido.

## Alternativas consideradas

1. **Mover a validação 100% para o Kong, removendo da aplicação**: mais próximo da leitura literal mais simples
   do requisito, mas o plugin `jwt` do Kong só valida assinatura/expiração — ele não sabe nada sobre
   `Customer.status` no banco. Perderíamos a revalidação em tempo real (cliente marcado `INACTIVE` perde acesso
   na hora, sem esperar o token expirar) que já era uma decisão de segurança deliberada e documentada
   (ver `docs/architecture.md`, seção 5.3). Também exigiria reescrever `JwtAuthenticationFilter` para decodificar
   o token sem verificar assinatura (confiando cegamente no Kong) — um padrão que existe e é usado em produção
   em outros contextos, mas que introduz uma superfície de risco maior (qualquer bypass do Kong quebraria a
   segurança por completo) sem necessidade real neste projeto.
2. **Manter só a aplicação validando** (como estava): não aproveitaria a orientação do professor nem demonstraria
   o Kong participando ativamente da segurança, algo explicitamente citado como possível na correção do
   enunciado.
3. **Defesa em profundidade (escolhida)**: os dois validam, cada um com seu papel.

## Justificativa

- **Atende à orientação do professor de forma direta e verificável**: um `curl` com um token adulterado recebe
  `401` do próprio Kong, sem a requisição sequer chegar na aplicação — demonstrável ao vivo no vídeo de entrega.
- **Não regride nenhuma propriedade de segurança já construída**: a revalidação de status do cliente a cada
  request continua existindo exatamente como antes.
- **Custo de implementação e risco baixos**: nenhuma linha de código Java mudou; a mudança inteira ficou
  contida em Terraform (`oficina-mvp-infra-iac`), um claim novo no token (`iss`, `oficina-auth-function`) e a
  separação de um `Ingress` (`oficina-mvp-java-backend/k8s/ingress-public.yaml`) para escopar o plugin só às
  rotas públicas de OS.
- **Padrão comum e legítimo na indústria**: validar o mesmo token em mais de uma camada (gateway + serviço) é
  uma prática real de defesa em profundidade, não uma solução de compromisso improvisada.

## Detalhes técnicos

- Todo cliente compartilha um único `KongConsumer` (`username` = claim `iss` do token, default `customer-app`)
  — não há um Consumer por CPF, já que o volume de clientes é dinâmico e não cadastrado previamente no Kong.
- O plugin `jwt` do Kong casa o token pelo claim `iss`, e valida a assinatura com o mesmo `CUSTOMER_JWT_SECRET`
  já compartilhado entre `oficina-auth-function` e `oficina-mvp-java-backend` — agora um terceiro repositório
  (`oficina-mvp-infra-iac`) também precisa desse mesmo segredo como GitHub Secret.
- Só as rotas `/api/public/service-orders/**` passam pelo plugin (`Ingress` dedicado,
  `konghq.com/plugins: customer-jwt-auth`) — rotas administrativas, health checks e o endpoint interno
  (`/api/internal/customers/{document}`, consumido pela própria Lambda) continuam sem essa camada extra, pois
  usam outros mecanismos de autenticação (JWT administrativo, `X-Internal-Api-Key`).

## Consequências

- Três repositórios agora precisam manter `CUSTOMER_JWT_SECRET` sincronizado (antes eram dois):
  `oficina-auth-function` (assina), `oficina-mvp-infra-iac` (Kong valida), `oficina-mvp-java-backend` (aplicação
  valida) — mudar esse segredo exige atualizar os três ao mesmo tempo.
- **Não testado em cluster real ainda** (nenhum `apply` rodou até a data deste ADR — ver
  `POST-TECH/FASE-3/plans/00-decisoes-tecnicas.md`) — validar com um `curl` de token válido e um adulterado
  assim que o ambiente estiver de pé, antes de considerar isso definitivamente funcional.
- Bootstrap: os CRDs do Kong (`KongConsumer`/`KongClusterPlugin`) são instalados pela mesma Helm release que os
  recursos deste ADR consomem — o primeiro `apply` num cluster novo pode precisar rodar duas vezes (ver
  README de `oficina-mvp-infra-iac`, seção "Validação do JWT de cliente no Kong").
- Limitação aceita conscientemente: o Kong só protege tráfego **norte-sul** (entrando pelo LoadBalancer); um pod
  dentro do mesmo cluster que chamasse `oficina-app-service` diretamente (tráfego leste-oeste) contornaria o
  Kong — mitigado hoje só pelo fato de a aplicação continuar validando o token de qualquer forma (defesa em
  profundidade), não por uma NetworkPolicy dedicada (fora do escopo deste projeto).
