package br.com.oficina.mvp.shared.security;

import br.com.oficina.mvp.shared.config.InternalApiProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Autentica chamadas de serviço-a-serviço (ex: a Function Serverless de autenticação via CPF, em outro repositório)
 * que precisam consultar {@code /api/internal/**} sem um JWT de usuário/cliente — ver docs/architecture.md, seção 5.
 */
@Component
public class InternalApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private static final String HEADER = "X-Internal-Api-Key";

    private final InternalApiProperties properties;

    public InternalApiKeyAuthenticationFilter(InternalApiProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var apiKey = request.getHeader(HEADER);

        if (apiKey != null && !apiKey.isBlank() && apiKey.equals(properties.apiKey())
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"));
            var authentication = new UsernamePasswordAuthenticationToken("internal-service", null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
