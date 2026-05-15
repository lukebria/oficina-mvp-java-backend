package br.com.oficina.mvp.dtos;

import br.com.oficina.mvp.dtos.enums.ServiceOrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequestDto(
        @NotNull ServiceOrderStatus status,
        String comment
) {}
