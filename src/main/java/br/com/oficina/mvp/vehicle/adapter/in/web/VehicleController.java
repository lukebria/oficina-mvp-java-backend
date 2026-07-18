package br.com.oficina.mvp.vehicle.adapter.in.web;

import br.com.oficina.mvp.vehicle.application.port.in.VehicleUseCase;
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
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleUseCase vehicleUseCase;

    public VehicleController(VehicleUseCase vehicleUseCase) { this.vehicleUseCase = vehicleUseCase; }

    @GetMapping
    @Operation(summary = "Lista veículos")
    public List<VehicleResponseDto> list() {
        return vehicleUseCase.list().stream().map(VehicleResponseDto::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha veículo")
    public VehicleResponseDto findById(@PathVariable Long id) {
        return VehicleResponseDto.from(vehicleUseCase.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria veículo")
    public VehicleResponseDto create(@RequestBody @Valid VehicleRequestDto request) {
        return VehicleResponseDto.from(vehicleUseCase.create(request.toCommand()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza veículo")
    public VehicleResponseDto update(@PathVariable Long id, @RequestBody @Valid VehicleRequestDto request) {
        return VehicleResponseDto.from(vehicleUseCase.update(id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove veículo")
    public void delete(@PathVariable Long id) { vehicleUseCase.delete(id); }
}
