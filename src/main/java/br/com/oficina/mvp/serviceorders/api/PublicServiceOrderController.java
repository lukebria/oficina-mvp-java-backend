package br.com.oficina.mvp.serviceorders.api;

import br.com.oficina.mvp.serviceorders.api.dto.CustomerApprovalRequest;
import br.com.oficina.mvp.serviceorders.api.dto.PublicServiceOrderResponse;
import br.com.oficina.mvp.serviceorders.application.ServiceOrderApplicationService;
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
    public PublicServiceOrderResponse status(@PathVariable String code, @RequestParam @NotBlank String document) {
        return service.findPublicByCode(code, document);
    }

    @PostMapping("/{code}/approve")
    @Operation(summary = "Aprovação pública do orçamento pelo cliente")
    public PublicServiceOrderResponse approve(@PathVariable String code, @RequestBody @Valid CustomerApprovalRequest request) {
        return service.approveByCustomer(code, request.document(), request.comment());
    }
}
