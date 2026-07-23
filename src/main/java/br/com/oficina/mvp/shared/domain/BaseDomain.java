package br.com.oficina.mvp.shared.domain;

import java.time.OffsetDateTime;

public abstract class BaseDomain {
    private Long id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    protected BaseDomain() {}

    protected BaseDomain(Long id, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
