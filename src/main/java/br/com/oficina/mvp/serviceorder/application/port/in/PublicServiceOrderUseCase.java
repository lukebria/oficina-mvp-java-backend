package br.com.oficina.mvp.serviceorder.application.port.in;

import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;

public interface PublicServiceOrderUseCase {
    ServiceOrder findByCode(String code, String document);

    ServiceOrder approveByCustomer(String code, String document, String comment);
}
