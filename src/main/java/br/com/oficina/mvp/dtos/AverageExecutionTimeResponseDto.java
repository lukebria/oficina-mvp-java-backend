package br.com.oficina.mvp.dtos;

public record AverageExecutionTimeResponseDto(
        long finalizedOrders,
        double averageExecutionMinutes,
        double averageExecutionHours
) {}
