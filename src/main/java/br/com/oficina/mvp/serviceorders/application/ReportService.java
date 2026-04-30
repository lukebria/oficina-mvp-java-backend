package br.com.oficina.mvp.serviceorders.application;

import br.com.oficina.mvp.serviceorders.api.dto.AverageExecutionTimeResponse;
import br.com.oficina.mvp.serviceorders.infra.ServiceOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final ServiceOrderRepository repository;

    public ReportService(ServiceOrderRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public AverageExecutionTimeResponse averageExecutionTime() {
        var durations = repository.findAll()
                .stream()
                .filter(order -> order.getStartedAt() != null && order.getFinalizedAt() != null)
                .map(order -> Duration.between(order.getStartedAt(), order.getFinalizedAt()).toMinutes())
                .collect(Collectors.toList());

        var count = durations.size();

        var averageMinutes = durations.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        return new AverageExecutionTimeResponse(
                count,
                averageMinutes,
                averageMinutes / 60.0
        );
    }
}
