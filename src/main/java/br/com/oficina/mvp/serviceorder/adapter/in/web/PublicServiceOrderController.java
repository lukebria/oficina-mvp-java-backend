package br.com.oficina.mvp.serviceorder.adapter.in.web;

import br.com.oficina.mvp.serviceorder.application.port.in.PublicServiceOrderUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rotas protegidas pelo fluxo de autenticação via CPF (token emitido pela Function Serverless externa) — ver
 * docs/architecture.md, seção 5/7.4. O documento do cliente autenticado vem sempre do {@code Authentication}
 * (subject do JWT), nunca de um parâmetro de request: assim um cliente só consegue consultar/aprovar a própria OS,
 * mesmo de posse de um token válido.
 */
@RestController
@Validated
@RequestMapping("/api/public/service-orders")
public class PublicServiceOrderController {
    private final PublicServiceOrderUseCase publicServiceOrderUseCase;

    public PublicServiceOrderController(PublicServiceOrderUseCase publicServiceOrderUseCase) {
        this.publicServiceOrderUseCase = publicServiceOrderUseCase;
    }

    @GetMapping("/{code}")
    @Operation(summary = "Consulta pública de OS por código, autenticado via CPF (Bearer token)")
    public PublicServiceOrderResponseDto status(@PathVariable String code) {
        return PublicServiceOrderResponseDto.from(publicServiceOrderUseCase.findByCode(code, authenticatedDocument()));
    }

    @PostMapping("/{code}/approval")
    @Operation(summary = "Registra a decisão (aprovação ou recusa) pública do orçamento pelo cliente, autenticado via CPF (Bearer token)")
    public PublicServiceOrderResponseDto decideApproval(@PathVariable String code, @RequestBody @Valid CustomerApprovalRequestDto request) {
        return PublicServiceOrderResponseDto.from(publicServiceOrderUseCase.decideApprovalByCustomer(
                code, authenticatedDocument(), request.approved(), request.comment()));
    }

    private String authenticatedDocument() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
