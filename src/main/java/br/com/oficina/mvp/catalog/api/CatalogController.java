package br.com.oficina.mvp.catalog.api;

import br.com.oficina.mvp.catalog.api.dto.ServiceCatalogItemRequest;
import br.com.oficina.mvp.catalog.api.dto.ServiceCatalogItemResponse;
import br.com.oficina.mvp.catalog.application.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class CatalogController {
    private final CatalogService service;

    public CatalogController(CatalogService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lista serviços do catálogo")
    public List<ServiceCatalogItemResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha serviço")
    public ServiceCatalogItemResponse findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria serviço")
    public ServiceCatalogItemResponse create(@RequestBody @Valid ServiceCatalogItemRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza serviço")
    public ServiceCatalogItemResponse update(@PathVariable Long id, @RequestBody @Valid ServiceCatalogItemRequest request) { return service.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove serviço")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
