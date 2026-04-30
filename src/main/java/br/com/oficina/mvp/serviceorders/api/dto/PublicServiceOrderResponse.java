package br.com.oficina.mvp.serviceorders.api.dto;

import br.com.oficina.mvp.serviceorders.domain.ServiceOrder;
import br.com.oficina.mvp.serviceorders.domain.ServiceOrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record PublicServiceOrderResponse(
        String code,
        ServiceOrderStatus status,
        String customerName,
        VehicleView vehicle,
        BudgetView budget,
        List<ServiceOrderResponse.OrderServiceView> services,
        List<ServiceOrderResponse.OrderPartView> parts,
        List<ServiceOrderResponse.HistoryView> history
) {
    public static PublicServiceOrderResponse from(ServiceOrder order) {
        var full = ServiceOrderResponse.from(order);
        return new PublicServiceOrderResponse(
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
