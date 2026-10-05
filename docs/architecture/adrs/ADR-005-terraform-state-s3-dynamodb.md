# ADR-005 — Backend do Terraform state: S3 + DynamoDB lock

| | |
|---|---|
| **Status** | Aceito |
| **Data** | 2026-09-26 |

## Contexto

O repositório `oficina-mvp-infra-iac` já usava backend S3 para o state do Terraform, mas sem tabela de lock —
duas execuções simultâneas (ex: um `apply` manual e um disparo de pipeline ao mesmo tempo) podiam corromper o
state. Era preciso decidir como resolver isso para os três repositórios de infraestrutura do projeto
(`oficina-mvp-infra-iac`, `oficina-mvp-infra-db`, `oficina-auth-function`).

## Decisão

**Manter S3** como backend e **adicionar uma tabela DynamoDB** (`oficina-mvp-infra-iac-tf-lock`) para lock,
compartilhada entre os três repositórios (cada um com sua própria `key` no mesmo bucket, evitando colisão — o
`LockID` do DynamoDB inclui bucket+key).

## Alternativas consideradas

- **Migrar para Terraform Cloud**: o plano free do Terraform Cloud cobre esse volume de recursos sem custo, mas
  exige mais esforço de configuração (criar workspace, ajustar autenticação nos workflows de CI para usar um
  token do Terraform Cloud em vez de credenciais AWS diretas).

## Justificativa

- **Menor esforço de mudança**: manter S3 (já em uso) e só adicionar uma tabela de lock é uma mudança pequena e
  incremental — não exige reconfigurar nenhum pipeline de CI/CD para autenticar contra um serviço externo novo.
- **Custo previsível e mínimo**: o DynamoDB tem free tier permanente (não só de 12 meses) que cobre folgado uma
  tabela pequena de lock (`billing_mode = PAY_PER_REQUEST`, um item por lock ativo) — mesmo que a conta de AWS
  Academy Learner Lab não seja elegível ao free tier tradicional, o volume de uso aqui é desprezível.
- **Compartilhar a tabela entre repositórios** evita provisionar (e depois ter que lembrar de manter) três
  tabelas idênticas para o mesmo propósito.

## Consequências

- **Bootstrap em duas fases necessário** (aplicável só ao `oficina-mvp-infra-iac`, que já tinha state antes
  desta decisão): a tabela precisa existir antes de ser referenciada no bloco `backend "s3"` — referenciar antes
  dela existir quebra `terraform init`/`plan` por falta de lock. Fase 1: criar a tabela usando o backend atual
  (sem lock). Fase 2 (manual, depois do primeiro `apply` da Fase 1): adicionar `dynamodb_table` ao `backends.tf`
  e rodar `terraform init -migrate-state`.
- **Dependência de ordem entre repositórios**: `oficina-mvp-infra-db` e `oficina-auth-function` já nasceram
  referenciando a tabela compartilhada diretamente (sem o problema de bootstrap, por não terem state prévio) —
  mas isso significa que o primeiro `terraform init` desses dois repositórios só funciona depois que a Fase 1 do
  `oficina-mvp-infra-iac` já tiver sido aplicada e a tabela existir de fato na conta.

## Adendo — deploy real (2026-10-04/05)

- **Bucket renomeado**: o bucket original (`oficina-mvp-infra-iac`) pertencia a **outra conta AWS** (nomes de bucket
  são globais), e o `terraform init` falhava. O state passou para **`oficina-mvp-tfstate-536036031274`** (sufixo =
  ID da conta), criado uma vez pela CLI, privado, versionado e criptografado. A decisão (S3 + DynamoDB) não muda.
- **Lock na prática**: `oficina-mvp-infra-db` e `oficina-auth-function` usam a tabela de lock normalmente. No
  `oficina-mvp-infra-iac` a Fase 2 não foi feita: como a tabela é criada e destruída junto com o próprio ambiente
  (que é recriado do zero a cada janela de uso), travar o state do repo que a cria seria circular. O risco
  (dois `apply` simultâneos no infra-iac) é mitigado pela chave `DEPLOY_ENABLED` e pela execução sequencial
  descrita no runbook do projeto.
