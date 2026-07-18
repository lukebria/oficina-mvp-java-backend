package br.com.oficina.mvp.customer.adapter.in.web;

import br.com.oficina.mvp.customer.application.port.in.CustomerCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequestDto(
        @NotBlank @Size(min = 2) String name,
        @NotBlank @Size(min = 11, max = 18) String document,
        @Email String email,
        @Size(min = 8) String phone
) {
    public CustomerCommand toCommand() {
        return new CustomerCommand(name(), document(), email(), phone());
    }
}
