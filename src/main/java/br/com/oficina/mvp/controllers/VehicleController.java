package br.com.oficina.mvp.controllers;

import br.com.oficina.mvp.dtos.VehicleRequestDto;
import br.com.oficina.mvp.dtos.VehicleResponseDto;
import br.com.oficina.mvp.services.VehicleService;
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
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) { this.vehicleService = vehicleService; }

    @GetMapping
    @Operation(summary = "Lista veículos")
    public List<VehicleResponseDto> list() { return vehicleService.list(); }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha veículo")
    public VehicleResponseDto findById(@PathVariable Long id) { return vehicleService.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria veículo")
    public VehicleResponseDto create(@RequestBody @Valid VehicleRequestDto request) { return vehicleService.create(request); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza veículo")
    public VehicleResponseDto update(@PathVariable Long id, @RequestBody @Valid VehicleRequestDto request) { return vehicleService.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove veículo")
    public void delete(@PathVariable Long id) { vehicleService.delete(id); }
}
