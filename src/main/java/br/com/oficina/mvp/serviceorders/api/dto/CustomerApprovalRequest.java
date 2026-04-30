package br.com.oficina.mvp.serviceorders.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerApprovalRequest(
        @NotBlank @Size(min = 11, max = 18) String document,
        String comment
) {}
