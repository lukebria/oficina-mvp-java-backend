package br.com.oficina.mvp.vehicle.application.port.in;

public record VehicleCommand(Long customerId, String plate, String brand, String model, Integer manufacturingYear) {}
