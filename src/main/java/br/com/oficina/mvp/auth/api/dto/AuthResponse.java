package br.com.oficina.mvp.auth.api.dto;

import br.com.oficina.mvp.auth.domain.Role;

public record AuthResponse(String token, UserView user) {
    public record UserView(Long id, String name, String email, Role role) {}
}
