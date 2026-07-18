package br.com.oficina.mvp.auth.application.port.in;

import br.com.oficina.mvp.auth.domain.User;

public record AuthResult(String token, User user) {}
