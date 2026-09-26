# ADR-001 — Comunicação síncrona HTTP entre a Lambda e o backend

| | |
|---|---|
| **Status** | Aceito (já implementado) |
| **Data** | 2026-09-26 (documentado retroativamente) |

## Contexto

A Function Serverless (`oficina-auth-function`) precisa consultar existência/status do cliente no backend
(`oficina-mvp-java-backend`) antes de emitir um JWT. Essa consulta pode ser síncrona (HTTP request-response,
bloqueando a execução da Lambda até a resposta) ou assíncrona (ex: publicar um evento numa fila e aguardar
resposta por outro canal).

## Decisão

Comunicação **síncrona via HTTP** (`fetch` nativo do Node, sem client HTTP externo) — a Lambda chama
`GET /api/internal/customers/{document}` e aguarda a resposta antes de decidir se assina o token.

## Alternativas consideradas

- **Fila (SQS) + callback/polling**: a Lambda publicaria uma mensagem e esperaria uma resposta por outro canal
  (ex: WebSocket, polling num segundo endpoint). Adiciona complexidade e latência sem benefício claro para este
  caso de uso.
- **Replicação do dado de status do cliente para a Lambda** (ex: via evento de mudança de status, mantendo uma
  cópia local): eliminaria a chamada de rede síncrona, mas introduziria um problema de consistência eventual —
  um cliente marcado `INACTIVE` no backend poderia continuar autenticando na Lambda até a réplica atualizar.

## Justificativa

- **Natureza request-response do caso de uso**: o cliente que chama `POST /authenticate` está esperando uma
  resposta imediata (o token, ou um erro) na mesma interação HTTP — não há um fluxo de negócio de "autenticar
  agora, receber o resultado depois".
- **Simplicidade de implementação e depuração**: um projeto de estudo com prazo de entrega definido se beneficia
  de manter o caminho mais simples que atende ao requisito, evitando infraestrutura de mensageria adicional
  (fila, mais um componente a provisionar, monitorar e documentar) sem necessidade comprovada.
- **Volume esperado baixo**: o número de autenticações concorrentes esperado para este projeto não justifica a
  complexidade de desacoplar via fila — a latência adicional de uma chamada HTTP síncrona (dentro da mesma
  região AWS) é aceitável.

## Consequências

- **Acoplamento de disponibilidade**: se o backend estiver fora do ar ou inacessível pela rede, a autenticação
  via CPF falha imediatamente (erro 500) — não há fallback nem fila de retry.
- **Latência da Lambda inclui a latência do backend**: o tempo de resposta de `POST /authenticate` é a soma do
  tempo de validação local + round-trip HTTP até o backend + tempo de resposta do backend.
- Se o volume de autenticações crescer significativamente no futuro (fora do escopo atual do projeto), esta
  decisão deveria ser revisitada.
