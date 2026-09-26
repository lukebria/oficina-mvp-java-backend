# RFC-003 — Estratégia de autenticação (fluxo público via CPF)

| | |
|---|---|
| **Status** | Aceito (já implementado) |
| **Data** | 2026-09-26 (documentado retroativamente — a implementação é anterior a este RFC) |
| **Autores** | Equipe do projeto (registrado por Claude a pedido de Lucas) |

## Contexto

O enunciado exige: proteger rotas sensíveis com autenticação via CPF, através de uma Function Serverless que (1)
valida o CPF do cliente, (2) consulta existência/status do cliente na base de dados, e (3) gera/devolve um JWT
válido para consumo das APIs protegidas.

## Opções consideradas

1. **Function serverless consulta o backend via HTTP interno**, sem tocar no banco diretamente — a function não
   conhece o schema, só um contrato HTTP versionado (`GET /api/internal/customers/{document}`).
2. **Function serverless acessa o banco de dados diretamente** (mesma connection string do backend) — evitaria
   uma chamada de rede a mais, mas acopla a function ao schema físico do banco.
3. **O próprio backend valida o CPF e assina o token**, sem nenhuma function serverless — mais simples, mas não
   atende ao requisito explícito do enunciado ("Criar uma Function Serverless para...").

## Decisão

**Opção 1**: a function (`oficina-auth-function`) valida o CPF/CNPJ localmente (mesmo algoritmo do backend,
`DocumentValidator`/`documentValidator.ts`), consulta `GET /api/internal/customers/{document}` no backend
(autenticado por `X-Internal-Api-Key`, não JWT) e, se o cliente existir e estiver `ACTIVE`, assina um JWT HS256
com um segredo dedicado (`CUSTOMER_JWT_SECRET`, diferente do `JWT_SECRET` administrativo).

## Justificativa

- **Desacoplamento de schema**: a function nunca precisa saber como `customers` é modelada no banco — só o
  contrato HTTP (`found`/`customerId`/`status`). Uma mudança de schema no backend não quebra a function, desde
  que o contrato do endpoint interno seja mantido.
- **Segredo dedicado**: usar uma chave JWT diferente da administrativa limita o raio de impacto se a function
  (rodando fora do controle direto do time de backend, num repositório/deploy separado) for comprometida — o
  atacante não ganharia acesso a rotas administrativas.
- **Revalidação a cada request**: como o backend confere `Customer.status` a cada chamada às rotas protegidas
  (não só no momento em que o token foi emitido), um cliente marcado `INACTIVE` depois de já ter um token válido
  perde acesso imediatamente — sem essa revalidação, o desenho seria mais fraco independente de qual opção de
  arquitetura fosse escolhida.

## Consequências

- **Acoplamento por contrato HTTP, não por schema**: qualquer mudança nos campos do endpoint interno
  (`/api/internal/customers/{document}`) ou nas claims/segredo do JWT precisa ser coordenada entre os dois
  repositórios (`oficina-mvp-java-backend` e `oficina-auth-function`) — não há checagem automática de
  compatibilidade entre eles hoje (nenhum contrato OpenAPI compartilhado, nenhum teste de contrato).
- **Comunicação síncrona em runtime**: a function depende do backend estar no ar e acessível pela rede no
  momento da autenticação — ver ADR-001 para a decisão de manter isso síncrono (não assíncrono/fila).
- **Duplicação da regra de validação de CPF/CNPJ**: a mesma regra (mod-11) existe em duas linguagens
  (`DocumentValidator.java` e `documentValidator.ts`), mantidas manualmente em sincronia — risco de drift já
  identificado na auditoria do repositório, sem teste cruzado entre as duas implementações até o momento deste
  RFC.
