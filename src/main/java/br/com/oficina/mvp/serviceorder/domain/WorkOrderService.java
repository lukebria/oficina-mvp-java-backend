package br.com.oficina.mvp.serviceorder.domain;

import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;

import java.math.BigDecimal;

public class WorkOrderService extends WorkOrderLineItem {
    private final ServiceCatalogItem serviceItem;

    public WorkOrderService(ServiceCatalogItem serviceItem, Integer quantity) {
        super(quantity == null ? 1 : quantity, serviceItem.getBasePrice());
        this.serviceItem = serviceItem;
    }

    public WorkOrderService(Long id, ServiceCatalogItem serviceItem, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        super(id, quantity, unitPrice, totalPrice);
        this.serviceItem = serviceItem;
    }

    public ServiceCatalogItem getServiceItem() { return serviceItem; }
}
