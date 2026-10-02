package br.com.oficina.mvp.shared.observability;

import br.com.oficina.mvp.shared.domain.ServiceOrderStatus;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Métricas de negócio expostas via Micrometer, para os dashboards de observabilidade exigidos pela Fase 3
 * (volume diário de ordens de serviço, tempo médio de execução por status, erros/falhas de integração). O
 * registro em si (New Relic, via exporter OTLP configurado em {@code application.yml}) é o único pedaço que
 * depende de credenciais externas — as métricas em si são coletadas sempre, mesmo sem nenhum exporter
 * configurado. Ver POST-TECH/FASE-3/plans/05-observabilidade-new-relic.md.
 */
@Component
public class BusinessMetrics {
    private final MeterRegistry registry;

    public BusinessMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordServiceOrderCreated() {
        registry.counter("service_order.created").increment();
    }

    /** {@code previousStatus} é o status que a OS acabou de deixar - a duração é quanto tempo ficou nele. */
    public void recordServiceOrderStatusDuration(ServiceOrderStatus previousStatus, Duration duration) {
        Timer.builder("service_order.status.duration")
                .tag("status", previousStatus.name())
                .register(registry)
                .record(duration);
    }

    public void recordIntegrationFailure(String integration) {
        registry.counter("integration.failure", "integration", integration).increment();
    }
}
