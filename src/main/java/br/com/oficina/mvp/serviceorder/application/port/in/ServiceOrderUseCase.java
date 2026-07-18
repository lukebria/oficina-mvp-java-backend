package br.com.oficina.mvp.serviceorder.application.port.in;

import br.com.oficina.mvp.shared.domain.ServiceOrderStatus;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;

import java.util.List;

public interface ServiceOrderUseCase {
    List<ServiceOrder> list();

    ServiceOrder findById(Long id);

    ServiceOrder create(CreateServiceOrderCommand command);

    ServiceOrder approve(Long id, String comment);

    ServiceOrder updateStatus(Long id, ServiceOrderStatus status, String comment);

    ServiceOrder updateDiagnosis(Long id, String diagnosis);
}
