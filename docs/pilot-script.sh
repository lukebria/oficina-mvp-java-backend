#!/usr/bin/env bash
#
# Roteiro executável do piloto: login -> cadastro de cliente -> cadastro de veículo ->
# cadastro do serviço de catálogo -> cadastro da peça -> criação da ordem de serviço ->
# aprovação pelo cliente -> finalização -> entrega.
#
# Uso (Git Bash / WSL / Linux / macOS):
#   bash docs/pilot-script.sh
#
# Variáveis de ambiente aceitas (todas têm valor padrão):
#   BASE_URL             endereço da API                          (default: http://localhost:3000)
#   SEED_ADMIN_EMAIL      email do admin semeado                   (default: admin@oficina.com)
#   SEED_ADMIN_PASSWORD   senha do admin semeado                   (default: Admin@123)
#   CUSTOMER_EMAIL         email real do cliente da OS — é para essa
#                          caixa que a API envia a notificação a cada
#                          transição de status (RECUSADA não notifica).
#                          Troque para a caixa de quem for validar o
#                          recebimento durante a gravação.            (default: lucas.bria@gmail.com)
#
# Exemplo apontando para outro ambiente e outro email de verificação:
#   BASE_URL=https://piloto.oficina.exemplo.com CUSTOMER_EMAIL=voce@exemplo.com bash docs/pilot-script.sh
#
# Envio real de email exige que o ambiente tenha MAIL_HOST/MAIL_USERNAME/MAIL_PASSWORD
# configurados (ver README — Variáveis de ambiente). Sem isso a API loga a falha de envio
# mas o fluxo de status continua normalmente (não é bloqueante).

set -uo pipefail

BASE_URL="${BASE_URL:-http://localhost:3000}"
ADMIN_EMAIL="${SEED_ADMIN_EMAIL:-admin@oficina.com}"
ADMIN_PASSWORD="${SEED_ADMIN_PASSWORD:-Admin@123}"

# CPF válido (dígitos verificadores conferidos por DocumentValidatorTest.shouldValidateCpf).
CUSTOMER_DOCUMENT="52998224725"
CUSTOMER_NAME="Maria Cliente"
CUSTOMER_EMAIL="${CUSTOMER_EMAIL:-lucas.bria@gmail.com}" #incluir um email valido para o teste de envio de email
CUSTOMER_PHONE="11999999999"
VEHICLE_PLATE="ABC1D23"

# Sufixo único por execução: evita colidir com o "Troca de óleo" / "FILTRO-OLEO-001" já
# semeados pelo DataSeeder (nome do serviço e SKU da peça são colunas UNIQUE no banco).
RUN_SUFFIX="$(date +%s)"

if [ -t 1 ]; then
  BOLD=$'\033[1m'; CYAN=$'\033[36m'; GREEN=$'\033[32m'; RED=$'\033[31m'; YELLOW=$'\033[33m'; NC=$'\033[0m'
else
  BOLD=''; CYAN=''; GREEN=''; RED=''; YELLOW=''; NC=''
fi

step() { printf "\n%s%s==> %s%s\n" "$BOLD" "$CYAN" "$1" "$NC"; }
warn() { printf "%sAVISO: %s%s\n" "$YELLOW" "$1" "$NC"; }

