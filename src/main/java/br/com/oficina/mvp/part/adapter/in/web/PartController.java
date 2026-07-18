package br.com.oficina.mvp.part.adapter.in.web;

import br.com.oficina.mvp.part.application.port.in.PartUseCase;
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
    private final PartUseCase partUseCase;

    public PartController(PartUseCase partUseCase) { this.partUseCase = partUseCase; }

    @GetMapping
    @Operation(summary = "Lista peças/insumos")
    public List<PartResponseDto> list() {
        return partUseCase.list().stream().map(PartResponseDto::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha peça/insumo")
    public PartResponseDto findById(@PathVariable Long id) {
        return PartResponseDto.from(partUseCase.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria peça/insumo")
    public PartResponseDto create(@RequestBody @Valid PartRequestDto request) {
        return PartResponseDto.from(partUseCase.create(request.toCommand()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza peça/insumo")
    public PartResponseDto update(@PathVariable Long id, @RequestBody @Valid PartRequestDto request) {
        return PartResponseDto.from(partUseCase.update(id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove peça/insumo")
    public void delete(@PathVariable Long id) { partUseCase.delete(id); }
}
