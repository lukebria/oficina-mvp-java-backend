package br.com.oficina.mvp.shared.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Assina tokens do fluxo público de cliente (autenticação via CPF) do mesmo jeito que a Function Serverless externa
 * faria — usado só em testes, para simular esse emissor sem depender do outro repositório. Ver
 * docs/architecture.md, seção 5, para o contrato completo (claims, segredo dedicado).
 */
public final class TestCustomerTokens {
    private TestCustomerTokens() {}

    public static String forDocument(String document, String customerJwtSecret) {
        var key = Keys.hmacShaKeyFor(customerJwtSecret.getBytes(StandardCharsets.UTF_8));
        var now = Instant.now();
        return Jwts.builder()
                .subject(document)
                .claim("role", "CUSTOMER")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900)))
                .signWith(key)
                .compact();
    }
}
