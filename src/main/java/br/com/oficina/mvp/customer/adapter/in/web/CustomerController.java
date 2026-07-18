package br.com.oficina.mvp.customer.adapter.in.web;

import br.com.oficina.mvp.customer.application.port.in.CustomerUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerUseCase customerUseCase;

    public CustomerController(CustomerUseCase customerUseCase) { this.customerUseCase = customerUseCase; }

    @GetMapping
    @Operation(summary = "Lista clientes")
    public List<CustomerResponseDto> list() {
        return customerUseCase.list().stream().map(CustomerResponseDto::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha cliente")
    public CustomerResponseDto findById(@PathVariable Long id) {
        return CustomerResponseDto.from(customerUseCase.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria cliente")
    public CustomerResponseDto create(@RequestBody @Valid CustomerRequestDto request) {
        return CustomerResponseDto.from(customerUseCase.create(request.toCommand()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza cliente")
    public CustomerResponseDto update(@PathVariable Long id, @RequestBody @Valid CustomerRequestDto request) {
        return CustomerResponseDto.from(customerUseCase.update(id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove cliente")
    public void delete(@PathVariable Long id) { customerUseCase.delete(id); }
}
