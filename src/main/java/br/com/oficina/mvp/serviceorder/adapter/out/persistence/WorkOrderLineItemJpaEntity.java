package br.com.oficina.mvp.serviceorder.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.math.BigDecimal;

@MappedSuperclass
abstract class WorkOrderLineItemJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrderJpaEntity serviceOrder;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    protected WorkOrderLineItemJpaEntity() {}

    protected WorkOrderLineItemJpaEntity(ServiceOrderJpaEntity serviceOrder, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        this.serviceOrder = serviceOrder;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    Long getId() { return id; }
    ServiceOrderJpaEntity getServiceOrder() { return serviceOrder; }
    Integer getQuantity() { return quantity; }
    BigDecimal getUnitPrice() { return unitPrice; }
    BigDecimal getTotalPrice() { return totalPrice; }
}
