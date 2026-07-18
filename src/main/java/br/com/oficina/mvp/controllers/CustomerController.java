package br.com.oficina.mvp.controllers;

import br.com.oficina.mvp.dtos.CustomerRequestDto;
import br.com.oficina.mvp.dtos.CustomerResponseDto;
import br.com.oficina.mvp.services.CustomerService;
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
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) { this.customerService = customerService; }

    @GetMapping
    @Operation(summary = "Lista clientes")
    public List<CustomerResponseDto> list() { return customerService.list(); }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha cliente")
    public CustomerResponseDto findById(@PathVariable Long id) { return customerService.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria cliente")
    public CustomerResponseDto create(@RequestBody @Valid CustomerRequestDto request) { return customerService.create(request); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza cliente")
    public CustomerResponseDto update(@PathVariable Long id, @RequestBody @Valid CustomerRequestDto request) { return customerService.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove cliente")
    public void delete(@PathVariable Long id) { customerService.delete(id); }
}
