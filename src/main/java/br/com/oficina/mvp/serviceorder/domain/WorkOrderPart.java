package br.com.oficina.mvp.serviceorder.domain;

import br.com.oficina.mvp.part.domain.Part;

import java.math.BigDecimal;

public class WorkOrderPart extends WorkOrderLineItem {
    private final Part part;

    public WorkOrderPart(Part part, Integer quantity) {
        super(quantity, part.getUnitPrice());
        this.part = part;
    }

    public WorkOrderPart(Long id, Part part, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        super(id, quantity, unitPrice, totalPrice);
        this.part = part;
    }

    public Part getPart() { return part; }
}
