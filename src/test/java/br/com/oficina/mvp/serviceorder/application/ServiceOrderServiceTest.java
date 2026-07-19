package br.com.oficina.mvp.serviceorder.application;

import br.com.oficina.mvp.catalog.application.port.out.CatalogRepositoryPort;
import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;
import br.com.oficina.mvp.customer.application.port.out.CustomerRepositoryPort;
import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.shared.domain.ServiceOrderStatus;
import br.com.oficina.mvp.part.application.port.out.PartRepositoryPort;
import br.com.oficina.mvp.part.domain.Part;
import br.com.oficina.mvp.serviceorder.application.port.in.CreateServiceOrderCommand;
import br.com.oficina.mvp.serviceorder.application.port.out.ServiceOrderRepositoryPort;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import br.com.oficina.mvp.serviceorder.domain.WorkOrderPart;
import br.com.oficina.mvp.shared.exception.BusinessException;
import br.com.oficina.mvp.vehicle.application.port.out.VehicleRepositoryPort;
import br.com.oficina.mvp.vehicle.domain.Vehicle;
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
class ServiceOrderServiceTest {

    @Mock
    ServiceOrderRepositoryPort serviceOrders;
    @Mock
    CustomerRepositoryPort customers;
    @Mock
    VehicleRepositoryPort vehicles;
    @Mock
    CatalogRepositoryPort catalog;
    @Mock
    PartRepositoryPort parts;

    @InjectMocks
    ServiceOrderService service;

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
    void shouldListActiveOrdersByDefault() {
        var order = new ServiceOrder("OS-001", customer, vehicle, null);
        when(serviceOrders.findActiveOrderedByStatusPriority()).thenReturn(List.of(order));

        assertThat(service.list(false)).hasSize(1);
    }

    @Test
    void shouldListAllOrdersWhenAllIsTrue() {
        var order = new ServiceOrder("OS-001", customer, vehicle, null);
        when(serviceOrders.findAll()).thenReturn(List.of(order));

        assertThat(service.list(true)).hasSize(1);
    }

    @Test
    void shouldRejectInvalidDocumentOnCreate() {
        var command = validCommand("111.111.111-11");

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF/CNPJ inválido");
    }

