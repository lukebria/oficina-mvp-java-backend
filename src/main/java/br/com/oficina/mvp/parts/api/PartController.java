package br.com.oficina.mvp.parts.api;

import br.com.oficina.mvp.parts.api.dto.PartRequest;
import br.com.oficina.mvp.parts.api.dto.PartResponse;
import br.com.oficina.mvp.parts.application.PartService;
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
    public List<PartResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha peça/insumo")
    public PartResponse findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria peça/insumo")
    public PartResponse create(@RequestBody @Valid PartRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza peça/insumo")
    public PartResponse update(@PathVariable Long id, @RequestBody @Valid PartRequest request) { return service.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove peça/insumo")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
