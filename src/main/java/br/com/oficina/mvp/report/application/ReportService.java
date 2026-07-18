package br.com.oficina.mvp.report.application;

import br.com.oficina.mvp.report.application.port.in.AverageExecutionTimeResult;
import br.com.oficina.mvp.report.application.port.in.ReportUseCase;
import br.com.oficina.mvp.serviceorder.application.port.out.ServiceOrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class ReportService implements ReportUseCase {
    private final ServiceOrderRepositoryPort serviceOrders;

    public ReportService(ServiceOrderRepositoryPort serviceOrders) {
        this.serviceOrders = serviceOrders;
    }

    @Override
    @Transactional(readOnly = true)
    public AverageExecutionTimeResult averageExecutionTime() {
        var durations = serviceOrders.findAll()
                .stream()
                .filter(order -> order.getStartedAt() != null && order.getFinalizedAt() != null)
                .map(order -> Duration.between(order.getStartedAt(), order.getFinalizedAt()).toMinutes())
                .toList();

        var count = durations.size();

        var averageMinutes = durations.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        return new AverageExecutionTimeResult(count, averageMinutes, averageMinutes / 60.0);
    }
}
