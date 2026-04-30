package br.com.oficina.mvp.shared.config;

import br.com.oficina.mvp.auth.domain.Role;
import br.com.oficina.mvp.auth.domain.User;
import br.com.oficina.mvp.auth.infra.UserRepository;
import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;
import br.com.oficina.mvp.catalog.infra.ServiceCatalogItemRepository;
import br.com.oficina.mvp.parts.domain.Part;
import br.com.oficina.mvp.parts.infra.PartRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {
    @Bean
    @Profile("!test")
    CommandLineRunner seedInitialData(
            UserRepository users,
            ServiceCatalogItemRepository services,
            PartRepository parts,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.admin-email}") String adminEmail,
            @Value("${app.seed.admin-password}") String adminPassword
    ) {
        return args -> {
            if (!users.existsByEmail(adminEmail)) {
                users.save(new User("Administrador", adminEmail, passwordEncoder.encode(adminPassword), Role.ADMIN));
            }

            if (services.count() == 0) {
                services.save(new ServiceCatalogItem("Troca de óleo", "Troca de óleo do motor", new BigDecimal("180.00"), 60, true));
                services.save(new ServiceCatalogItem("Alinhamento", "Alinhamento de direção", new BigDecimal("120.00"), 45, true));
                services.save(new ServiceCatalogItem("Diagnóstico eletrônico", "Leitura e diagnóstico via scanner", new BigDecimal("150.00"), 50, true));
            }

            if (parts.count() == 0) {
                parts.save(new Part("Filtro de óleo", "FILTRO-OLEO-001", new BigDecimal("45.90"), 25, 5, true));
                parts.save(new Part("Óleo 5W30", "OLEO-5W30-001", new BigDecimal("39.90"), 80, 12, true));
                parts.save(new Part("Pastilha de freio", "PASTILHA-FREIO-001", new BigDecimal("180.00"), 10, 3, true));
            }
        };
    }
}
