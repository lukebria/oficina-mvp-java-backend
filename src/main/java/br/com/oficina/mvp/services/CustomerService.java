package br.com.oficina.mvp.services;

import br.com.oficina.mvp.dtos.CustomerRequestDto;
import br.com.oficina.mvp.dtos.CustomerResponseDto;
import br.com.oficina.mvp.domains.Customer;
import br.com.oficina.mvp.infra.CustomerRepository;
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
    public List<CustomerResponseDto> list() {
        return customers.findAll()
                .stream()
                .map(CustomerResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponseDto findById(Long id) {
        return CustomerResponseDto.from(findEntity(id));
    }

    @Transactional
    public CustomerResponseDto create(CustomerRequestDto request) {
        var document = DocumentValidator.requireValid(request.document());
        return CustomerResponseDto.from(customers.save(new Customer(request.name(), document, request.email(), request.phone())));
    }

    @Transactional
    public CustomerResponseDto update(Long id, CustomerRequestDto request) {
        var customer = findEntity(id);
        var document = DocumentValidator.requireValid(request.document());
        customer.update(request.name(), document, request.email(), request.phone());
        return CustomerResponseDto.from(customer);
    }

    @Transactional
    public void delete(Long id) {
        customers.delete(findEntity(id));
    }

    public Customer findEntity(Long id) {
        return customers.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado.", HttpStatus.NOT_FOUND));
    }
}
