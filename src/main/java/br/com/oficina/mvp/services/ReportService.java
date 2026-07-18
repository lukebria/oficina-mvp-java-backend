package br.com.oficina.mvp.services;

import br.com.oficina.mvp.dtos.AverageExecutionTimeResponseDto;
import br.com.oficina.mvp.infra.ServiceOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class ReportService {
    private final ServiceOrderRepository serviceOrders;

    public ReportService(ServiceOrderRepository serviceOrders) {
        this.serviceOrders = serviceOrders;
    }

    @Transactional(readOnly = true)
    public AverageExecutionTimeResponseDto averageExecutionTime() {
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

        return new AverageExecutionTimeResponseDto(
                count,
                averageMinutes,
                averageMinutes / 60.0
        );
    }
}
