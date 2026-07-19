package br.com.oficina.mvp.serviceorder.adapter.in.web;

import br.com.oficina.mvp.serviceorder.application.port.in.PublicServiceOrderUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/public/service-orders")
public class PublicServiceOrderController {
    private final PublicServiceOrderUseCase publicServiceOrderUseCase;

    public PublicServiceOrderController(PublicServiceOrderUseCase publicServiceOrderUseCase) {
        this.publicServiceOrderUseCase = publicServiceOrderUseCase;
    }

    @GetMapping("/{code}")
    @Operation(summary = "Consulta pública de OS por código e CPF/CNPJ")
    public PublicServiceOrderResponseDto status(@PathVariable String code, @RequestParam @NotBlank String document) {
        return PublicServiceOrderResponseDto.from(publicServiceOrderUseCase.findByCode(code, document));
    }

    @PostMapping("/{code}/approval")
    @Operation(summary = "Registra a decisão (aprovação ou recusa) pública do orçamento pelo cliente")
    public PublicServiceOrderResponseDto decideApproval(@PathVariable String code, @RequestBody @Valid CustomerApprovalRequestDto request) {
        return PublicServiceOrderResponseDto.from(publicServiceOrderUseCase.decideApprovalByCustomer(code, request.document(), request.approved(), request.comment()));
    }
}
