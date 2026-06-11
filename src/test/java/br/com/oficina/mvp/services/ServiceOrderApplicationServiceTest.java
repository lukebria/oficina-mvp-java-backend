package br.com.oficina.mvp.services;

import br.com.oficina.mvp.domains.Customer;
import br.com.oficina.mvp.domains.Part;
import br.com.oficina.mvp.domains.ServiceCatalogItem;
import br.com.oficina.mvp.domains.ServiceOrder;
import br.com.oficina.mvp.domains.Vehicle;
import br.com.oficina.mvp.domains.WorkOrderPart;
import br.com.oficina.mvp.dtos.CreateServiceOrderRequestDto;
import br.com.oficina.mvp.dtos.enums.ServiceOrderStatus;
import br.com.oficina.mvp.infra.CustomerRepository;
import br.com.oficina.mvp.infra.PartRepository;
import br.com.oficina.mvp.infra.ServiceCatalogItemRepository;
import br.com.oficina.mvp.infra.ServiceOrderRepository;
import br.com.oficina.mvp.infra.VehicleRepository;
import br.com.oficina.mvp.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderApplicationServiceTest {

    @Mock
    ServiceOrderRepository serviceOrders;
    @Mock
    CustomerRepository customers;
    @Mock
    VehicleRepository vehicles;
    @Mock
    ServiceCatalogItemRepository catalog;
    @Mock
    PartRepository parts;

    @InjectMocks
    ServiceOrderApplicationService service;

    private Customer customer;
    private Vehicle vehicle;
    private ServiceCatalogItem catalogItem;
    private Part part;

    @BeforeEach
    void setUp() {
        customer = new Customer("João Silva", "52998224725", "joao@email.com", "11999999999");
        vehicle = new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020);
        catalogItem = new ServiceCatalogItem("Troca de óleo", "Completa", new BigDecimal("150.00"), 60, true);
        part = new Part("Filtro", "FLT-001", new BigDecimal("35.00"), 10, 2, true);
        ReflectionTestUtils.setField(catalogItem, "id", 1L);
        ReflectionTestUtils.setField(part, "id", 1L);
    }

    @Test
    void shouldListOrders() {
        var order = new ServiceOrder("OS-001", customer, vehicle, null);
        when(serviceOrders.findAll()).thenReturn(List.of(order));

        assertThat(service.list()).hasSize(1);
    }

    @Test
    void shouldRejectInvalidDocumentOnCreate() {
        var request = validRequest("111.111.111-11");

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF/CNPJ inválido");
    }

    @Test
    void shouldRejectInvalidPlateOnCreate() {
        var request = new CreateServiceOrderRequestDto(
                "529.982.247-25",
                new CreateServiceOrderRequestDto.CustomerData("João", "joao@email.com", "11999999999"),
                new CreateServiceOrderRequestDto.VehicleData("INVALID", "Fiat", "Uno", 2020),
                null,
                List.of(new CreateServiceOrderRequestDto.ServiceItemData(1L, 1)),
                null
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Placa");
    }

    @Test
    void shouldCreateOrderWithNewCustomerAndVehicle() {
        when(customers.findByDocument("52998224725")).thenReturn(Optional.empty());
        when(customers.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vehicles.findByPlate("ABC1234")).thenReturn(Optional.empty());
        when(vehicles.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(catalog.findById(1L)).thenReturn(Optional.of(catalogItem));
        when(serviceOrders.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(validRequest("529.982.247-25"));

        assertThat(result.status()).isEqualTo(ServiceOrderStatus.AGUARDANDO_APROVACAO);
        assertThat(result.code()).startsWith("OS-");
    }

    @Test
    void shouldCreateOrderWithExistingCustomerAndVehicle() {
        when(customers.findByDocument("52998224725")).thenReturn(Optional.of(customer));
        when(vehicles.findByPlate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(catalog.findById(1L)).thenReturn(Optional.of(catalogItem));
        when(serviceOrders.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(validRequest("529.982.247-25"));

        assertThat(result.customer().name()).isEqualTo("João Silva");
    }

    @Test
    void shouldRejectInactiveService() {
        var inactive = new ServiceCatalogItem("Inativo", null, new BigDecimal("10.00"), 30, false);
        when(customers.findByDocument("52998224725")).thenReturn(Optional.of(customer));
        when(vehicles.findByPlate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(catalog.findById(1L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.create(validRequest("529.982.247-25")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("serviços");
    }

    @Test
    void shouldRejectMissingService() {
        when(customers.findByDocument("52998224725")).thenReturn(Optional.of(customer));
        when(vehicles.findByPlate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(catalog.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(validRequest("529.982.247-25")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("serviços");
    }

    @Test
    void shouldRejectInactivePart() {
        var inactivePart = new Part("Inativo", "IN-001", new BigDecimal("10.00"), 5, 1, false);
        when(customers.findByDocument("52998224725")).thenReturn(Optional.of(customer));
        when(vehicles.findByPlate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(catalog.findById(1L)).thenReturn(Optional.of(catalogItem));
        when(parts.findById(1L)).thenReturn(Optional.of(inactivePart));

        var request = new CreateServiceOrderRequestDto(
                "529.982.247-25",
                new CreateServiceOrderRequestDto.CustomerData("João", "joao@email.com", "11999999999"),
                new CreateServiceOrderRequestDto.VehicleData("ABC-1234", "Fiat", "Uno", 2020),
                null,
                List.of(new CreateServiceOrderRequestDto.ServiceItemData(1L, 1)),
                List.of(new CreateServiceOrderRequestDto.PartItemData(1L, 1))
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("peças");
    }

    @Test
    void shouldApproveOrderAndDecrementStock() {
        var order = orderWaitingApproval();
        order.addPart(new WorkOrderPart(part, 2));
        ReflectionTestUtils.setField(order, "id", 1L);
        when(serviceOrders.findById(1L)).thenReturn(Optional.of(order));

        var result = service.approve(1L, "Aprovado");

        assertThat(result.status()).isEqualTo(ServiceOrderStatus.EM_EXECUCAO);
        assertThat(part.getStockQuantity()).isEqualTo(8);
    }

    @Test
    void shouldRejectApprovalWithInsufficientStock() {
        var order = orderWaitingApproval();
        part = new Part("Filtro", "FLT-001", new BigDecimal("35.00"), 1, 2, true);
        order.addPart(new WorkOrderPart(part, 2));
        ReflectionTestUtils.setField(order, "id", 1L);
        when(serviceOrders.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.approve(1L, "Aprovado"))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT));
    }

    @Test
    void shouldUpdateStatus() {
        var order = new ServiceOrder("OS-010", customer, vehicle, null);
        ReflectionTestUtils.setField(order, "id", 1L);
        when(serviceOrders.findById(1L)).thenReturn(Optional.of(order));

        var result = service.updateStatus(1L, ServiceOrderStatus.EM_DIAGNOSTICO, "Diagnóstico");

        assertThat(result.status()).isEqualTo(ServiceOrderStatus.EM_DIAGNOSTICO);
    }

    @Test
    void shouldFindPublicByCode() {
        var order = orderWaitingApproval();
        when(serviceOrders.findByCode("OS-PUBLIC")).thenReturn(Optional.of(order));

        var result = service.findPublicByCode("OS-PUBLIC", "529.982.247-25");

        assertThat(result.code()).isEqualTo("OS-PUBLIC");
    }

    @Test
    void shouldRejectPublicLookupWithWrongDocument() {
        var order = orderWaitingApproval();
        when(serviceOrders.findByCode("OS-PUBLIC")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.findPublicByCode("OS-PUBLIC", "111.111.111-11"))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void shouldApproveByCustomer() {
        var order = orderWaitingApproval();
        when(serviceOrders.findByCode("OS-PUBLIC")).thenReturn(Optional.of(order));

        var result = service.approveByCustomer("OS-PUBLIC", "529.982.247-25", null);

        assertThat(result.status()).isEqualTo(ServiceOrderStatus.EM_EXECUCAO);
    }

    @Test
    void shouldRejectCustomerApprovalWithWrongDocument() {
        var order = orderWaitingApproval();
        when(serviceOrders.findByCode("OS-PUBLIC")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.approveByCustomer("OS-PUBLIC", "111.111.111-11", null))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void shouldRejectCustomerApprovalWhenNotWaiting() {
        var order = new ServiceOrder("OS-PUBLIC", customer, vehicle, null);
        when(serviceOrders.findByCode("OS-PUBLIC")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.approveByCustomer("OS-PUBLIC", "529.982.247-25", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("aguardando aprovação");
    }

    @Test
    void shouldCreateOrderWithParts() {
        when(customers.findByDocument("52998224725")).thenReturn(Optional.of(customer));
        when(vehicles.findByPlate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(catalog.findById(1L)).thenReturn(Optional.of(catalogItem));
        when(parts.findById(1L)).thenReturn(Optional.of(part));
        when(serviceOrders.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new CreateServiceOrderRequestDto(
                "529.982.247-25",
                new CreateServiceOrderRequestDto.CustomerData("João Silva", "joao@email.com", "11999999999"),
                new CreateServiceOrderRequestDto.VehicleData("ABC-1234", "Fiat", "Uno", 2020),
                null,
                List.of(new CreateServiceOrderRequestDto.ServiceItemData(1L, 1)),
                List.of(new CreateServiceOrderRequestDto.PartItemData(1L, 2))
        );

        var result = service.create(request);

        assertThat(result.parts()).hasSize(1);
    }

    @Test
    void shouldRejectPublicLookupWhenOrderNotFound() {
        when(serviceOrders.findByCode("INEXISTENTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findPublicByCode("INEXISTENTE", "529.982.247-25"))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void shouldApproveByCustomerWithCustomComment() {
        var order = orderWaitingApproval();
        when(serviceOrders.findByCode("OS-PUBLIC")).thenReturn(Optional.of(order));

        var result = service.approveByCustomer("OS-PUBLIC", "529.982.247-25", "Aprovado via portal");

        assertThat(result.status()).isEqualTo(ServiceOrderStatus.EM_EXECUCAO);
    }

    @Test
    void shouldRejectCustomerApprovalWhenOrderNotFound() {
        when(serviceOrders.findByCode("INEXISTENTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approveByCustomer("INEXISTENTE", "529.982.247-25", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não encontrada");
    }

    @Test
    void shouldRejectMissingPart() {
        when(customers.findByDocument("52998224725")).thenReturn(Optional.of(customer));
        when(vehicles.findByPlate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(catalog.findById(1L)).thenReturn(Optional.of(catalogItem));
        when(parts.findById(1L)).thenReturn(Optional.empty());

        var request = new CreateServiceOrderRequestDto(
                "529.982.247-25",
                new CreateServiceOrderRequestDto.CustomerData("João", "joao@email.com", "11999999999"),
                new CreateServiceOrderRequestDto.VehicleData("ABC-1234", "Fiat", "Uno", 2020),
                null,
                List.of(new CreateServiceOrderRequestDto.ServiceItemData(1L, 1)),
                List.of(new CreateServiceOrderRequestDto.PartItemData(1L, 1))
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("peças");
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(serviceOrders.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ordem de serviço não encontrada");
    }

    private ServiceOrder orderWaitingApproval() {
        var order = new ServiceOrder("OS-PUBLIC", customer, vehicle, null);
        order.markBudgetWaitingApproval();
        return order;
    }

    private CreateServiceOrderRequestDto validRequest(String document) {
        return new CreateServiceOrderRequestDto(
                document,
                new CreateServiceOrderRequestDto.CustomerData("João Silva", "joao@email.com", "11999999999"),
                new CreateServiceOrderRequestDto.VehicleData("ABC-1234", "Fiat", "Uno", 2020),
                "Barulho no motor",
                List.of(new CreateServiceOrderRequestDto.ServiceItemData(1L, 1)),
                null
        );
    }
}
