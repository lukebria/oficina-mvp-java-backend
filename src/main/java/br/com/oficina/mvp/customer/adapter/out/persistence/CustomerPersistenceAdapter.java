package br.com.oficina.mvp.customer.adapter.out.persistence;

import br.com.oficina.mvp.customer.application.port.out.CustomerRepositoryPort;
import br.com.oficina.mvp.customer.domain.Customer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class CustomerPersistenceAdapter implements CustomerRepositoryPort {
    private final CustomerJpaRepository jpaRepository;

    CustomerPersistenceAdapter(CustomerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Customer> findAll() {
        return jpaRepository.findAll().stream().map(CustomerMapper::toDomain).toList();
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return jpaRepository.findById(id).map(CustomerMapper::toDomain);
    }

    @Override
    public Optional<Customer> findByDocument(String document) {
        return jpaRepository.findByDocument(document).map(CustomerMapper::toDomain);
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity entity;
        if (customer.getId() == null) {
            entity = CustomerMapper.toNewEntity(customer);
        } else {
            entity = jpaRepository.getReferenceById(customer.getId());
            CustomerMapper.applyToEntity(customer, entity);
        }
        return CustomerMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public void delete(Customer customer) {
        jpaRepository.deleteById(customer.getId());
    }
}
