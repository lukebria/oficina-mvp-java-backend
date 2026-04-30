package br.com.oficina.mvp.serviceorders.domain;

import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "work_order_services")
public class WorkOrderService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrder serviceOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_item_id", nullable = false)
    private ServiceCatalogItem serviceItem;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    protected WorkOrderService() {}

    public WorkOrderService(ServiceCatalogItem serviceItem, Integer quantity) {
        this.serviceItem = serviceItem;
        this.quantity = quantity == null ? 1 : quantity;
        this.unitPrice = serviceItem.getBasePrice();
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    void attachTo(ServiceOrder serviceOrder) { this.serviceOrder = serviceOrder; }

    public Long getId() { return id; }
    public ServiceCatalogItem getServiceItem() { return serviceItem; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
}
