package br.com.oficina.mvp.serviceorders.api.dto;

import br.com.oficina.mvp.serviceorders.domain.ServiceOrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull ServiceOrderStatus status,
        String comment
) {}
