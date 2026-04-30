package br.com.oficina.mvp.parts.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PartRequest(
        @NotBlank @Size(min = 2) String name,
        @NotBlank String sku,
        @NotNull @DecimalMin(value = "0.00") BigDecimal unitPrice,
        @NotNull @Min(0) Integer stockQuantity,
        @Min(0) Integer minStock,
        Boolean active
) {}
