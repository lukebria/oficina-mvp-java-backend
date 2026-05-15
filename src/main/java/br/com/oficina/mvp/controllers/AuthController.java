package br.com.oficina.mvp.controllers;

import br.com.oficina.mvp.dtos.AuthResponseDto;
import br.com.oficina.mvp.dtos.LoginRequestDto;
import br.com.oficina.mvp.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica um usuário administrativo e retorna um JWT")
    public AuthResponseDto login(@RequestBody @Valid LoginRequestDto request) {
        return authService.login(request);
    }
}
