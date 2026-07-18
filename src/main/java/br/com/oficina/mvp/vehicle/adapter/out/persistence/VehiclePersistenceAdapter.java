package br.com.oficina.mvp.vehicle.adapter.out.persistence;

import br.com.oficina.mvp.vehicle.application.port.out.VehicleRepositoryPort;
import br.com.oficina.mvp.vehicle.domain.Vehicle;
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
        return jpaRepository.findAll();
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Vehicle> findByPlate(String plate) {
        return jpaRepository.findByPlate(plate);
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        return jpaRepository.save(vehicle);
    }

    @Override
    public void delete(Vehicle vehicle) {
        jpaRepository.delete(vehicle);
    }
}
