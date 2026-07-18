package br.com.oficina.mvp.vehicle.adapter.in.web;

import br.com.oficina.mvp.vehicle.domain.Vehicle;
import java.time.OffsetDateTime;

public record VehicleResponseDto(
        Long id,
        Long customerId,
        String customerName,
        String plate,
        String brand,
        String model,
        Integer manufacturingYear,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static VehicleResponseDto from(Vehicle vehicle) {
        return new VehicleResponseDto(
                vehicle.getId(), vehicle.getCustomer().getId(), vehicle.getCustomer().getName(),
                vehicle.getPlate(), vehicle.getBrand(), vehicle.getModel(), vehicle.getManufacturingYear(),
                vehicle.getCreatedAt(), vehicle.getUpdatedAt()
        );
    }
}
