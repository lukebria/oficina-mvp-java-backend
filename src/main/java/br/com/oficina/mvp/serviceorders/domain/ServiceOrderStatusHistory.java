package br.com.oficina.mvp.serviceorders.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "service_order_status_history")
public class ServiceOrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrder serviceOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceOrderStatus status;

    private String comment;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    protected ServiceOrderStatusHistory() {}

    public ServiceOrderStatusHistory(ServiceOrder serviceOrder, ServiceOrderStatus status, String comment) {
        this.serviceOrder = serviceOrder;
        this.status = status;
        this.comment = comment;
    }

    @PrePersist
    void prePersist() { this.changedAt = OffsetDateTime.now(); }

    public Long getId() { return id; }
    public ServiceOrderStatus getStatus() { return status; }
    public String getComment() { return comment; }
    public OffsetDateTime getChangedAt() { return changedAt; }
}
