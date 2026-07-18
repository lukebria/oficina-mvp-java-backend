package br.com.oficina.mvp.serviceorder.domain;

import br.com.oficina.mvp.part.domain.Part;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "work_order_parts")
public class WorkOrderPart extends WorkOrderLineItem {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    protected WorkOrderPart() {}

    public WorkOrderPart(Part part, Integer quantity) {
        super(quantity, part.getUnitPrice());
        this.part = part;
    }

    public Part getPart() { return part; }
}
