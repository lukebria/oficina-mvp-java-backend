package br.com.oficina.mvp.report.adapter.in.web;

import br.com.oficina.mvp.report.application.port.in.ReportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportUseCase reportUseCase;

    public ReportController(ReportUseCase reportUseCase) { this.reportUseCase = reportUseCase; }

    @GetMapping("/average-execution-time")
    @Operation(summary = "Retorna o tempo médio de execução das OS finalizadas")
    public AverageExecutionTimeResponseDto averageExecutionTime() {
        return AverageExecutionTimeResponseDto.from(reportUseCase.averageExecutionTime());
    }
}
