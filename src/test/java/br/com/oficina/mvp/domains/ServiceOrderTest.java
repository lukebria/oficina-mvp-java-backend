package br.com.oficina.mvp.domains;

import br.com.oficina.mvp.dtos.enums.ServiceOrderStatus;
import br.com.oficina.mvp.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderTest {

    private Customer customer;
    private Vehicle vehicle;
    private ServiceCatalogItem catalogItem;
    private Part part;

    @BeforeEach
    void setUp() {
        customer = new Customer("João Silva", "52998224725", "joao@email.com", "11999999999");
        vehicle = new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020);
        catalogItem = new ServiceCatalogItem("Troca de óleo", "Completa", new BigDecimal("150.00"), 60, true);
        part = new Part("Filtro de óleo", "FLT-001", new BigDecimal("35.00"), 10, 2, true);
    }

    @Test
    void shouldCreateOrderWithInitialStatus() {
        var order = new ServiceOrder("OS-001", customer, vehicle, "Barulho no motor");

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.RECEBIDA);
        assertThat(order.getCode()).isEqualTo("OS-001");
        assertThat(order.getHistory()).hasSize(1);
    }

    @Test
    void shouldRecalculateTotalsWhenAddingItems() {
        var order = new ServiceOrder("OS-002", customer, vehicle, null);
        order.addService(new WorkOrderService(catalogItem, 2));
        order.addPart(new WorkOrderPart(part, 1));

        assertThat(order.getTotalServices()).isEqualByComparingTo("300.00");
        assertThat(order.getTotalParts()).isEqualByComparingTo("35.00");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("335.00");
    }

    @Test
    void shouldMarkBudgetWaitingApproval() {
        var order = new ServiceOrder("OS-003", customer, vehicle, null);
        order.markBudgetWaitingApproval();

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.AGUARDANDO_APROVACAO);
    }

    @Test
    void shouldApproveOrder() {
        var order = new ServiceOrder("OS-004", customer, vehicle, null);
        order.markBudgetWaitingApproval();

        order.approve("Cliente aprovou o orçamento");

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.EM_EXECUCAO);
        assertThat(order.getApprovedAt()).isNotNull();
        assertThat(order.getStartedAt()).isNotNull();
    }

    @Test
    void shouldApproveWithDefaultCommentWhenBlank() {
        var order = new ServiceOrder("OS-005", customer, vehicle, null);
        order.markBudgetWaitingApproval();

        order.approve("  ");

        assertThat(order.getHistory()).anyMatch(h ->
                h.getStatus() == ServiceOrderStatus.EM_EXECUCAO
                        && h.getComment().contains("Orçamento aprovado"));
    }

    @Test
    void shouldChangeStatusThroughWorkflow() {
        var order = new ServiceOrder("OS-006", customer, vehicle, null);

        order.changeStatus(ServiceOrderStatus.EM_DIAGNOSTICO, "Iniciando diagnóstico");
        order.changeStatus(ServiceOrderStatus.AGUARDANDO_APROVACAO, "Orçamento pronto");
        order.changeStatus(ServiceOrderStatus.EM_EXECUCAO, "Execução iniciada");
        order.changeStatus(ServiceOrderStatus.FINALIZADA, "Serviço concluído");
        order.changeStatus(ServiceOrderStatus.ENTREGUE, "Veículo entregue");

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.ENTREGUE);
        assertThat(order.getStartedAt()).isNotNull();
        assertThat(order.getFinalizedAt()).isNotNull();
        assertThat(order.getDeliveredAt()).isNotNull();
    }

    @Test
    void shouldRejectInvalidStatusChange() {
        var order = new ServiceOrder("OS-007", customer, vehicle, null);

        assertThatThrownBy(() -> order.changeStatus(ServiceOrderStatus.ENTREGUE, "inválido"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldUseDefaultQuantityForService() {
        var order = new ServiceOrder("OS-008", customer, vehicle, null);
        order.addService(new WorkOrderService(catalogItem, null));

        assertThat(order.getServices().getFirst().getQuantity()).isEqualTo(1);
    }
}
