package br.com.oficina.mvp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerApprovalRequestDto(
        @NotBlank @Size(min = 11, max = 18) String document,
        String comment
) {}
