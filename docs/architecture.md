# Arquitetura — Oficina MVP Backend Java 25

## Visão geral

O projeto é um monolito modular em camadas, usando DDD.

## Módulos

- `auth`: usuário administrativo, login e JWT.
- `customers`: cliente, CPF/CNPJ e dados de contato.
- `vehicles`: veículo vinculado ao cliente.
- `catalog`: catálogo de serviços da oficina.
- `parts`: peças e insumos com controle de estoque.
- `serviceorders`: ordem de serviço, orçamento, aprovação, transição de status e histórico.
- `shared`: configurações, segurança, validações e tratamento de erro.

## Camadas por módulo

```txt
module/
  domain/       entidades e regras de domínio
  application/  casos de uso e orquestração
  infra/        repositórios Spring Data JPA
  api/          controllers REST e DTOs
```

## Domínio crítico

O domínio crítico é a Ordem de Serviço:

1. cria OS com cliente e veículo;
2. calcula orçamento com serviços e peças;
3. cria histórico inicial;
4. envia para aprovação;
5. valida estoque antes da aprovação;
6. baixa estoque;
7. controla transições de status;
8. registra histórico de cada mudança.

## Fluxo de status

```txt
RECEBIDA
  -> EM_DIAGNOSTICO
  -> AGUARDANDO_APROVACAO
  -> EM_EXECUCAO
  -> FINALIZADA
  -> ENTREGUE
```

Também é permitido que a criação da OS vá diretamente de `RECEBIDA` para `AGUARDANDO_APROVACAO`, porque o MVP já calcula o orçamento automaticamente na criação.

## Segurança

- Login administrativo: `/api/auth/login`.
- JWT Bearer para rotas administrativas.
- Rotas públicas liberadas:
  - `/api/public/**`
  - `/api/health`
  - `/swagger-ui.html`
  - `/v3/api-docs/**`

## Banco

O banco é PostgreSQL, versionado por Flyway. O modelo é relacional:

- `users`
- `customers`
- `vehicles`
- `service_catalog_items`
- `parts`
- `service_orders`
- `work_order_services`
- `work_order_parts`
- `service_order_status_history`
