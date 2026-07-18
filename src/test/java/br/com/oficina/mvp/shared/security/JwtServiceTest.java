package br.com.oficina.mvp.shared.security;

import br.com.oficina.mvp.auth.domain.User;
import br.com.oficina.mvp.shared.domain.Role;
import br.com.oficina.mvp.shared.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        var properties = new JwtProperties("teste-teste-teste-teste-teste-teste-teste-teste", 60);
        jwtService = new JwtService(properties);
    }

    @Test
    void shouldGenerateAndParseToken() {
        var user = new User("Admin", "admin@oficina.com", "hash", Role.ADMIN);

        var token = jwtService.generate(user);
        var claims = jwtService.parse(token);

        assertThat(token).isNotBlank();
        assertThat(claims.getSubject()).isNotNull();
        assertThat(claims.get("email", String.class)).isEqualTo("admin@oficina.com");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }
}
