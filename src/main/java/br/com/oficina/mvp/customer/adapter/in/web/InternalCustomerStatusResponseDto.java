package br.com.oficina.mvp.customer.adapter.in.web;

import br.com.oficina.mvp.customer.domain.Customer;

/**
 * Resposta do endpoint interno consumido pela Function Serverless externa (autenticação via CPF) para checar
 * existência/status do cliente antes de emitir o token — ver docs/architecture.md, seção 5. "status" é
 * {@code ACTIVE}/{@code INACTIVE} (persistido em {@code Customer.status}) quando encontrado, ou
 * {@code NOT_FOUND} — um valor só de resposta, nunca persistido — quando o documento não existe.
 */
public record InternalCustomerStatusResponseDto(boolean found, Long customerId, String name, String status) {
    public static InternalCustomerStatusResponseDto found(Customer customer) {
        return new InternalCustomerStatusResponseDto(true, customer.getId(), customer.getName(), customer.getStatus().name());
    }

    public static InternalCustomerStatusResponseDto notFound() {
        return new InternalCustomerStatusResponseDto(false, null, null, "NOT_FOUND");
    }
}
