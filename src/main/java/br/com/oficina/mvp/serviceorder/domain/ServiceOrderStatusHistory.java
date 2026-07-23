package br.com.oficina.mvp.serviceorder.domain;

import br.com.oficina.mvp.shared.domain.ServiceOrderStatus;

import java.time.LocalDateTime;

public class ServiceOrderStatusHistory {
    private Long id;
    private final ServiceOrder serviceOrder;
    private final ServiceOrderStatus status;
    private final String comment;
    private final LocalDateTime changedAt;

    public ServiceOrderStatusHistory(ServiceOrder serviceOrder, ServiceOrderStatus status, String comment) {
        this.serviceOrder = serviceOrder;
        this.status = status;
        this.comment = comment;
        this.changedAt = LocalDateTime.now();
    }

    public ServiceOrderStatusHistory(Long id, ServiceOrder serviceOrder, ServiceOrderStatus status, String comment, LocalDateTime changedAt) {
        this.id = id;
        this.serviceOrder = serviceOrder;
        this.status = status;
        this.comment = comment;
        this.changedAt = changedAt;
    }

    public Long getId() { return id; }
    public ServiceOrderStatus getStatus() { return status; }
    public String getComment() { return comment; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
