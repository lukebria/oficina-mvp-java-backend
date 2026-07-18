package br.com.oficina.mvp.controllers;

import br.com.oficina.mvp.dtos.PartRequestDto;
import br.com.oficina.mvp.dtos.PartResponseDto;
import br.com.oficina.mvp.services.PartService;
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
@RequestMapping("/api/parts")
public class PartController {
    private final PartService partService;

    public PartController(PartService partService) { this.partService = partService; }

    @GetMapping
    @Operation(summary = "Lista peças/insumos")
    public List<PartResponseDto> list() { return partService.list(); }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha peça/insumo")
    public PartResponseDto findById(@PathVariable Long id) { return partService.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria peça/insumo")
    public PartResponseDto create(@RequestBody @Valid PartRequestDto request) { return partService.create(request); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza peça/insumo")
    public PartResponseDto update(@PathVariable Long id, @RequestBody @Valid PartRequestDto request) { return partService.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove peça/insumo")
    public void delete(@PathVariable Long id) { partService.delete(id); }
}
