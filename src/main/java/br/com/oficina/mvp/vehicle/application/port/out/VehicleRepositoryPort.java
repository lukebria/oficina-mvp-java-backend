package br.com.oficina.mvp.vehicle.application.port.out;

import br.com.oficina.mvp.vehicle.domain.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleRepositoryPort {
    List<Vehicle> findAll();

    Optional<Vehicle> findById(Long id);

    Optional<Vehicle> findByPlate(String plate);

    Vehicle save(Vehicle vehicle);

    void delete(Vehicle vehicle);
}
