package br.com.oficina.mvp.serviceorder.application.port.out;

import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;

public interface ServiceOrderNotificationPort {
    void notifyStatusChanged(ServiceOrder order);
}
