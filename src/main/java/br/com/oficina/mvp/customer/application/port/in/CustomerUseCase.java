package br.com.oficina.mvp.customer.application.port.in;

import br.com.oficina.mvp.customer.domain.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerUseCase {
    List<Customer> list();

    Customer findById(Long id);

    Optional<Customer> findByDocument(String document);

    Customer create(CustomerCommand command);

    Customer update(Long id, CustomerCommand command);

    void delete(Long id);
}
