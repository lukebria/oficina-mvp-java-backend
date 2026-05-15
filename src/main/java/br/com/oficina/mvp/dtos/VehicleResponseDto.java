package br.com.oficina.mvp.dtos;

import br.com.oficina.mvp.domains.Vehicle;
import java.time.OffsetDateTime;

public record VehicleResponseDto(
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
    public static VehicleResponseDto from(Vehicle vehicle) {
        return new VehicleResponseDto(
                vehicle.getId(), vehicle.getCustomer().getId(), vehicle.getCustomer().getName(),
                vehicle.getPlate(), vehicle.getBrand(), vehicle.getModel(), vehicle.getYear(),
                vehicle.getCreatedAt(), vehicle.getUpdatedAt()
        );
    }
}
