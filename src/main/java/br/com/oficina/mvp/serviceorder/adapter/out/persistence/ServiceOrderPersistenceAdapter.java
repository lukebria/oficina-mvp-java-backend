package br.com.oficina.mvp.serviceorder.adapter.out.persistence;

import br.com.oficina.mvp.serviceorder.application.port.out.ServiceOrderRepositoryPort;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class ServiceOrderPersistenceAdapter implements ServiceOrderRepositoryPort {
    private final ServiceOrderJpaRepository jpaRepository;

    ServiceOrderPersistenceAdapter(ServiceOrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ServiceOrder> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<ServiceOrder> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ServiceOrder> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }

    @Override
    public ServiceOrder save(ServiceOrder order) {
        return jpaRepository.save(order);
    }
}
