package br.com.oficina.mvp.vehicles.api;

import br.com.oficina.mvp.vehicles.api.dto.VehicleRequest;
import br.com.oficina.mvp.vehicles.api.dto.VehicleResponse;
import br.com.oficina.mvp.vehicles.application.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleService service;

    public VehicleController(VehicleService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lista veículos")
    public List<VehicleResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha veículo")
    public VehicleResponse findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria veículo")
    public VehicleResponse create(@RequestBody @Valid VehicleRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza veículo")
    public VehicleResponse update(@PathVariable Long id, @RequestBody @Valid VehicleRequest request) { return service.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove veículo")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
