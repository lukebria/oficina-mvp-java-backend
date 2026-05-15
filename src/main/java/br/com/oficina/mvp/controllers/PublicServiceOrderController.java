package br.com.oficina.mvp.controllers;

import br.com.oficina.mvp.dtos.CustomerApprovalRequestDto;
import br.com.oficina.mvp.dtos.PublicServiceOrderResponseDto;
import br.com.oficina.mvp.services.ServiceOrderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/public/service-orders")
public class PublicServiceOrderController {
    private final ServiceOrderApplicationService service;

    public PublicServiceOrderController(ServiceOrderApplicationService service) { this.service = service; }

    @GetMapping("/{code}")
    @Operation(summary = "Consulta pública de OS por código e CPF/CNPJ")
    public PublicServiceOrderResponseDto status(@PathVariable String code, @RequestParam @NotBlank String document) {
        return service.findPublicByCode(code, document);
    }

    @PostMapping("/{code}/approve")
    @Operation(summary = "Aprovação pública do orçamento pelo cliente")
    public PublicServiceOrderResponseDto approve(@PathVariable String code, @RequestBody @Valid CustomerApprovalRequestDto request) {
        return service.approveByCustomer(code, request.document(), request.comment());
    }
}
