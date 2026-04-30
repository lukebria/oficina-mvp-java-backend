package br.com.oficina.mvp.catalog.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServiceCatalogItemRequest(
        @NotBlank @Size(min = 2) String name,
        String description,
        @NotNull @DecimalMin(value = "0.00") BigDecimal basePrice,
        @NotNull @Positive Integer estimatedMinutes,
        Boolean active
) {}
