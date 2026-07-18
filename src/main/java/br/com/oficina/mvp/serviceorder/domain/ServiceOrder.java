package br.com.oficina.mvp.serviceorder.domain;

import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.shared.domain.ServiceOrderStatus;
import br.com.oficina.mvp.shared.domain.BaseEntity;
import br.com.oficina.mvp.vehicle.domain.Vehicle;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_orders")
public class ServiceOrder extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceOrderStatus status = ServiceOrderStatus.RECEBIDA;

    @Column(name = "customer_notes")
    private String customerNotes;

    private String diagnosis;

    @Column(name = "total_services", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalServices = BigDecimal.ZERO;

    @Column(name = "total_parts", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalParts = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "finalized_at")
    private OffsetDateTime finalizedAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkOrderService> services = new ArrayList<>();

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkOrderPart> parts = new ArrayList<>();

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceOrderStatusHistory> history = new ArrayList<>();

    protected ServiceOrder() {}

    public ServiceOrder(String code, Customer customer, Vehicle vehicle, String customerNotes) {
        this.code = code;
        this.customer = customer;
        this.vehicle = vehicle;
        this.customerNotes = customerNotes;
        this.status = ServiceOrderStatus.RECEBIDA;
        addHistory(ServiceOrderStatus.RECEBIDA, "OS recebida e registrada.");
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

    public void approve(String comment) {
        ServiceOrderStatusPolicy.assertTransition(this.status, ServiceOrderStatus.EM_EXECUCAO);
        var now = OffsetDateTime.now();
        this.status = ServiceOrderStatus.EM_EXECUCAO;
        this.approvedAt = now;
        this.startedAt = now;
        addHistory(ServiceOrderStatus.EM_EXECUCAO,
                comment == null || comment.isBlank() ? "Orçamento aprovado. Execução iniciada." : comment);
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
