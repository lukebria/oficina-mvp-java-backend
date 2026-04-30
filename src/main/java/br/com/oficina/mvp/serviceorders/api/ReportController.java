package br.com.oficina.mvp.serviceorders.api;

import br.com.oficina.mvp.serviceorders.api.dto.AverageExecutionTimeResponse;
import br.com.oficina.mvp.serviceorders.application.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) { this.service = service; }

    @GetMapping("/average-execution-time")
    @Operation(summary = "Retorna o tempo médio de execução das OS finalizadas")
    public AverageExecutionTimeResponse averageExecutionTime() {
        return service.averageExecutionTime();
    }
}
