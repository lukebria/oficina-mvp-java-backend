package br.com.oficina.mvp.dtos;

import br.com.oficina.mvp.domains.ServiceOrder;
import br.com.oficina.mvp.dtos.enums.ServiceOrderStatus;
import br.com.oficina.mvp.domains.ServiceOrderStatusHistory;
import br.com.oficina.mvp.domains.WorkOrderPart;
import br.com.oficina.mvp.domains.WorkOrderService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

public record ServiceOrderResponseDto(
        Long id,
        String code,
        ServiceOrderStatus status,
        CustomerView customer,
        VehicleView vehicle,
        String customerNotes,
        BudgetView budget,
        List<OrderServiceView> services,
        List<OrderPartView> parts,
        List<HistoryView> history,
        OffsetDateTime approvedAt,
        OffsetDateTime startedAt,
        OffsetDateTime finalizedAt,
        OffsetDateTime deliveredAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ServiceOrderResponseDto from(ServiceOrder order) {
        return new ServiceOrderResponseDto(
                order.getId(),
                order.getCode(),
                order.getStatus(),
                new CustomerView(
                        order.getCustomer().getId(),
                        order.getCustomer().getName(),
                        order.getCustomer().getDocument()
                ),
                new VehicleView(
                        order.getVehicle().getId(),
                        order.getVehicle().getPlate(),
                        order.getVehicle().getBrand(),
                        order.getVehicle().getModel(),
                        order.getVehicle().getManufacturingYear()
                ),
                order.getCustomerNotes(),
                new BudgetView(
                        order.getTotalServices(),
                        order.getTotalParts(),
                        order.getTotalAmount()
                ),
                order.getServices().stream()
                        .map(OrderServiceView::from)
                        .toList(),
                order.getParts().stream()
                        .map(OrderPartView::from)
                        .toList(),
                order.getHistory().stream()
                        .sorted(Comparator.comparing(
                                ServiceOrderStatusHistory::getChangedAt,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ))
                        .map(HistoryView::from)
                        .toList(),
                order.getApprovedAt(),
                order.getStartedAt(),
                order.getFinalizedAt(),
                order.getDeliveredAt(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public record CustomerView(Long id, String name, String document) {}
    public record VehicleView(Long id, String plate, String brand, String model, Integer manufacturingYear) {}
    public record BudgetView(BigDecimal totalServices, BigDecimal totalParts, BigDecimal totalAmount) {}

    public record OrderServiceView(Long id, Long serviceItemId, String name, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        static OrderServiceView from(WorkOrderService item) {
            return new OrderServiceView(item.getId(), item.getServiceItem().getId(), item.getServiceItem().getName(), item.getQuantity(), item.getUnitPrice(), item.getTotalPrice());
        }
    }

    public record OrderPartView(Long id, Long partId, String name, String sku, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        static OrderPartView from(WorkOrderPart item) {
            return new OrderPartView(item.getId(), item.getPart().getId(), item.getPart().getName(), item.getPart().getSku(), item.getQuantity(), item.getUnitPrice(), item.getTotalPrice());
        }
    }

    public record HistoryView(Long id, ServiceOrderStatus status, String comment, java.time.LocalDateTime changedAt) {
        static HistoryView from(ServiceOrderStatusHistory history) {
            return new HistoryView(history.getId(), history.getStatus(), history.getComment(), history.getChangedAt());
        }
    }
}
