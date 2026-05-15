package br.com.oficina.mvp.services;

import br.com.oficina.mvp.dtos.AuthResponseDto;
import br.com.oficina.mvp.dtos.LoginRequestDto;
import br.com.oficina.mvp.infra.UserRepository;
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
    public AuthResponseDto login(LoginRequestDto request) {
        var user = users.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Credenciais inválidas.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("Credenciais inválidas.", HttpStatus.UNAUTHORIZED);
        }

        return new AuthResponseDto(jwtService.generate(user),
                new AuthResponseDto.UserView(user.getId(), user.getName(), user.getEmail(), user.getRole()));
    }
}
