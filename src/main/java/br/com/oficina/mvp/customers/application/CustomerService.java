package br.com.oficina.mvp.customers.application;

import br.com.oficina.mvp.customers.api.dto.CustomerRequest;
import br.com.oficina.mvp.customers.api.dto.CustomerResponse;
import br.com.oficina.mvp.customers.domain.Customer;
import br.com.oficina.mvp.customers.infra.CustomerRepository;
import br.com.oficina.mvp.shared.exception.BusinessException;
import br.com.oficina.mvp.shared.validation.DocumentValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    private final CustomerRepository customers;

    public CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> list() {
        return customers.findAll()
                .stream()
                .map(CustomerResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return CustomerResponse.from(findEntity(id));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        var document = DocumentValidator.normalize(request.document());
        validateDocument(document);
        return CustomerResponse.from(customers.save(new Customer(request.name(), document, request.email(), request.phone())));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        var customer = findEntity(id);
        var document = DocumentValidator.normalize(request.document());
        validateDocument(document);
        customer.update(request.name(), document, request.email(), request.phone());
        return CustomerResponse.from(customer);
    }

    @Transactional
    public void delete(Long id) {
        customers.delete(findEntity(id));
    }

    public Customer findEntity(Long id) {
        return customers.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado.", HttpStatus.NOT_FOUND));
    }

    private void validateDocument(String document) {
        if (!DocumentValidator.isValidCpfOrCnpj(document)) {
            throw new BusinessException("CPF/CNPJ inválido.", HttpStatus.BAD_REQUEST);
        }
    }
}
