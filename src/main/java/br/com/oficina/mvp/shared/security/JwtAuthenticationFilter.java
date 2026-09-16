package br.com.oficina.mvp.shared.security;

import br.com.oficina.mvp.auth.application.port.out.UserRepositoryPort;
import br.com.oficina.mvp.customer.application.port.out.CustomerRepositoryPort;
import br.com.oficina.mvp.customer.domain.CustomerStatus;
import io.jsonwebtoken.security.SignatureException;
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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepositoryPort userRepository;
    private final CustomerRepositoryPort customerRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepositoryPort userRepository,
                                    CustomerRepositoryPort customerRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        var token = header.substring(7);
        try {
            authenticateAsStaff(request, token);
        } catch (SignatureException signatureMismatch) {
            try {
                authenticateAsCustomer(request, token);
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateAsStaff(HttpServletRequest request, String token) {
        var claims = jwtService.parse(token);
        var userId = Long.valueOf(claims.getSubject());
        var user = userRepository.findById(userId);

        if (user.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.get().getRole().name()));
            var authentication = new UsernamePasswordAuthenticationToken(user.get().getEmail(), null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    /**
     * Token do fluxo público de cliente (autenticação via CPF), emitido pela Function Serverless externa —
     * ver docs/architecture.md, seção 5. O {@code sub} do token é o documento normalizado, que também vira o
     * principal da autenticação (usado depois para restringir a OS consultada/aprovada ao próprio cliente).
     * Um cliente {@code INACTIVE} nunca autentica, mesmo com um token ainda válido — o status é revalidado a
     * cada request, não só no momento em que a Function externa emitiu o token.
     */
    private void authenticateAsCustomer(HttpServletRequest request, String token) {
        var claims = jwtService.parseCustomer(token);
        var document = claims.getSubject();
        var customer = customerRepository.findByDocument(document);

        if (customer.isPresent() && customer.get().getStatus() == CustomerStatus.ACTIVE
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
            var authentication = new UsernamePasswordAuthenticationToken(document, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}
