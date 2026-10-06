package br.com.oficina.mvp.shared.observability;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regressão do plano 13: no Spring Boot 4 a auto-configuração de tracing/OpenTelemetry fica em módulos próprios
 * (spring-boot-starter-opentelemetry). Sem eles no classpath as bibliotecas OTLP ficam inertes e a aplicação não
 * exporta nada para o New Relic, sem nenhum erro. Este teste falha se esses módulos sumirem do build.
 */
@SpringBootTest
@ActiveProfiles("test")
class ObservabilityAutoConfigurationIntegrationTest {
    @Autowired
    ApplicationContext context;

    @Test
    void shouldAutoConfigureOpenTelemetrySdk() {
        assertThat(context.getBeanNamesForType(OpenTelemetry.class)).isNotEmpty();
    }

    @Test
    void shouldAutoConfigureOpenTelemetryTracer() {
        assertThat(context.getBean(Tracer.class)).isInstanceOf(OtelTracer.class);
    }
}
