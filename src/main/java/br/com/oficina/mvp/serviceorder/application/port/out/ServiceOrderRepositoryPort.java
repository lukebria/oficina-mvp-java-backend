package br.com.oficina.mvp.serviceorder.application.port.out;

import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;

import java.util.List;
import java.util.Optional;

public interface ServiceOrderRepositoryPort {
    List<ServiceOrder> findAll();

    Optional<ServiceOrder> findById(Long id);

    Optional<ServiceOrder> findByCode(String code);

    ServiceOrder save(ServiceOrder order);
}
