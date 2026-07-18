package br.com.oficina.mvp.report.application;

import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.shared.domain.ServiceOrderStatus;
import br.com.oficina.mvp.serviceorder.application.port.out.ServiceOrderRepositoryPort;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import br.com.oficina.mvp.vehicle.domain.Vehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    ServiceOrderRepositoryPort serviceOrders;

    @InjectMocks
    ReportService service;

    @Test
    void shouldReturnZeroWhenNoCompletedOrders() {
        when(serviceOrders.findAll()).thenReturn(List.of());

        var result = service.averageExecutionTime();

        assertThat(result.finalizedOrders()).isZero();
        assertThat(result.averageExecutionMinutes()).isZero();
        assertThat(result.averageExecutionHours()).isZero();
    }

    @Test
    void shouldIgnoreOrdersWithoutTimestamps() {
        var order = createOrder("OS-001");
        when(serviceOrders.findAll()).thenReturn(List.of(order));

        var result = service.averageExecutionTime();

        assertThat(result.finalizedOrders()).isZero();
    }

    @Test
    void shouldCalculateAverageExecutionTime() {
        var order1 = createOrder("OS-001");
        ReflectionTestUtils.setField(order1, "startedAt", OffsetDateTime.parse("2026-01-01T10:00:00+00:00"));
        ReflectionTestUtils.setField(order1, "finalizedAt", OffsetDateTime.parse("2026-01-01T12:00:00+00:00"));

        var order2 = createOrder("OS-002");
        ReflectionTestUtils.setField(order2, "startedAt", OffsetDateTime.parse("2026-01-01T08:00:00+00:00"));
        ReflectionTestUtils.setField(order2, "finalizedAt", OffsetDateTime.parse("2026-01-01T10:00:00+00:00"));

        when(serviceOrders.findAll()).thenReturn(List.of(order1, order2));

        var result = service.averageExecutionTime();

        assertThat(result.finalizedOrders()).isEqualTo(2);
        assertThat(result.averageExecutionMinutes()).isEqualTo(120.0);
        assertThat(result.averageExecutionHours()).isEqualTo(2.0);
    }

    private ServiceOrder createOrder(String code) {
        var customer = new Customer("João", "52998224725", null, null);
        var vehicle = new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020);
        var order = new ServiceOrder(code, customer, vehicle, null);
        order.changeStatus(ServiceOrderStatus.EM_DIAGNOSTICO, "diag");
        return order;
    }
}
