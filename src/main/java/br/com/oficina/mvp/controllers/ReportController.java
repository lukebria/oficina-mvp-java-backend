package br.com.oficina.mvp.controllers;

import br.com.oficina.mvp.dtos.AverageExecutionTimeResponseDto;
import br.com.oficina.mvp.services.ReportService;
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
    public AverageExecutionTimeResponseDto averageExecutionTime() {
        return service.averageExecutionTime();
    }
}
