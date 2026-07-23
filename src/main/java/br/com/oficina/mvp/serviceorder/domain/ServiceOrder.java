package br.com.oficina.mvp.serviceorder.domain;

import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.shared.domain.BaseDomain;
import br.com.oficina.mvp.shared.domain.ServiceOrderStatus;
import br.com.oficina.mvp.vehicle.domain.Vehicle;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceOrder extends BaseDomain {
    private final String code;
    private Customer customer;
    private Vehicle vehicle;
    private ServiceOrderStatus status;
    private final String customerNotes;
    private String diagnosis;
    private BigDecimal totalServices = BigDecimal.ZERO;
    private BigDecimal totalParts = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private OffsetDateTime approvedAt;
    private OffsetDateTime startedAt;
    private OffsetDateTime finalizedAt;
    private OffsetDateTime deliveredAt;
    private final List<WorkOrderService> services = new ArrayList<>();
    private final List<WorkOrderPart> parts = new ArrayList<>();
    private final List<ServiceOrderStatusHistory> history = new ArrayList<>();

    public ServiceOrder(String code, Customer customer, Vehicle vehicle, String customerNotes) {
        this.code = code;
        this.customer = customer;
        this.vehicle = vehicle;
        this.customerNotes = customerNotes;
        this.status = ServiceOrderStatus.RECEBIDA;
        addHistory(ServiceOrderStatus.RECEBIDA, "OS recebida e registrada.");
    }

    public ServiceOrder(Long id, OffsetDateTime createdAt, OffsetDateTime updatedAt,
                         String code, Customer customer, Vehicle vehicle, ServiceOrderStatus status,
                         String customerNotes, String diagnosis,
                         BigDecimal totalServices, BigDecimal totalParts, BigDecimal totalAmount,
                         OffsetDateTime approvedAt, OffsetDateTime startedAt, OffsetDateTime finalizedAt, OffsetDateTime deliveredAt) {
        super(id, createdAt, updatedAt);
        this.code = code;
        this.customer = customer;
        this.vehicle = vehicle;
        this.status = status;
        this.customerNotes = customerNotes;
        this.diagnosis = diagnosis;
        this.totalServices = totalServices;
        this.totalParts = totalParts;
        this.totalAmount = totalAmount;
        this.approvedAt = approvedAt;
        this.startedAt = startedAt;
        this.finalizedAt = finalizedAt;
        this.deliveredAt = deliveredAt;
    }

    public void addService(WorkOrderService item) {
        item.attachTo(this);
        this.services.add(item);
        recalculateTotals();
    }

    public void addPart(WorkOrderPart item) {
        item.attachTo(this);
        this.parts.add(item);
        recalculateTotals();
    }

    public void markBudgetWaitingApproval() {
        this.status = ServiceOrderStatus.AGUARDANDO_APROVACAO;
        addHistory(ServiceOrderStatus.AGUARDANDO_APROVACAO, "Orçamento gerado automaticamente e enviado para aprovação.");
    }

    public void decideApproval(boolean approved, String comment) {
        if (approved) {
            ServiceOrderStatusPolicy.assertTransition(this.status, ServiceOrderStatus.EM_EXECUCAO);
            var now = OffsetDateTime.now();
            this.status = ServiceOrderStatus.EM_EXECUCAO;
            this.approvedAt = now;
            this.startedAt = now;
            addHistory(ServiceOrderStatus.EM_EXECUCAO,
                    comment == null || comment.isBlank() ? "Orçamento aprovado. Execução iniciada." : comment);
        } else {
            ServiceOrderStatusPolicy.assertTransition(this.status, ServiceOrderStatus.RECUSADA);
            this.status = ServiceOrderStatus.RECUSADA;
            addHistory(ServiceOrderStatus.RECUSADA,
                    comment == null || comment.isBlank() ? "Orçamento recusado." : comment);
        }
    }

    public void updateDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public void changeStatus(ServiceOrderStatus nextStatus, String comment) {
        ServiceOrderStatusPolicy.assertTransition(this.status, nextStatus);
        this.status = nextStatus;
        if (nextStatus == ServiceOrderStatus.EM_EXECUCAO && startedAt == null) startedAt = OffsetDateTime.now();
        if (nextStatus == ServiceOrderStatus.FINALIZADA) finalizedAt = OffsetDateTime.now();
        if (nextStatus == ServiceOrderStatus.ENTREGUE) deliveredAt = OffsetDateTime.now();
        addHistory(nextStatus, comment);
    }

    private void addHistory(ServiceOrderStatus status, String comment) {
        this.history.add(new ServiceOrderStatusHistory(this, status, comment));
    }

    private void recalculateTotals() {
        this.totalServices = services.stream().map(WorkOrderService::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalParts = parts.stream().map(WorkOrderPart::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = totalServices.add(totalParts);
    }

    public String getCode() { return code; }
    public Customer getCustomer() { return customer; }
    public Vehicle getVehicle() { return vehicle; }
    public ServiceOrderStatus getStatus() { return status; }
    public String getCustomerNotes() { return customerNotes; }
    public String getDiagnosis() { return diagnosis; }
    public BigDecimal getTotalServices() { return totalServices; }
    public BigDecimal getTotalParts() { return totalParts; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getFinalizedAt() { return finalizedAt; }
    public OffsetDateTime getDeliveredAt() { return deliveredAt; }
    public List<WorkOrderService> getServices() { return services; }
    public List<WorkOrderPart> getParts() { return parts; }
    public List<ServiceOrderStatusHistory> getHistory() { return history; }
}
