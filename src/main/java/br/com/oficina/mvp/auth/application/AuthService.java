package br.com.oficina.mvp.auth.application;

import br.com.oficina.mvp.auth.application.port.in.AuthResult;
import br.com.oficina.mvp.auth.application.port.in.AuthUseCase;
import br.com.oficina.mvp.auth.application.port.in.LoginCommand;
import br.com.oficina.mvp.auth.application.port.out.UserRepositoryPort;
import br.com.oficina.mvp.shared.exception.BusinessException;
import br.com.oficina.mvp.shared.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements AuthUseCase {
    private final UserRepositoryPort users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepositoryPort users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult login(LoginCommand command) {
        var user = users.findByEmail(command.email())
                .orElseThrow(() -> new BusinessException("Credenciais inválidas.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new BusinessException("Credenciais inválidas.", HttpStatus.UNAUTHORIZED);
        }

        return new AuthResult(jwtService.generate(user), user);
    }
}
