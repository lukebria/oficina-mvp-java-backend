package br.com.oficina.mvp.serviceorder.adapter.in.web;

import jakarta.validation.constraints.NotNull;

public record ApprovalRequestDto(
        @NotNull Boolean approved,
        String comment
) {}
