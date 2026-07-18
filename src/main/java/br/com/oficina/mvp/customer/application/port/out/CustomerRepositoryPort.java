package br.com.oficina.mvp.customer.application.port.out;

import br.com.oficina.mvp.customer.domain.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepositoryPort {
    List<Customer> findAll();

    Optional<Customer> findById(Long id);

    Optional<Customer> findByDocument(String document);

    Customer save(Customer customer);

    void delete(Customer customer);
}
