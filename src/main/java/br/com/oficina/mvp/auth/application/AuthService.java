package br.com.oficina.mvp.auth.application;

import br.com.oficina.mvp.auth.api.dto.AuthResponse;
import br.com.oficina.mvp.auth.api.dto.LoginRequest;
import br.com.oficina.mvp.auth.infra.UserRepository;
import br.com.oficina.mvp.shared.exception.BusinessException;
import br.com.oficina.mvp.shared.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        var user = users.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Credenciais inválidas.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("Credenciais inválidas.", HttpStatus.UNAUTHORIZED);
        }

        return new AuthResponse(jwtService.generate(user),
                new AuthResponse.UserView(user.getId(), user.getName(), user.getEmail(), user.getRole()));
    }
}
