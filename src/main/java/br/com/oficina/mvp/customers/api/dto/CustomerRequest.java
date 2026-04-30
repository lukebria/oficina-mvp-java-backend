package br.com.oficina.mvp.customers.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Size(min = 2) String name,
        @NotBlank @Size(min = 11, max = 18) String document,
        @Email String email,
        @Size(min = 8) String phone
) {}
