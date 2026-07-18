package br.com.oficina.mvp.controllers;

import br.com.oficina.mvp.dtos.ServiceCatalogItemRequestDto;
import br.com.oficina.mvp.dtos.ServiceCatalogItemResponseDto;
import br.com.oficina.mvp.services.CatalogService;
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
@RequestMapping("/api/services")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) { this.catalogService = catalogService; }

    @GetMapping
    @Operation(summary = "Lista serviços do catálogo")
    public List<ServiceCatalogItemResponseDto> list() { return catalogService.list(); }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha serviço")
    public ServiceCatalogItemResponseDto findById(@PathVariable Long id) { return catalogService.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria serviço")
    public ServiceCatalogItemResponseDto create(@RequestBody @Valid ServiceCatalogItemRequestDto request) { return catalogService.create(request); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza serviço")
    public ServiceCatalogItemResponseDto update(@PathVariable Long id, @RequestBody @Valid ServiceCatalogItemRequestDto request) { return catalogService.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove serviço")
    public void delete(@PathVariable Long id) { catalogService.delete(id); }
}
