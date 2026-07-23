package br.com.oficina.mvp.serviceorder.adapter.out.persistence;

import br.com.oficina.mvp.customer.adapter.out.persistence.CustomerJpaEntity;
import br.com.oficina.mvp.shared.domain.ServiceOrderStatus;
import br.com.oficina.mvp.shared.persistence.BaseJpaEntity;
import br.com.oficina.mvp.vehicle.adapter.out.persistence.VehicleJpaEntity;
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
class ServiceOrderJpaEntity extends BaseJpaEntity {
    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerJpaEntity customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleJpaEntity vehicle;

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
    private List<WorkOrderServiceJpaEntity> services = new ArrayList<>();

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkOrderPartJpaEntity> parts = new ArrayList<>();

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceOrderStatusHistoryJpaEntity> history = new ArrayList<>();

    protected ServiceOrderJpaEntity() {}

    ServiceOrderJpaEntity(String code, CustomerJpaEntity customer, VehicleJpaEntity vehicle, String customerNotes) {
        this.code = code;
        this.customer = customer;
        this.vehicle = vehicle;
        this.customerNotes = customerNotes;
    }

    String getCode() { return code; }
    CustomerJpaEntity getCustomer() { return customer; }
    VehicleJpaEntity getVehicle() { return vehicle; }
    ServiceOrderStatus getStatus() { return status; }
    String getCustomerNotes() { return customerNotes; }
    String getDiagnosis() { return diagnosis; }
    BigDecimal getTotalServices() { return totalServices; }
    BigDecimal getTotalParts() { return totalParts; }
    BigDecimal getTotalAmount() { return totalAmount; }
    OffsetDateTime getApprovedAt() { return approvedAt; }
    OffsetDateTime getStartedAt() { return startedAt; }
    OffsetDateTime getFinalizedAt() { return finalizedAt; }
    OffsetDateTime getDeliveredAt() { return deliveredAt; }
    List<WorkOrderServiceJpaEntity> getServices() { return services; }
    List<WorkOrderPartJpaEntity> getParts() { return parts; }
    List<ServiceOrderStatusHistoryJpaEntity> getHistory() { return history; }

    void setCustomer(CustomerJpaEntity customer) { this.customer = customer; }
    void setVehicle(VehicleJpaEntity vehicle) { this.vehicle = vehicle; }
    void setStatus(ServiceOrderStatus status) { this.status = status; }
    void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    void setTotalServices(BigDecimal totalServices) { this.totalServices = totalServices; }
    void setTotalParts(BigDecimal totalParts) { this.totalParts = totalParts; }
    void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }
    void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    void setFinalizedAt(OffsetDateTime finalizedAt) { this.finalizedAt = finalizedAt; }
    void setDeliveredAt(OffsetDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
}
