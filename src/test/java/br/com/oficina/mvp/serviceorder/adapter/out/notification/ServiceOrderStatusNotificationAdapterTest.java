package br.com.oficina.mvp.serviceorder.adapter.out.notification;

import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import br.com.oficina.mvp.vehicle.domain.Vehicle;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class ServiceOrderStatusNotificationAdapterTest {

    private final ServiceOrderStatusNotificationAdapter adapter = new ServiceOrderStatusNotificationAdapter();

    @Test
    void shouldNotThrowWhenCustomerHasEmail() {
        var customer = new Customer("João Silva", "52998224725", "joao@email.com", "11999999999");
        var vehicle = new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020);
        var order = new ServiceOrder("OS-001", customer, vehicle, null);

        assertThatCode(() -> adapter.notifyStatusChanged(order)).doesNotThrowAnyException();
    }

    @Test
    void shouldNotThrowWhenCustomerHasNoEmail() {
        var customer = new Customer("João Silva", "52998224725", null, "11999999999");
        var vehicle = new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020);
        var order = new ServiceOrder("OS-001", customer, vehicle, null);

        assertThatCode(() -> adapter.notifyStatusChanged(order)).doesNotThrowAnyException();
    }
}
