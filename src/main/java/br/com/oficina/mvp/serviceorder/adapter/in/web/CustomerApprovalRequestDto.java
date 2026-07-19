package br.com.oficina.mvp.serviceorder.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CustomerApprovalRequestDto(
        @NotBlank @Size(min = 11, max = 18) String document,
        @NotNull Boolean approved,
        String comment
) {}
