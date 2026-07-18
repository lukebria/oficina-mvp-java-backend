package br.com.oficina.mvp.report.adapter.in.web;

import br.com.oficina.mvp.report.application.port.in.AverageExecutionTimeResult;

public record AverageExecutionTimeResponseDto(
        long finalizedOrders,
        double averageExecutionMinutes,
        double averageExecutionHours
) {
    public static AverageExecutionTimeResponseDto from(AverageExecutionTimeResult result) {
        return new AverageExecutionTimeResponseDto(result.finalizedOrders(), result.averageExecutionMinutes(), result.averageExecutionHours());
    }
}
