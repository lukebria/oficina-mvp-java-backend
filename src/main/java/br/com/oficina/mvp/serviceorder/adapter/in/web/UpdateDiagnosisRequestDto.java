package br.com.oficina.mvp.serviceorder.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record UpdateDiagnosisRequestDto(
        @NotBlank String diagnosis
) {}
