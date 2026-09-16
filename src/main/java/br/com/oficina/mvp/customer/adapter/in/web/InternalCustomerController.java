package br.com.oficina.mvp.customer.adapter.in.web;

import br.com.oficina.mvp.customer.application.port.in.CustomerUseCase;
import br.com.oficina.mvp.shared.validation.DocumentValidator;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de serviço-a-serviço, consumido pela Function Serverless externa de autenticação via CPF (repositório
 * separado) para checar existência/status do cliente antes de emitir o JWT do fluxo público — ver
 * docs/architecture.md, seção 5. Protegido por {@code X-Internal-Api-Key}
 * ({@link br.com.oficina.mvp.shared.security.InternalApiKeyAuthenticationFilter}), não por JWT.
 */
@RestController
@RequestMapping("/api/internal/customers")
public class InternalCustomerController {
    private final CustomerUseCase customerUseCase;

    public InternalCustomerController(CustomerUseCase customerUseCase) {
        this.customerUseCase = customerUseCase;
    }

    @GetMapping("/{document}")
    @Operation(summary = "Consulta existência/status do cliente para a Function Serverless de autenticação via CPF")
    public ResponseEntity<InternalCustomerStatusResponseDto> status(@PathVariable String document) {
        var normalized = DocumentValidator.normalize(document);
        return customerUseCase.findByDocument(normalized)
                .map(customer -> ResponseEntity.ok(InternalCustomerStatusResponseDto.found(customer)))
                .orElseGet(() -> ResponseEntity.status(404).body(InternalCustomerStatusResponseDto.notFound()));
    }
}
