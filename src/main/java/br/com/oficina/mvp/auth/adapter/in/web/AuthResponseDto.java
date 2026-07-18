package br.com.oficina.mvp.auth.adapter.in.web;

import br.com.oficina.mvp.auth.application.port.in.AuthResult;
import br.com.oficina.mvp.shared.domain.Role;

public record AuthResponseDto(String token, UserView user) {
    public record UserView(Long id, String name, String email, Role role) {}

    public static AuthResponseDto from(AuthResult result) {
        var user = result.user();
        return new AuthResponseDto(result.token(), new UserView(user.getId(), user.getName(), user.getEmail(), user.getRole()));
    }
}
