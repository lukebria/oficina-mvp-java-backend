package br.com.oficina.mvp.customer.adapter.in.web;

import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.customer.domain.CustomerStatus;
import java.time.OffsetDateTime;

public record CustomerResponseDto(
        Long id,
        String name,
        String document,
        String email,
        String phone,
        CustomerStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static CustomerResponseDto from(Customer customer) {
        return new CustomerResponseDto(
                customer.getId(), customer.getName(), customer.getDocument(), customer.getEmail(), customer.getPhone(),
                customer.getStatus(), customer.getCreatedAt(), customer.getUpdatedAt()
        );
    }
}
