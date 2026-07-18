package br.com.oficina.mvp.serviceorder.adapter.out.persistence;

import br.com.oficina.mvp.serviceorder.application.port.out.ServiceOrderRepositoryPort;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import org.hibernate.Hibernate;
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
        return jpaRepository.findAll().stream().map(this::initialize).toList();
    }

    @Override
    public Optional<ServiceOrder> findById(Long id) {
        return jpaRepository.findById(id).map(this::initialize);
    }

    @Override
    public Optional<ServiceOrder> findByCode(String code) {
        return jpaRepository.findByCode(code).map(this::initialize);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public ServiceOrder save(ServiceOrder order) {
        return initialize(jpaRepository.save(order));
    }

    // open-in-view é false: sem isso, associações lazy quebram ao serem lidas fora da transação (ex: no controller).
    // Coleções são inicializadas em consultas separadas (não JOIN FETCH) para evitar MultipleBagFetchException do Hibernate.
    private ServiceOrder initialize(ServiceOrder order) {
        Hibernate.initialize(order.getCustomer());
        Hibernate.initialize(order.getVehicle());
        Hibernate.initialize(order.getServices());
        order.getServices().forEach(item -> Hibernate.initialize(item.getServiceItem()));
        Hibernate.initialize(order.getParts());
        order.getParts().forEach(item -> Hibernate.initialize(item.getPart()));
        Hibernate.initialize(order.getHistory());
        return order;
    }
}
