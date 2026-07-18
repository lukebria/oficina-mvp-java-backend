package br.com.oficina.mvp.vehicle.application;

import br.com.oficina.mvp.customer.application.port.in.CustomerUseCase;
import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.shared.exception.BusinessException;
import br.com.oficina.mvp.vehicle.application.port.in.VehicleCommand;
import br.com.oficina.mvp.vehicle.application.port.out.VehicleRepositoryPort;
import br.com.oficina.mvp.vehicle.domain.Vehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    VehicleRepositoryPort vehicles;

    @Mock
    CustomerUseCase customerUseCase;

    @InjectMocks
    VehicleService service;

    @Test
    void shouldListVehicles() {
        var customer = new Customer("João", "52998224725", null, null);
        when(vehicles.findAll()).thenReturn(List.of(new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020)));

        assertThat(service.list()).hasSize(1);
    }

    @Test
    void shouldCreateVehicle() {
        var customer = new Customer("João", "52998224725", null, null);
        var command = new VehicleCommand(1L, "ABC-1234", "Fiat", "Uno", 2020);
        when(customerUseCase.findById(1L)).thenReturn(customer);
        when(vehicles.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(command);

        assertThat(result.getPlate()).isEqualTo("ABC1234");
    }

    @Test
    void shouldRejectInvalidPlate() {
        var command = new VehicleCommand(1L, "INVALID", "Fiat", "Uno", 2020);
        when(customerUseCase.findById(1L)).thenReturn(new Customer("João", "52998224725", null, null));

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void shouldUpdateVehicle() {
        var customer = new Customer("João", "52998224725", null, null);
        var vehicle = new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020);
        when(vehicles.findById(1L)).thenReturn(Optional.of(vehicle));
        when(customerUseCase.findById(1L)).thenReturn(customer);

        var command = new VehicleCommand(1L, "ABC1D23", "VW", "Gol", 2021);
        var result = service.update(1L, command);

        assertThat(result.getPlate()).isEqualTo("ABC1D23");
        assertThat(result.getBrand()).isEqualTo("VW");
    }

    @Test
    void shouldFindById() {
        var customer = new Customer("João", "52998224725", null, null);
        var vehicle = new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020);
        when(vehicles.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThat(service.findById(1L).getPlate()).isEqualTo("ABC1234");
    }

    @Test
    void shouldThrowWhenNotFound() {
        when(vehicles.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Veículo não encontrado");
    }

    @Test
    void shouldDeleteVehicle() {
        var customer = new Customer("João", "52998224725", null, null);
        var vehicle = new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020);
        when(vehicles.findById(1L)).thenReturn(Optional.of(vehicle));

        service.delete(1L);

        verify(vehicles).delete(vehicle);
    }
}
