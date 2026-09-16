package br.com.oficina.mvp.shared.security;

import br.com.oficina.mvp.auth.domain.User;
import br.com.oficina.mvp.shared.domain.Role;
import br.com.oficina.mvp.shared.config.JwtProperties;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String CUSTOMER_SECRET = "teste-cliente-teste-cliente-teste-cliente-teste";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        var properties = new JwtProperties("teste-teste-teste-teste-teste-teste-teste-teste", 60, CUSTOMER_SECRET);
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

    @Test
    void shouldParseCustomerTokenSignedWithDedicatedSecret() {
        var token = TestCustomerTokens.forDocument("52998224725", CUSTOMER_SECRET);

        var claims = jwtService.parseCustomer(token);

        assertThat(claims.getSubject()).isEqualTo("52998224725");
        assertThat(claims.get("role", String.class)).isEqualTo("CUSTOMER");
    }

    @Test
    void shouldRejectCustomerTokenAsAdminTokenAndViceVersa() {
        var customerToken = TestCustomerTokens.forDocument("52998224725", CUSTOMER_SECRET);
        var adminToken = jwtService.generate(new User("Admin", "admin@oficina.com", "hash", Role.ADMIN));

        assertThatThrownBy(() -> jwtService.parse(customerToken)).isInstanceOf(SignatureException.class);
        assertThatThrownBy(() -> jwtService.parseCustomer(adminToken)).isInstanceOf(SignatureException.class);
    }
}
