package br.com.oficina.mvp.vehicle.adapter.out.persistence;

import br.com.oficina.mvp.customer.adapter.out.persistence.CustomerJpaEntity;
import br.com.oficina.mvp.vehicle.application.port.out.VehicleRepositoryPort;
import br.com.oficina.mvp.vehicle.domain.Vehicle;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// @Transactional na classe: os métodos montam o domínio puro navegando associações lazy da entidade JPA
// (Vehicle -> Customer), então precisam de uma sessão Hibernate viva durante toda a execução do método —
// não podem depender de um @Transactional externo (ex: quando o port é chamado direto, sem passar pelo service).
@Component
@Transactional
class VehiclePersistenceAdapter implements VehicleRepositoryPort {
    private final VehicleJpaRepository jpaRepository;
    private final EntityManager entityManager;

    VehiclePersistenceAdapter(VehicleJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<Vehicle> findAll() {
        return jpaRepository.findAll().stream().map(VehicleMapper::toDomain).toList();
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        return jpaRepository.findById(id).map(VehicleMapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findByPlate(String plate) {
        return jpaRepository.findByPlate(plate).map(VehicleMapper::toDomain);
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        var customerRef = entityManager.getReference(CustomerJpaEntity.class, vehicle.getCustomer().getId());
        VehicleJpaEntity entity;
        if (vehicle.getId() == null) {
            entity = VehicleMapper.toNewEntity(vehicle, customerRef);
        } else {
            entity = jpaRepository.getReferenceById(vehicle.getId());
            VehicleMapper.applyToEntity(vehicle, entity, customerRef);
        }
        return VehicleMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public void delete(Vehicle vehicle) {
        jpaRepository.deleteById(vehicle.getId());
    }
}
