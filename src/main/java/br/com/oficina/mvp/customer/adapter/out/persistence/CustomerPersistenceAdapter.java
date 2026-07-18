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
        return jpaRepository.findAll();
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Customer> findByDocument(String document) {
        return jpaRepository.findByDocument(document);
    }

    @Override
    public Customer save(Customer customer) {
        return jpaRepository.save(customer);
    }

    @Override
    public void delete(Customer customer) {
        jpaRepository.delete(customer);
    }
}
