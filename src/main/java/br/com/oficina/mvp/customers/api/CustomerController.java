package br.com.oficina.mvp.customers.api;

import br.com.oficina.mvp.customers.api.dto.CustomerRequest;
import br.com.oficina.mvp.customers.api.dto.CustomerResponse;
import br.com.oficina.mvp.customers.application.CustomerService;
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
    private final CustomerService service;

    public CustomerController(CustomerService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lista clientes")
    public List<CustomerResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha cliente")
    public CustomerResponse findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria cliente")
    public CustomerResponse create(@RequestBody @Valid CustomerRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza cliente")
    public CustomerResponse update(@PathVariable Long id, @RequestBody @Valid CustomerRequest request) { return service.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove cliente")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
