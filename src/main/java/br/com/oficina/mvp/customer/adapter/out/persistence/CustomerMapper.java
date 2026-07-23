package br.com.oficina.mvp.customer.adapter.out.persistence;

import br.com.oficina.mvp.customer.domain.Customer;

public final class CustomerMapper {
    private CustomerMapper() {}

    public static Customer toDomain(CustomerJpaEntity entity) {
        return new Customer(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getName(),
                entity.getDocument(),
                entity.getEmail(),
                entity.getPhone()
        );
    }

    static CustomerJpaEntity toNewEntity(Customer domain) {
        return new CustomerJpaEntity(domain.getName(), domain.getDocument(), domain.getEmail(), domain.getPhone());
    }

    static void applyToEntity(Customer domain, CustomerJpaEntity entity) {
        entity.setName(domain.getName());
        entity.setDocument(domain.getDocument());
        entity.setEmail(domain.getEmail());
        entity.setPhone(domain.getPhone());
    }
}
