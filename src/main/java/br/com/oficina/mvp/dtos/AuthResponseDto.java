package br.com.oficina.mvp.dtos;

import br.com.oficina.mvp.dtos.enums.Role;

public record AuthResponseDto(String token, UserView user) {
    public record UserView(Long id, String name, String email, Role role) {}
}
