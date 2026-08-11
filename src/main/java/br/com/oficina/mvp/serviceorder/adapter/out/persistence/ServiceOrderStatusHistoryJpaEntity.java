package br.com.oficina.mvp.serviceorder.adapter.out.persistence;

import br.com.oficina.mvp.shared.domain.ServiceOrderStatus;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "service_order_status_history")
class ServiceOrderStatusHistoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrderJpaEntity serviceOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceOrderStatus status;

    private String comment;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    protected ServiceOrderStatusHistoryJpaEntity() {}

    ServiceOrderStatusHistoryJpaEntity(ServiceOrderJpaEntity serviceOrder, ServiceOrderStatus status, String comment, LocalDateTime changedAt) {
        this.serviceOrder = serviceOrder;
        this.status = status;
        this.comment = comment;
        this.changedAt = changedAt;
    }

    @PrePersist
    void prePersist() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }

    Long getId() { return id; }
    ServiceOrderStatus getStatus() { return status; }
    String getComment() { return comment; }
    LocalDateTime getChangedAt() { return changedAt; }
}
