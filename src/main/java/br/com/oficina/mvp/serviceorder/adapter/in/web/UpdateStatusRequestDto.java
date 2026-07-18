package br.com.oficina.mvp.serviceorder.adapter.in.web;

import br.com.oficina.mvp.shared.domain.ServiceOrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequestDto(
        @NotNull ServiceOrderStatus status,
        String comment
) {}
