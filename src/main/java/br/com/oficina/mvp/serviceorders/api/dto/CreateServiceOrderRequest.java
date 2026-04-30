package br.com.oficina.mvp.serviceorders.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateServiceOrderRequest(
        @NotBlank @Size(min = 11, max = 18) String customerDocument,
        @Valid @NotNull CustomerData customer,
        @Valid @NotNull VehicleData vehicle,
        String customerNotes,
        @Valid @NotEmpty List<ServiceItemData> services,
        @Valid List<PartItemData> parts
) {
    public record CustomerData(
            @NotBlank @Size(min = 2) String name,
            @Email String email,
            @Size(min = 8) String phone
    ) {}

    public record VehicleData(
            @NotBlank @Size(min = 7, max = 8) String plate,
            @NotBlank @Size(min = 2) String brand,
            @NotBlank String model,
            @NotNull @Min(1900) Integer year
    ) {}

    public record ServiceItemData(
            @NotNull Long serviceItemId,
            @Positive Integer quantity
    ) {}

    public record PartItemData(
            @NotNull Long partId,
            @NotNull @Positive Integer quantity
    ) {}
}
