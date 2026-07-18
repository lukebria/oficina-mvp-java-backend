package br.com.oficina.mvp.domains;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "work_order_services")
public class WorkOrderService extends WorkOrderLineItem {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_item_id", nullable = false)
    private ServiceCatalogItem serviceItem;

    protected WorkOrderService() {}

    public WorkOrderService(ServiceCatalogItem serviceItem, Integer quantity) {
        super(quantity == null ? 1 : quantity, serviceItem.getBasePrice());
        this.serviceItem = serviceItem;
    }

    public ServiceCatalogItem getServiceItem() { return serviceItem; }
}
