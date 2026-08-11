package br.com.oficina.mvp.part.adapter.out.persistence;

import br.com.oficina.mvp.shared.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "parts")
public class PartJpaEntity extends BaseJpaEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "min_stock", nullable = false)
    private Integer minStock;

    @Column(nullable = false)
    private Boolean active = true;

    protected PartJpaEntity() {}

    PartJpaEntity(String name, String sku, BigDecimal unitPrice, Integer stockQuantity, Integer minStock, Boolean active) {
        this.name = name;
        this.sku = sku;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.minStock = minStock;
        this.active = active;
    }

    String getName() { return name; }
    String getSku() { return sku; }
    BigDecimal getUnitPrice() { return unitPrice; }
    Integer getStockQuantity() { return stockQuantity; }
    Integer getMinStock() { return minStock; }
    Boolean getActive() { return active; }

    void setName(String name) { this.name = name; }
    void setSku(String sku) { this.sku = sku; }
    void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    void setMinStock(Integer minStock) { this.minStock = minStock; }
    void setActive(Boolean active) { this.active = active; }
}
