package br.com.oficina.mvp.serviceorder.adapter.in.web;

import br.com.oficina.mvp.serviceorder.application.port.in.ServiceOrderUseCase;
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
    private final ServiceOrderUseCase serviceOrderUseCase;

    public ServiceOrderController(ServiceOrderUseCase serviceOrderUseCase) {
        this.serviceOrderUseCase = serviceOrderUseCase;
    }

    @GetMapping
    @Operation(summary = "Lista ordens de serviço")
    public List<ServiceOrderResponseDto> list() {
        return serviceOrderUseCase.list().stream().map(ServiceOrderResponseDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria ordem de serviço e gera orçamento automaticamente")
    public ServiceOrderResponseDto create(@RequestBody @Valid CreateServiceOrderRequestDto request) {
        return ServiceOrderResponseDto.from(serviceOrderUseCase.create(request.toCommand()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha ordem de serviço")
    public ServiceOrderResponseDto findById(@PathVariable Long id) {
        return ServiceOrderResponseDto.from(serviceOrderUseCase.findById(id));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Aprova orçamento da OS via fluxo administrativo")
    public ServiceOrderResponseDto approve(@PathVariable Long id, @RequestBody(required = false) CustomerApprovalRequestDto request) {
        return ServiceOrderResponseDto.from(serviceOrderUseCase.approve(id, request == null ? null : request.comment()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza status da OS respeitando a política de transição")
    public ServiceOrderResponseDto updateStatus(@PathVariable Long id, @RequestBody @Valid UpdateStatusRequestDto request) {
        return ServiceOrderResponseDto.from(serviceOrderUseCase.updateStatus(id, request.status(), request.comment()));
    }
}
