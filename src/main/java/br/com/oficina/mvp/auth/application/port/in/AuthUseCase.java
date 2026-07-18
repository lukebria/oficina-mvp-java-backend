package br.com.oficina.mvp.auth.application.port.in;

public interface AuthUseCase {
    AuthResult login(LoginCommand command);
}
