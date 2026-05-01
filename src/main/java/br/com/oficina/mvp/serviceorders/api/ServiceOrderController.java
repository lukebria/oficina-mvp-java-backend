package br.com.oficina.mvp.serviceorders.api;

import br.com.oficina.mvp.serviceorders.api.dto.CreateServiceOrderRequest;
import br.com.oficina.mvp.serviceorders.api.dto.CustomerApprovalRequest;
import br.com.oficina.mvp.serviceorders.api.dto.ServiceOrderResponse;
import br.com.oficina.mvp.serviceorders.api.dto.UpdateStatusRequest;
import br.com.oficina.mvp.serviceorders.application.ServiceOrderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-orders")
public class ServiceOrderController {
    private final ServiceOrderApplicationService service;

    public ServiceOrderController(ServiceOrderApplicationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista ordens de serviço")
    public List<ServiceOrderResponse> list() { return service.list(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria ordem de serviço e gera orçamento automaticamente")
    public ServiceOrderResponse create(@RequestBody @Valid CreateServiceOrderRequest request) { return service.create(request); }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha ordem de serviço")
    public ServiceOrderResponse findById(@PathVariable Long id) { return service.findById(id); }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Aprova orçamento da OS via fluxo administrativo")
    public ServiceOrderResponse approve(@PathVariable Long id, @RequestBody(required = false) CustomerApprovalRequest request) {
        return service.approve(id, request == null ? null : request.comment());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza status da OS respeitando a política de transição")
    public ServiceOrderResponse updateStatus(@PathVariable Long id, @RequestBody @Valid UpdateStatusRequest request) {
        return service.updateStatus(id, request.status(), request.comment());
    }
}
