package br.com.oficina.mvp.services;

import br.com.oficina.mvp.domains.Customer;
import br.com.oficina.mvp.domains.Vehicle;
import br.com.oficina.mvp.dtos.VehicleRequestDto;
import br.com.oficina.mvp.infra.VehicleRepository;
import br.com.oficina.mvp.shared.exception.BusinessException;
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
    VehicleRepository vehicles;

    @Mock
    CustomerService customerService;

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
        var request = new VehicleRequestDto(1L, "ABC-1234", "Fiat", "Uno", 2020);
        when(customerService.findEntity(1L)).thenReturn(customer);
        when(vehicles.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(request);

        assertThat(result.plate()).isEqualTo("ABC1234");
    }

    @Test
    void shouldRejectInvalidPlate() {
        var request = new VehicleRequestDto(1L, "INVALID", "Fiat", "Uno", 2020);
        when(customerService.findEntity(1L)).thenReturn(new Customer("João", "52998224725", null, null));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void shouldUpdateVehicle() {
        var customer = new Customer("João", "52998224725", null, null);
        var vehicle = new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020);
        when(vehicles.findById(1L)).thenReturn(Optional.of(vehicle));
        when(customerService.findEntity(1L)).thenReturn(customer);

        var request = new VehicleRequestDto(1L, "ABC1D23", "VW", "Gol", 2021);
        var result = service.update(1L, request);

        assertThat(result.plate()).isEqualTo("ABC1D23");
        assertThat(result.brand()).isEqualTo("VW");
    }

    @Test
    void shouldFindById() {
        var customer = new Customer("João", "52998224725", null, null);
        var vehicle = new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020);
        when(vehicles.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThat(service.findById(1L).plate()).isEqualTo("ABC1234");
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
