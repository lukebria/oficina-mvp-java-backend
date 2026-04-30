package br.com.oficina.mvp.vehicles.application;

import br.com.oficina.mvp.customers.application.CustomerService;
import br.com.oficina.mvp.shared.exception.BusinessException;
import br.com.oficina.mvp.shared.validation.PlateValidator;
import br.com.oficina.mvp.vehicles.api.dto.VehicleRequest;
import br.com.oficina.mvp.vehicles.api.dto.VehicleResponse;
import br.com.oficina.mvp.vehicles.domain.Vehicle;
import br.com.oficina.mvp.vehicles.infra.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleService {
    private final VehicleRepository vehicles;
    private final CustomerService customerService;

    public VehicleService(VehicleRepository vehicles, CustomerService customerService) {
        this.vehicles = vehicles;
        this.customerService = customerService;
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> list() {
        return vehicles.findAll().stream().map(VehicleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse findById(Long id) { return VehicleResponse.from(findEntity(id)); }

    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        var customer = customerService.findEntity(request.customerId());
        var plate = PlateValidator.normalize(request.plate());
        validatePlate(plate);
        return VehicleResponse.from(vehicles.save(new Vehicle(customer, plate, request.brand(), request.model(), request.year())));
    }

    @Transactional
    public VehicleResponse update(Long id, VehicleRequest request) {
        var vehicle = findEntity(id);
        var customer = customerService.findEntity(request.customerId());
        var plate = PlateValidator.normalize(request.plate());
        validatePlate(plate);
        vehicle.update(customer, plate, request.brand(), request.model(), request.year());
        return VehicleResponse.from(vehicle);
    }

    @Transactional
    public void delete(Long id) { vehicles.delete(findEntity(id)); }

    public Vehicle findEntity(Long id) {
        return vehicles.findById(id)
                .orElseThrow(() -> new BusinessException("Veículo não encontrado.", HttpStatus.NOT_FOUND));
    }

    private void validatePlate(String plate) {
        if (!PlateValidator.isValidBrazilianPlate(plate)) {
            throw new BusinessException("Placa de veículo inválida.", HttpStatus.BAD_REQUEST);
        }
    }
}
