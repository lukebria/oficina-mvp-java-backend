package br.com.oficina.mvp.dtos;

import br.com.oficina.mvp.domains.ServiceOrder;
import br.com.oficina.mvp.dtos.enums.ServiceOrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record PublicServiceOrderResponseDto(
        String code,
        ServiceOrderStatus status,
        String customerName,
        VehicleView vehicle,
        BudgetView budget,
        List<ServiceOrderResponseDto.OrderServiceView> services,
        List<ServiceOrderResponseDto.OrderPartView> parts,
        List<ServiceOrderResponseDto.HistoryView> history
) {
    public static PublicServiceOrderResponseDto from(ServiceOrder order) {
        var full = ServiceOrderResponseDto.from(order);
        return new PublicServiceOrderResponseDto(
                order.getCode(),
                order.getStatus(),
                order.getCustomer().getName(),
                new VehicleView(order.getVehicle().getPlate(), order.getVehicle().getBrand(), order.getVehicle().getModel(), order.getVehicle().getYear()),
                new BudgetView(order.getTotalServices(), order.getTotalParts(), order.getTotalAmount()),
                full.services(), full.parts(), full.history()
        );
    }

    public record VehicleView(String plate, String brand, String model, Integer year) {}
    public record BudgetView(BigDecimal totalServices, BigDecimal totalParts, BigDecimal totalAmount) {}
}
