package br.com.oficina.mvp.serviceorders.api.dto;

public record AverageExecutionTimeResponse(
        long finalizedOrders,
        double averageExecutionMinutes,
        double averageExecutionHours
) {}
