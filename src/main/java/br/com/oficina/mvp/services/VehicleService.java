package br.com.oficina.mvp.services;

import br.com.oficina.mvp.shared.exception.BusinessException;
import br.com.oficina.mvp.shared.validation.PlateValidator;
import br.com.oficina.mvp.dtos.VehicleRequestDto;
import br.com.oficina.mvp.dtos.VehicleResponseDto;
import br.com.oficina.mvp.domains.Vehicle;
import br.com.oficina.mvp.infra.VehicleRepository;
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
    public List<VehicleResponseDto> list() {
        return vehicles.findAll().stream().map(VehicleResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponseDto findById(Long id) { return VehicleResponseDto.from(findEntity(id)); }

    @Transactional
    public VehicleResponseDto create(VehicleRequestDto request) {
        var customer = customerService.findEntity(request.customerId());
        var plate = PlateValidator.normalize(request.plate());
        validatePlate(plate);
        return VehicleResponseDto.from(vehicles.save(new Vehicle(customer, plate, request.brand(), request.model(), request.year())));
    }

    @Transactional
    public VehicleResponseDto update(Long id, VehicleRequestDto request) {
        var vehicle = findEntity(id);
        var customer = customerService.findEntity(request.customerId());
        var plate = PlateValidator.normalize(request.plate());
        validatePlate(plate);
        vehicle.update(customer, plate, request.brand(), request.model(), request.year());
        return VehicleResponseDto.from(vehicle);
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
