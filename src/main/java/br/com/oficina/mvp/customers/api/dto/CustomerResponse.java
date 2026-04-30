package br.com.oficina.mvp.customers.api.dto;

import br.com.oficina.mvp.customers.domain.Customer;
import java.time.OffsetDateTime;

public record CustomerResponse(
        Long id,
        String name,
        String document,
        String email,
        String phone,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(), customer.getName(), customer.getDocument(), customer.getEmail(), customer.getPhone(),
                customer.getCreatedAt(), customer.getUpdatedAt()
        );
    }
}
