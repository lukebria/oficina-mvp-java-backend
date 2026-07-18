package br.com.oficina.mvp.report.application.port.in;

public record AverageExecutionTimeResult(long finalizedOrders, double averageExecutionMinutes, double averageExecutionHours) {}
