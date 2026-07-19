package br.com.oficina.mvp.serviceorder.adapter.out.notification;

import br.com.oficina.mvp.serviceorder.application.port.out.ServiceOrderNotificationPort;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
class ServiceOrderStatusNotificationAdapter implements ServiceOrderNotificationPort {
    private static final Logger log = LoggerFactory.getLogger(ServiceOrderStatusNotificationAdapter.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    ServiceOrderStatusNotificationAdapter(JavaMailSender mailSender, @Value("${app.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void notifyStatusChanged(ServiceOrder order) {
        var email = order.getCustomer().getEmail();
        if (email == null || email.isBlank()) {
            log.warn("OS {} mudou para o status {}, mas o cliente {} não tem e-mail cadastrado; notificação não enviada.",
                    order.getCode(), order.getStatus(), order.getCustomer().getName());
            return;
        }
        try {
            mailSender.send(buildMessage(order, email));
            log.info("Notificando cliente {} sobre OS {}: status alterado para {}.", email, order.getCode(), order.getStatus());
        } catch (MailException e) {
            log.error("Falha ao enviar e-mail de notificação da OS {} para {}.", order.getCode(), email, e);
        }
    }

    private SimpleMailMessage buildMessage(ServiceOrder order, String email) {
        var vehicle = order.getVehicle();
        var message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("OS %s - status atualizado para %s".formatted(order.getCode(), order.getStatus()));
        message.setText("""
                Olá, %s!

                Sua ordem de serviço %s (%s %s - placa %s) teve o status atualizado para: %s.

                Qualquer dúvida, entre em contato com a nossa oficina.
                """.formatted(order.getCustomer().getName(), order.getCode(), vehicle.getBrand(), vehicle.getModel(),
                vehicle.getPlate(), order.getStatus()));
        return message;
    }
}
