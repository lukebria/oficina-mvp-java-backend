package br.com.oficina.mvp.services;

import br.com.oficina.mvp.domains.User;
import br.com.oficina.mvp.dtos.LoginRequestDto;
import br.com.oficina.mvp.dtos.enums.Role;
import br.com.oficina.mvp.infra.UserRepository;
import br.com.oficina.mvp.shared.exception.BusinessException;
import br.com.oficina.mvp.shared.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository users;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtService jwtService;

    @InjectMocks
    AuthService service;

    @Test
    void shouldLoginSuccessfully() {
        var user = new User("Admin", "admin@oficina.com", "hash", Role.ADMIN);
        when(users.findByEmail("admin@oficina.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Admin@123", "hash")).thenReturn(true);
        when(jwtService.generate(user)).thenReturn("jwt-token");

        var result = service.login(new LoginRequestDto("admin@oficina.com", "Admin@123"));

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.user().email()).isEqualTo("admin@oficina.com");
    }

    @Test
    void shouldRejectUnknownEmail() {
        when(users.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequestDto("unknown@email.com", "senha")))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void shouldRejectWrongPassword() {
        var user = new User("Admin", "admin@oficina.com", "hash", Role.ADMIN);
        when(users.findByEmail("admin@oficina.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequestDto("admin@oficina.com", "wrong")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Credenciais inválidas");
    }
}
