package br.com.oficina.mvp.serviceorder.domain;

import java.math.BigDecimal;

public abstract class WorkOrderLineItem {
    private Long id;
    private ServiceOrder serviceOrder;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    protected WorkOrderLineItem(Integer quantity, BigDecimal unitPrice) {
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    protected WorkOrderLineItem(Long id, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        this.id = id;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    void attachTo(ServiceOrder serviceOrder) { this.serviceOrder = serviceOrder; }

    public Long getId() { return id; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
}
