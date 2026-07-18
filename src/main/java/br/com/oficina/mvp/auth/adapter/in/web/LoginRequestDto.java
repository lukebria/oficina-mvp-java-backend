package br.com.oficina.mvp.auth.adapter.in.web;

import br.com.oficina.mvp.auth.application.port.in.LoginCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6) String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(email(), password());
    }
}