    @Test
    void shouldRejectInvalidPlateOnCreate() {
        var command = new CreateServiceOrderCommand(
                "529.982.247-25",
                new CreateServiceOrderCommand.CustomerData("João", "joao@email.com", "11999999999"),
                new CreateServiceOrderCommand.VehicleData("INVALID", "Fiat", "Uno", 2020),
                null,
                List.of(new CreateServiceOrderCommand.ServiceItemData(1L, 1)),
                null
        );

        assertThatThrownBy(() -> service.create(command))
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

        var result = service.create(validCommand("529.982.247-25"));

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.AGUARDANDO_APROVACAO);
        assertThat(result.getCode()).startsWith("OS-");
    }

    @Test
    void shouldRetryCodeGenerationWhenCodeCollides() {
        when(customers.findByDocument("52998224725")).thenReturn(Optional.of(customer));
        when(vehicles.findByPlate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(catalog.findById(1L)).thenReturn(Optional.of(catalogItem));
        when(serviceOrders.existsByCode(any())).thenReturn(true, true, false);
        when(serviceOrders.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(validCommand("529.982.247-25"));

        assertThat(result.getCode()).startsWith("OS-");
    }

    @Test
    void shouldRejectCreationWhenCodeGenerationAttemptsAreExhausted() {
        when(customers.findByDocument("52998224725")).thenReturn(Optional.of(customer));
        when(vehicles.findByPlate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(serviceOrders.existsByCode(any())).thenReturn(true);

        assertThatThrownBy(() -> service.create(validCommand("529.982.247-25")))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void shouldCreateOrderWithExistingCustomerAndVehicle() {
        when(customers.findByDocument("52998224725")).thenReturn(Optional.of(customer));
        when(vehicles.findByPlate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(catalog.findById(1L)).thenReturn(Optional.of(catalogItem));
        when(serviceOrders.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(validCommand("529.982.247-25"));

        assertThat(result.getCustomer().getName()).isEqualTo("João Silva");
    }

    @Test
    void shouldRejectInactiveService() {
        var inactive = new ServiceCatalogItem("Inativo", null, new BigDecimal("10.00"), 30, false);
        when(customers.findByDocument("52998224725")).thenReturn(Optional.of(customer));
        when(vehicles.findByPlate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(catalog.findById(1L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.create(validCommand("529.982.247-25")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("serviços");
    }

    @Test
    void shouldRejectMissingService() {
        when(customers.findByDocument("52998224725")).thenReturn(Optional.of(customer));
        when(vehicles.findByPlate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(catalog.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(validCommand("529.982.247-25")))
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

        var command = new CreateServiceOrderCommand(
                "529.982.247-25",
                new CreateServiceOrderCommand.CustomerData("João", "joao@email.com", "11999999999"),
                new CreateServiceOrderCommand.VehicleData("ABC-1234", "Fiat", "Uno", 2020),
                null,
                List.of(new CreateServiceOrderCommand.ServiceItemData(1L, 1)),
                List.of(new CreateServiceOrderCommand.PartItemData(1L, 1))
        );

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("peças");
    }

    @Test
    void shouldApproveOrderAndDecrementStock() {
        var order = orderWaitingApproval();
        order.addPart(new WorkOrderPart(part, 2));
        ReflectionTestUtils.setField(order, "id", 1L);
        when(serviceOrders.findById(1L)).thenReturn(Optional.of(order));

        var result = service.decideApproval(1L, true, "Aprovado");

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.EM_EXECUCAO);
        assertThat(part.getStockQuantity()).isEqualTo(8);
    }

    @Test
    void shouldRejectApprovalWithInsufficientStock() {
        var order = orderWaitingApproval();
        part = new Part("Filtro", "FLT-001", new BigDecimal("35.00"), 1, 2, true);
        ReflectionTestUtils.setField(part, "id", 1L);
        order.addPart(new WorkOrderPart(part, 2));
        ReflectionTestUtils.setField(order, "id", 1L);
        when(serviceOrders.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.decideApproval(1L, true, "Aprovado"))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT));
    }

    @Test
    void shouldRejectOrderWithoutTouchingStock() {
        var order = orderWaitingApproval();
        order.addPart(new WorkOrderPart(part, 2));
        ReflectionTestUtils.setField(order, "id", 1L);
        when(serviceOrders.findById(1L)).thenReturn(Optional.of(order));

        var result = service.decideApproval(1L, false, "Cliente recusou o orçamento");

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.RECUSADA);
        assertThat(part.getStockQuantity()).isEqualTo(10);
    }

    @Test
    void shouldUpdateStatus() {
        var order = new ServiceOrder("OS-010", customer, vehicle, null);
        ReflectionTestUtils.setField(order, "id", 1L);
        when(serviceOrders.findById(1L)).thenReturn(Optional.of(order));

        var result = service.updateStatus(1L, ServiceOrderStatus.EM_DIAGNOSTICO, "Diagnóstico");

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.EM_DIAGNOSTICO);
    }

    @Test
    void shouldUpdateDiagnosis() {
        var order = new ServiceOrder("OS-011", customer, vehicle, null);
        ReflectionTestUtils.setField(order, "id", 1L);
        when(serviceOrders.findById(1L)).thenReturn(Optional.of(order));

        var result = service.updateDiagnosis(1L, "Pastilhas de freio desgastadas.");

        assertThat(result.getDiagnosis()).isEqualTo("Pastilhas de freio desgastadas.");
    }

    @Test
    void shouldThrowWhenUpdatingDiagnosisForMissingOrder() {
        when(serviceOrders.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDiagnosis(99L, "Diagnóstico"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ordem de serviço não encontrada");
    }

    @Test
    void shouldFindPublicByCode() {
        var order = orderWaitingApproval();
        when(serviceOrders.findByCode("OS-PUBLIC")).thenReturn(Optional.of(order));

        var result = service.findByCode("OS-PUBLIC", "529.982.247-25");

        assertThat(result.getCode()).isEqualTo("OS-PUBLIC");
    }

    @Test
    void shouldRejectPublicLookupWithWrongDocument() {
        var order = orderWaitingApproval();
        when(serviceOrders.findByCode("OS-PUBLIC")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.findByCode("OS-PUBLIC", "111.111.111-11"))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void shouldApproveByCustomer() {
        var order = orderWaitingApproval();
        when(serviceOrders.findByCode("OS-PUBLIC")).thenReturn(Optional.of(order));

        var result = service.decideApprovalByCustomer("OS-PUBLIC", "529.982.247-25", true, null);

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.EM_EXECUCAO);
    }

    @Test
    void shouldRejectByCustomerWithoutTouchingStock() {
        var order = orderWaitingApproval();
        order.addPart(new WorkOrderPart(part, 2));
        when(serviceOrders.findByCode("OS-PUBLIC")).thenReturn(Optional.of(order));

        var result = service.decideApprovalByCustomer("OS-PUBLIC", "529.982.247-25", false, "Cliente recusou o orçamento");

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.RECUSADA);
        assertThat(part.getStockQuantity()).isEqualTo(10);
    }

    @Test
    void shouldRejectCustomerApprovalWithWrongDocument() {
        var order = orderWaitingApproval();
        when(serviceOrders.findByCode("OS-PUBLIC")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.decideApprovalByCustomer("OS-PUBLIC", "111.111.111-11", true, null))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void shouldRejectCustomerApprovalWhenNotWaiting() {
        var order = new ServiceOrder("OS-PUBLIC", customer, vehicle, null);
        when(serviceOrders.findByCode("OS-PUBLIC")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.decideApprovalByCustomer("OS-PUBLIC", "529.982.247-25", true, null))
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

        var command = new CreateServiceOrderCommand(
                "529.982.247-25",
                new CreateServiceOrderCommand.CustomerData("João Silva", "joao@email.com", "11999999999"),
                new CreateServiceOrderCommand.VehicleData("ABC-1234", "Fiat", "Uno", 2020),
                null,
                List.of(new CreateServiceOrderCommand.ServiceItemData(1L, 1)),
                List.of(new CreateServiceOrderCommand.PartItemData(1L, 2))
        );

        var result = service.create(command);

        assertThat(result.getParts()).hasSize(1);
    }

    @Test
    void shouldRejectPublicLookupWhenOrderNotFound() {
        when(serviceOrders.findByCode("INEXISTENTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("INEXISTENTE", "529.982.247-25"))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void shouldApproveByCustomerWithCustomComment() {
        var order = orderWaitingApproval();
        when(serviceOrders.findByCode("OS-PUBLIC")).thenReturn(Optional.of(order));

        var result = service.decideApprovalByCustomer("OS-PUBLIC", "529.982.247-25", true, "Aprovado via portal");

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.EM_EXECUCAO);
    }

    @Test
    void shouldRejectCustomerApprovalWhenOrderNotFound() {
        when(serviceOrders.findByCode("INEXISTENTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.decideApprovalByCustomer("INEXISTENTE", "529.982.247-25", true, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não encontrada");
    }

    @Test
    void shouldRejectMissingPart() {
        when(customers.findByDocument("52998224725")).thenReturn(Optional.of(customer));
        when(vehicles.findByPlate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(catalog.findById(1L)).thenReturn(Optional.of(catalogItem));
        when(parts.findById(1L)).thenReturn(Optional.empty());

        var command = new CreateServiceOrderCommand(
                "529.982.247-25",
                new CreateServiceOrderCommand.CustomerData("João", "joao@email.com", "11999999999"),
                new CreateServiceOrderCommand.VehicleData("ABC-1234", "Fiat", "Uno", 2020),
                null,
                List.of(new CreateServiceOrderCommand.ServiceItemData(1L, 1)),
                List.of(new CreateServiceOrderCommand.PartItemData(1L, 1))
        );

        assertThatThrownBy(() -> service.create(command))
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

    private CreateServiceOrderCommand validCommand(String document) {
        return new CreateServiceOrderCommand(
                document,
                new CreateServiceOrderCommand.CustomerData("João Silva", "joao@email.com", "11999999999"),
                new CreateServiceOrderCommand.VehicleData("ABC-1234", "Fiat", "Uno", 2020),
                "Barulho no motor",
                List.of(new CreateServiceOrderCommand.ServiceItemData(1L, 1)),
                null
        );
    }
}
