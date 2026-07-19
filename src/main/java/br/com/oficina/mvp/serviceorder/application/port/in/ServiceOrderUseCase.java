package br.com.oficina.mvp.serviceorder.application.port.in;

import br.com.oficina.mvp.shared.domain.ServiceOrderStatus;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;

import java.util.List;

public interface ServiceOrderUseCase {
    List<ServiceOrder> list(boolean all);

    ServiceOrder findById(Long id);

    ServiceOrder create(CreateServiceOrderCommand command);

    ServiceOrder decideApproval(Long id, boolean approved, String comment);

    ServiceOrder updateStatus(Long id, ServiceOrderStatus status, String comment);

    ServiceOrder updateDiagnosis(Long id, String diagnosis);
}
