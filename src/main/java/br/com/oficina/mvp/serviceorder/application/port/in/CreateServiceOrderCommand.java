package br.com.oficina.mvp.serviceorder.application.port.in;

import java.util.List;

public record CreateServiceOrderCommand(
        String customerDocument,
        CustomerData customer,
        VehicleData vehicle,
        String customerNotes,
        List<ServiceItemData> services,
        List<PartItemData> parts
) {
    public record CustomerData(String name, String email, String phone) {}

    public record VehicleData(String plate, String brand, String model, Integer manufacturingYear) {}

    public record ServiceItemData(Long serviceItemId, Integer quantity) {}

    public record PartItemData(Long partId, Integer quantity) {}
}
