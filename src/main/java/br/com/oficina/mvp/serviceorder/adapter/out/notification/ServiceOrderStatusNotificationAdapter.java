package br.com.oficina.mvp.serviceorder.adapter.out.notification;

import br.com.oficina.mvp.serviceorder.application.port.out.ServiceOrderNotificationPort;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// MVP: só loga a notificação (canal definido é e-mail). Trocar por um envio real de e-mail é uma questão de
// implementar esta mesma porta com um EmailSender de verdade, sem tocar em domínio/application.
@Component
class ServiceOrderStatusNotificationAdapter implements ServiceOrderNotificationPort {
    private static final Logger log = LoggerFactory.getLogger(ServiceOrderStatusNotificationAdapter.class);

    @Override
    public void notifyStatusChanged(ServiceOrder order) {
        var email = order.getCustomer().getEmail();
        if (email == null || email.isBlank()) {
            log.warn("OS {} mudou para o status {}, mas o cliente {} não tem e-mail cadastrado; notificação não enviada.",
                    order.getCode(), order.getStatus(), order.getCustomer().getName());
            return;
        }
        log.info("Notificando cliente {} sobre OS {}: status alterado para {}.", email, order.getCode(), order.getStatus());
    }
}
