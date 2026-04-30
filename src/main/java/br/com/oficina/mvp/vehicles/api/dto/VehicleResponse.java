package br.com.oficina.mvp.vehicles.api.dto;

import br.com.oficina.mvp.vehicles.domain.Vehicle;
import java.time.OffsetDateTime;

public record VehicleResponse(
        Long id,
        Long customerId,
        String customerName,
        String plate,
        String brand,
        String model,
        Integer year,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(), vehicle.getCustomer().getId(), vehicle.getCustomer().getName(),
                vehicle.getPlate(), vehicle.getBrand(), vehicle.getModel(), vehicle.getYear(),
                vehicle.getCreatedAt(), vehicle.getUpdatedAt()
        );
    }
}
