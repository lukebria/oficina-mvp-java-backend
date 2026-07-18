package br.com.oficina.mvp.vehicle.adapter.out.persistence;

import br.com.oficina.mvp.vehicle.application.port.out.VehicleRepositoryPort;
import br.com.oficina.mvp.vehicle.domain.Vehicle;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class VehiclePersistenceAdapter implements VehicleRepositoryPort {
    private final VehicleJpaRepository jpaRepository;

    VehiclePersistenceAdapter(VehicleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Vehicle> findAll() {
        return jpaRepository.findAll().stream().map(this::initialize).toList();
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        return jpaRepository.findById(id).map(this::initialize);
    }

    @Override
    public Optional<Vehicle> findByPlate(String plate) {
        return jpaRepository.findByPlate(plate).map(this::initialize);
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        return initialize(jpaRepository.save(vehicle));
    }

    @Override
    public void delete(Vehicle vehicle) {
        jpaRepository.delete(vehicle);
    }

    // open-in-view é false: sem isso, associações lazy quebram ao serem lidas fora da transação (ex: no controller)
    private Vehicle initialize(Vehicle vehicle) {
        Hibernate.initialize(vehicle.getCustomer());
        return vehicle;
    }
}
