package br.com.oficina.mvp.auth.adapter.in.web;

import br.com.oficina.mvp.auth.application.port.in.AuthUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica um usuário administrativo e retorna um JWT")
    public AuthResponseDto login(@RequestBody @Valid LoginRequestDto request) {
        return AuthResponseDto.from(authUseCase.login(request.toCommand()));
    }
}
