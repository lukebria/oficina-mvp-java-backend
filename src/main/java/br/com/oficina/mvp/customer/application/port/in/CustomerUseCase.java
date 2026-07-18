package br.com.oficina.mvp.customer.application.port.in;

import br.com.oficina.mvp.customer.domain.Customer;

import java.util.List;

public interface CustomerUseCase {
    List<Customer> list();

    Customer findById(Long id);

    Customer create(CustomerCommand command);

    Customer update(Long id, CustomerCommand command);

    void delete(Long id);
}
