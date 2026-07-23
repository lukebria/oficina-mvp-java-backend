package br.com.oficina.mvp.vehicle.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, Long> {
    Optional<VehicleJpaEntity> findByPlate(String plate);
}
