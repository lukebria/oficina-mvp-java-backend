package br.com.oficina.mvp.serviceorders.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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
    private LocalDateTime changedAt;


    protected ServiceOrderStatusHistory() {
    }

    public ServiceOrderStatusHistory(ServiceOrder serviceOrder, ServiceOrderStatus status, String comment) {
        this.serviceOrder = serviceOrder;
        this.status = status;
        this.comment = comment;
    }

    @PrePersist
    public void prePersist() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public ServiceOrderStatus getStatus() {
        return status;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
