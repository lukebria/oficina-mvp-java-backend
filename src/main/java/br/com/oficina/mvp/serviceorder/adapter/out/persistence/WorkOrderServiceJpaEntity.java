package br.com.oficina.mvp.serviceorder.adapter.out.persistence;

import br.com.oficina.mvp.catalog.adapter.out.persistence.ServiceCatalogItemJpaEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "work_order_services")
class WorkOrderServiceJpaEntity extends WorkOrderLineItemJpaEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_item_id", nullable = false)
    private ServiceCatalogItemJpaEntity serviceItem;

    protected WorkOrderServiceJpaEntity() {}

    WorkOrderServiceJpaEntity(ServiceOrderJpaEntity serviceOrder, ServiceCatalogItemJpaEntity serviceItem, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        super(serviceOrder, quantity, unitPrice, totalPrice);
        this.serviceItem = serviceItem;
    }

    ServiceCatalogItemJpaEntity getServiceItem() { return serviceItem; }
}
