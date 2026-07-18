package br.com.oficina.mvp.vehicle.adapter.out.persistence;

import br.com.oficina.mvp.vehicle.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface VehicleJpaRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByPlate(String plate);
}
