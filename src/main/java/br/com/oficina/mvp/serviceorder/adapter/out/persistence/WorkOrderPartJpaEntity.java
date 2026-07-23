package br.com.oficina.mvp.serviceorder.adapter.out.persistence;

import br.com.oficina.mvp.part.adapter.out.persistence.PartJpaEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "work_order_parts")
class WorkOrderPartJpaEntity extends WorkOrderLineItemJpaEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id", nullable = false)
    private PartJpaEntity part;

    protected WorkOrderPartJpaEntity() {}

    WorkOrderPartJpaEntity(ServiceOrderJpaEntity serviceOrder, PartJpaEntity part, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        super(serviceOrder, quantity, unitPrice, totalPrice);
        this.part = part;
    }

    PartJpaEntity getPart() { return part; }
}
