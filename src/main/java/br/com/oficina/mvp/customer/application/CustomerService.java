package br.com.oficina.mvp.customer.application;

import br.com.oficina.mvp.customer.application.port.in.CustomerCommand;
import br.com.oficina.mvp.customer.application.port.in.CustomerUseCase;
import br.com.oficina.mvp.customer.application.port.out.CustomerRepositoryPort;
import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.customer.domain.CustomerStatus;
import br.com.oficina.mvp.shared.exception.BusinessException;
import br.com.oficina.mvp.shared.validation.DocumentValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService implements CustomerUseCase {
    private final CustomerRepositoryPort customers;

    public CustomerService(CustomerRepositoryPort customers) {
        this.customers = customers;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> list() {
        return customers.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return findEntity(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByDocument(String document) {
        return customers.findByDocument(DocumentValidator.normalize(document));
    }

    @Override
    @Transactional
    public Customer create(CustomerCommand command) {
        var document = DocumentValidator.requireValid(command.document());
        var status = command.status() != null ? command.status() : CustomerStatus.ACTIVE;
        return customers.save(new Customer(command.name(), document, command.email(), command.phone(), status));
    }

    @Override
    @Transactional
    public Customer update(Long id, CustomerCommand command) {
        var customer = findEntity(id);
        var document = DocumentValidator.requireValid(command.document());
        customer.update(command.name(), document, command.email(), command.phone(), command.status());
        customers.save(customer);
        return customer;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        customers.delete(findEntity(id));
    }

    private Customer findEntity(Long id) {
        return customers.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado.", HttpStatus.NOT_FOUND));
    }
}
