package br.com.oficina.mvp.serviceorder.adapter.out.persistence;

import br.com.oficina.mvp.serviceorder.application.port.out.ServiceOrderRepositoryPort;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// @Transactional na classe: os métodos montam o domínio puro navegando um grafo de associações lazy
// (customer, vehicle, services->serviceItem, parts->part, history), então precisam de uma sessão Hibernate
// viva durante toda a execução do método — não podem depender de um @Transactional externo (ex: quando o
// port é chamado direto, sem passar pelo service, como em vários testes de integração).
@Component
@Transactional
class ServiceOrderPersistenceAdapter implements ServiceOrderRepositoryPort {
    private final ServiceOrderJpaRepository jpaRepository;
    private final EntityManager entityManager;

    ServiceOrderPersistenceAdapter(ServiceOrderJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<ServiceOrder> findAll() {
        return jpaRepository.findAll().stream().map(ServiceOrderMapper::toDomain).toList();
    }

    @Override
    public List<ServiceOrder> findActiveOrderedByStatusPriority() {
        return jpaRepository.findActiveOrderedByStatusPriority().stream().map(ServiceOrderMapper::toDomain).toList();
    }

    @Override
    public Optional<ServiceOrder> findById(Long id) {
        return jpaRepository.findById(id).map(ServiceOrderMapper::toDomain);
    }

    @Override
    public Optional<ServiceOrder> findByCode(String code) {
        return jpaRepository.findByCode(code).map(ServiceOrderMapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public ServiceOrder save(ServiceOrder order) {
        ServiceOrderJpaEntity entity;
        if (order.getId() == null) {
            entity = ServiceOrderMapper.toNewEntity(order, entityManager);
        } else {
            entity = jpaRepository.getReferenceById(order.getId());
            ServiceOrderMapper.applyToEntity(order, entity);
        }
        return ServiceOrderMapper.toDomain(jpaRepository.save(entity));
    }
}
