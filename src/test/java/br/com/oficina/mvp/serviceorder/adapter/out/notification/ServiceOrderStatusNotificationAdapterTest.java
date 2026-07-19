package br.com.oficina.mvp.serviceorder.adapter.out.notification;

import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import br.com.oficina.mvp.vehicle.domain.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ServiceOrderStatusNotificationAdapterTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final ServiceOrderStatusNotificationAdapter adapter =
            new ServiceOrderStatusNotificationAdapter(mailSender, "no-reply@oficina.com");

    @Test
    void shouldSendEmailWhenCustomerHasEmail() {
        var customer = new Customer("João Silva", "52998224725", "joao@email.com", "11999999999");
        var vehicle = new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020);
        var order = new ServiceOrder("OS-001", customer, vehicle, null);

        adapter.notifyStatusChanged(order);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldNotSendEmailWhenCustomerHasNoEmail() {
        var customer = new Customer("João Silva", "52998224725", null, "11999999999");
        var vehicle = new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020);
        var order = new ServiceOrder("OS-001", customer, vehicle, null);

        assertThatCode(() -> adapter.notifyStatusChanged(order)).doesNotThrowAnyException();

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldNotThrowWhenMailSendingFails() {
        var customer = new Customer("João Silva", "52998224725", "joao@email.com", "11999999999");
        var vehicle = new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020);
        var order = new ServiceOrder("OS-001", customer, vehicle, null);
        willThrow(new MailSendException("erro simulado")).given(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> adapter.notifyStatusChanged(order)).doesNotThrowAnyException();
    }
}
