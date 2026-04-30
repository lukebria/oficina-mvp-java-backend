package br.com.oficina.mvp.vehicles.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
        @NotNull Long customerId,
        @NotBlank @Size(min = 7, max = 8) String plate,
        @NotBlank @Size(min = 2) String brand,
        @NotBlank String model,
        @NotNull @Min(1900) @Max(2100) Integer year
) {}
