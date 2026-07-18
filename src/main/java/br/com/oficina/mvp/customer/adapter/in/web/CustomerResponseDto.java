package br.com.oficina.mvp.customer.adapter.in.web;

import br.com.oficina.mvp.customer.domain.Customer;
import java.time.OffsetDateTime;

public record CustomerResponseDto(
        Long id,
        String name,
        String document,
        String email,
        String phone,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static CustomerResponseDto from(Customer customer) {
        return new CustomerResponseDto(
                customer.getId(), customer.getName(), customer.getDocument(), customer.getEmail(), customer.getPhone(),
                customer.getCreatedAt(), customer.getUpdatedAt()
        );
    }
}
