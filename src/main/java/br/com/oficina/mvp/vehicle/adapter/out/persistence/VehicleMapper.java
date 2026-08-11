package br.com.oficina.mvp.vehicle.adapter.out.persistence;

import br.com.oficina.mvp.customer.adapter.out.persistence.CustomerJpaEntity;
import br.com.oficina.mvp.customer.adapter.out.persistence.CustomerMapper;
import br.com.oficina.mvp.vehicle.domain.Vehicle;

public final class VehicleMapper {
    private VehicleMapper() {}

    public static Vehicle toDomain(VehicleJpaEntity entity) {
        return new Vehicle(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                CustomerMapper.toDomain(entity.getCustomer()),
                entity.getPlate(),
                entity.getBrand(),
                entity.getModel(),
                entity.getManufacturingYear()
        );
    }

    static VehicleJpaEntity toNewEntity(Vehicle domain, CustomerJpaEntity customerRef) {
        return new VehicleJpaEntity(customerRef, domain.getPlate(), domain.getBrand(), domain.getModel(), domain.getManufacturingYear());
    }

    static void applyToEntity(Vehicle domain, VehicleJpaEntity entity, CustomerJpaEntity customerRef) {
        entity.setCustomer(customerRef);
        entity.setPlate(domain.getPlate());
        entity.setBrand(domain.getBrand());
        entity.setModel(domain.getModel());
        entity.setManufacturingYear(domain.getManufacturingYear());
    }
}
