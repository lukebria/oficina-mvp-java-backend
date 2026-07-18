package br.com.oficina.mvp.controllers;

import br.com.oficina.mvp.dtos.CreateServiceOrderRequestDto;
import br.com.oficina.mvp.dtos.CustomerApprovalRequestDto;
import br.com.oficina.mvp.dtos.ServiceOrderResponseDto;
import br.com.oficina.mvp.dtos.UpdateStatusRequestDto;
import br.com.oficina.mvp.services.ServiceOrderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/service-orders")
public class ServiceOrderController {
    private final ServiceOrderApplicationService serviceOrderService;

    public ServiceOrderController(ServiceOrderApplicationService serviceOrderService) {
        this.serviceOrderService = serviceOrderService;
    }

    @GetMapping
    @Operation(summary = "Lista ordens de serviço")
    public List<ServiceOrderResponseDto> list() { return serviceOrderService.list(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria ordem de serviço e gera orçamento automaticamente")
    public ServiceOrderResponseDto create(@RequestBody @Valid CreateServiceOrderRequestDto request) { return serviceOrderService.create(request); }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha ordem de serviço")
    public ServiceOrderResponseDto findById(@PathVariable Long id) { return serviceOrderService.findById(id); }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Aprova orçamento da OS via fluxo administrativo")
    public ServiceOrderResponseDto approve(@PathVariable Long id, @RequestBody(required = false) CustomerApprovalRequestDto request) {
        return serviceOrderService.approve(id, request == null ? null : request.comment());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza status da OS respeitando a política de transição")
    public ServiceOrderResponseDto updateStatus(@PathVariable Long id, @RequestBody @Valid UpdateStatusRequestDto request) {
        return serviceOrderService.updateStatus(id, request.status(), request.comment());
    }
}
