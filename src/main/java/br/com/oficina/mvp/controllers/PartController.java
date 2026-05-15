package br.com.oficina.mvp.controllers;

import br.com.oficina.mvp.dtos.PartRequestDto;
import br.com.oficina.mvp.dtos.PartResponseDto;
import br.com.oficina.mvp.services.PartService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
public class PartController {
    private final PartService service;

    public PartController(PartService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lista peças/insumos")
    public List<PartResponseDto> list() { return service.list(); }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha peça/insumo")
    public PartResponseDto findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria peça/insumo")
    public PartResponseDto create(@RequestBody @Valid PartRequestDto request) { return service.create(request); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza peça/insumo")
    public PartResponseDto update(@PathVariable Long id, @RequestBody @Valid PartRequestDto request) { return service.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove peça/insumo")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