# Formata a resposta JSON pra leitura. No Windows, "python"/"python3" no PATH costumam ser o stub
# da Microsoft Store (não o interpretador de verdade) e corrompem acentos por causa da codepage —
# por isso tenta várias vias (incluindo o "py" launcher) e só aceita a que realmente funcionou
# (saída não vazia), com PYTHONIOENCODING/PYTHONUTF8 forçando UTF-8. Sem nenhuma funcionando, cai
# para exibir o JSON cru (sem indentação, mas sem corromper acentuação).
pretty() {
  local input out interpreter
  input=$(cat)
  for interpreter in python3 python py; do
    if command -v "$interpreter" >/dev/null 2>&1; then
      out=$(printf '%s' "$input" | PYTHONIOENCODING=utf-8 PYTHONUTF8=1 "$interpreter" -c \
        'import sys,json
try:
    print(json.dumps(json.load(sys.stdin), indent=2, ensure_ascii=False))
except Exception:
    pass' 2>/dev/null)
      if [ -n "$out" ]; then
        printf '%s\n' "$out"
        return
      fi
    fi
  done
  printf '%s\n' "$input"
}

field()    { printf '%s' "$1" | grep -o "\"$2\":\"[^\"]*\"" | head -n1 | cut -d'"' -f4; }
numfield() { printf '%s' "$1" | grep -o "\"$2\":[0-9]*"    | head -n1 | cut -d':' -f2; }

# request METHOD PATH BODY [auth] [expect_regex]
# Preenche LAST_STATUS / LAST_BODY. Retorna 0 se status casar com expect_regex (default ^2), 1 caso contrário.
request() {
  local method="$1" path="$2" body="$3" auth="${4:-}" expect="${5:-^2}"
  local args=(-s -w '\n%{http_code}' -X "$method" "$BASE_URL$path" -H "Content-Type: application/json")
  if [ "$auth" = "auth" ]; then
    args+=(-H "Authorization: Bearer $TOKEN")
  fi
  if [ -n "$body" ]; then
    args+=(--data-binary @-)
  fi

  printf "%s %s\n" "$method" "$path"

  local raw status body_out
  # Corpo é enviado via stdin (--data-binary @-), não como argumento (-d "..."): no Git Bash /
  # curl nativo do Windows, argumentos de linha de comando com acentos (ç, ó, ã...) passam por
  # uma conversão de codepage na criação do processo e chegam corrompidos na API. Via stdin o
  # UTF-8 chega intacto.
  if [ -n "$body" ]; then
    raw=$(printf '%s' "$body" | curl "${args[@]}") || { printf "%sFALHA DE CONEXÃO com %s%s\n" "$RED" "$BASE_URL" "$NC"; return 1; }
  else
    raw=$(curl "${args[@]}") || { printf "%sFALHA DE CONEXÃO com %s%s\n" "$RED" "$BASE_URL" "$NC"; return 1; }
  fi
  status=$(printf '%s' "$raw" | tail -n1)
  body_out=$(printf '%s' "$raw" | sed '$d')

  printf '%s' "$body_out" | pretty

  LAST_STATUS="$status"
  LAST_BODY="$body_out"

  if [[ "$status" =~ $expect ]]; then
    printf "%sOK%s (HTTP %s)\n" "$GREEN" "$NC" "$status"
    return 0
  else
    printf "%sFALHOU%s (HTTP %s)\n" "$RED" "$NC" "$status"
    return 1
  fi
}

echo "Base URL: $BASE_URL"

# 1. Login ------------------------------------------------------------------
step "1. Login (admin)"
if ! request POST "/api/auth/login" \
  "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}"; then
  printf "%sLogin falhou, abortando roteiro.%s\n" "$RED" "$NC"
  exit 1
fi
TOKEN=$(field "$LAST_BODY" token)
if [ -z "$TOKEN" ]; then
  printf "%sNão foi possível extrair o token do login. Abortando.%s\n" "$RED" "$NC"
  exit 1
fi
echo "Token obtido."

# 2. Cadastro do cliente ------------------------------------------------------
step "2. Cadastro do cliente"
if request POST "/api/customers" \
  "{\"name\":\"$CUSTOMER_NAME\",\"document\":\"$CUSTOMER_DOCUMENT\",\"email\":\"$CUSTOMER_EMAIL\",\"phone\":\"$CUSTOMER_PHONE\"}" \
  auth; then
  CUSTOMER_ID=$(numfield "$LAST_BODY" id)
  echo "Cliente id=$CUSTOMER_ID"
else
  warn "cadastro de cliente não confirmado (pode já existir de uma execução anterior). Seguindo em frente — a criação da OS busca ou cria o cliente automaticamente."
fi

# 3. Cadastro do veículo -------------------------------------------------------
step "3. Cadastro do veículo"
if [ -n "${CUSTOMER_ID:-}" ]; then
  if request POST "/api/vehicles" \
    "{\"customerId\":$CUSTOMER_ID,\"plate\":\"$VEHICLE_PLATE\",\"brand\":\"Toyota\",\"model\":\"Corolla\",\"manufacturingYear\":2022}" \
    auth; then
    VEHICLE_ID=$(numfield "$LAST_BODY" id)
    echo "Veículo id=$VEHICLE_ID"
  else
    warn "cadastro de veículo não confirmado (pode já existir). Seguindo em frente — a criação da OS busca ou cria o veículo automaticamente."
  fi
else
  warn "sem id de cliente confirmado, pulando cadastro isolado de veículo (a OS cria o veículo automaticamente)."
fi

# 4. Cadastro do serviço de catálogo usado na OS ---------------------------------
step "4. Cadastro do serviço de catálogo"
SERVICE_NAME="Troca de óleo (piloto $RUN_SUFFIX)"
if request POST "/api/services" \
  "{\"name\":\"$SERVICE_NAME\",\"description\":\"Troca de óleo do motor\",\"basePrice\":180.00,\"estimatedMinutes\":60,\"active\":true}" \
  auth; then
  SERVICE_ITEM_ID=$(numfield "$LAST_BODY" id)
  echo "Serviço de catálogo id=$SERVICE_ITEM_ID"
else
  warn "cadastro do serviço de catálogo falhou. Usando id=1 (item semeado 'Troca de óleo') como fallback."
  SERVICE_ITEM_ID=1
fi

# 5. Cadastro da peça usada na OS -------------------------------------------------
step "5. Cadastro da peça"
PART_SKU="FILTRO-OLEO-$RUN_SUFFIX"
if request POST "/api/parts" \
  "{\"name\":\"Filtro de óleo (piloto $RUN_SUFFIX)\",\"sku\":\"$PART_SKU\",\"unitPrice\":45.90,\"stockQuantity\":25,\"minStock\":5,\"active\":true}" \
  auth; then
  PART_ID=$(numfield "$LAST_BODY" id)
  echo "Peça id=$PART_ID"
else
  warn "cadastro da peça falhou. Usando id=1 (peça semeada 'Filtro de óleo') como fallback."
  PART_ID=1
fi

# 6. Criação da ordem de serviço -----------------------------------------------
step "6. Criação da ordem de serviço"
if ! request POST "/api/service-orders" \
  "{\"customerDocument\":\"$CUSTOMER_DOCUMENT\",\"customer\":{\"name\":\"$CUSTOMER_NAME\",\"email\":\"$CUSTOMER_EMAIL\",\"phone\":\"$CUSTOMER_PHONE\"},\"vehicle\":{\"plate\":\"$VEHICLE_PLATE\",\"brand\":\"Toyota\",\"model\":\"Corolla\",\"manufacturingYear\":2022},\"customerNotes\":\"Barulho ao frear\",\"services\":[{\"serviceItemId\":$SERVICE_ITEM_ID,\"quantity\":1}],\"parts\":[{\"partId\":$PART_ID,\"quantity\":1}]}" \
  auth; then
  printf "%sCriação da OS falhou, abortando roteiro.%s\n" "$RED" "$NC"
  exit 1
fi
OS_ID=$(numfield "$LAST_BODY" id)
OS_CODE=$(field "$LAST_BODY" code)
if [ -z "$OS_ID" ] || [ -z "$OS_CODE" ]; then
  printf "%sNão foi possível extrair id/code da OS criada. Abortando.%s\n" "$RED" "$NC"
  exit 1
fi
echo "OS criada: id=$OS_ID code=$OS_CODE (status esperado: AGUARDANDO_APROVACAO)"

# 7. Aprovação da OS pelo cliente (pública, sem token) --------------------------
step "7. Aprovação da OS pelo cliente"
if ! request POST "/api/public/service-orders/$OS_CODE/approval" \
  "{\"document\":\"$CUSTOMER_DOCUMENT\",\"approved\":true,\"comment\":\"Aprovado pelo cliente.\"}"; then
  printf "%sAprovação da OS falhou, abortando roteiro.%s\n" "$RED" "$NC"
  exit 1
fi
echo "Status esperado: EM_EXECUCAO"

# 8. Finalização do serviço ------------------------------------------------------
step "8. Finalização do serviço"
if ! request PATCH "/api/service-orders/$OS_ID/status" \
  '{"status":"FINALIZADA","comment":"Serviço finalizado."}' \
  auth; then
  printf "%sTransição para FINALIZADA falhou, abortando roteiro.%s\n" "$RED" "$NC"
  exit 1
fi

# 9. Entrega do veículo ao cliente ------------------------------------------------
step "9. Entrega do veículo ao cliente"
if ! request PATCH "/api/service-orders/$OS_ID/status" \
  '{"status":"ENTREGUE","comment":"Veículo entregue ao cliente."}' \
  auth; then
  printf "%sTransição para ENTREGUE falhou, abortando roteiro.%s\n" "$RED" "$NC"
  exit 1
fi

# 10. Conferência final -------------------------------------------------------------
step "10. Conferência final da OS"
request GET "/api/service-orders/$OS_ID" "" auth

printf "\n%s%sRoteiro concluído com sucesso: OS %s finalizada e entregue.%s\n" "$GREEN" "$BOLD" "$OS_CODE" "$NC"
