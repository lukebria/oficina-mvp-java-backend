package br.com.oficina.mvp.vehicle.application.port.in;

import br.com.oficina.mvp.vehicle.domain.Vehicle;

import java.util.List;

public interface VehicleUseCase {
    List<Vehicle> list();

    Vehicle findById(Long id);

    Vehicle create(VehicleCommand command);

    Vehicle update(Long id, VehicleCommand command);

    void delete(Long id);
}
