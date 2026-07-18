package br.com.oficina.mvp.vehicle.application;

import br.com.oficina.mvp.customer.application.port.in.CustomerUseCase;
import br.com.oficina.mvp.shared.exception.BusinessException;
import br.com.oficina.mvp.shared.validation.PlateValidator;
import br.com.oficina.mvp.vehicle.application.port.in.VehicleCommand;
import br.com.oficina.mvp.vehicle.application.port.in.VehicleUseCase;
import br.com.oficina.mvp.vehicle.application.port.out.VehicleRepositoryPort;
import br.com.oficina.mvp.vehicle.domain.Vehicle;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleService implements VehicleUseCase {
    private final VehicleRepositoryPort vehicles;
    private final CustomerUseCase customerUseCase;

    public VehicleService(VehicleRepositoryPort vehicles, CustomerUseCase customerUseCase) {
        this.vehicles = vehicles;
        this.customerUseCase = customerUseCase;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> list() {
        return vehicles.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicle findById(Long id) {
        return findEntity(id);
    }

    @Override
    @Transactional
    public Vehicle create(VehicleCommand command) {
        var customer = customerUseCase.findById(command.customerId());
        var plate = PlateValidator.requireValid(command.plate());
        return vehicles.save(new Vehicle(customer, plate, command.brand(), command.model(), command.manufacturingYear()));
    }

    @Override
    @Transactional
    public Vehicle update(Long id, VehicleCommand command) {
        var vehicle = findEntity(id);
        var customer = customerUseCase.findById(command.customerId());
        var plate = PlateValidator.requireValid(command.plate());
        vehicle.update(customer, plate, command.brand(), command.model(), command.manufacturingYear());
        return vehicle;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        vehicles.delete(findEntity(id));
    }

    private Vehicle findEntity(Long id) {
        return vehicles.findById(id)
                .orElseThrow(() -> new BusinessException("Veículo não encontrado.", HttpStatus.NOT_FOUND));
    }
}
