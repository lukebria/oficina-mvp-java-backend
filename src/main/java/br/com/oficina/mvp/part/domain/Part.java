package br.com.oficina.mvp.part.domain;

import br.com.oficina.mvp.shared.domain.BaseDomain;
import br.com.oficina.mvp.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Part extends BaseDomain {
    private String name;
    private String sku;
    private BigDecimal unitPrice;
    private Integer stockQuantity;
    private Integer minStock;
    private Boolean active;

    public Part(String name, String sku, BigDecimal unitPrice, Integer stockQuantity, Integer minStock, Boolean active) {
        this.name = name;
        this.sku = normalizeSku(sku);
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity == null ? 0 : stockQuantity;
        this.minStock = minStock == null ? 0 : minStock;
        this.active = active == null || active;
    }

    public Part(Long id, OffsetDateTime createdAt, OffsetDateTime updatedAt,
                String name, String sku, BigDecimal unitPrice, Integer stockQuantity, Integer minStock, Boolean active) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.sku = sku;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.minStock = minStock;
        this.active = active;
    }

    public void update(String name, String sku, BigDecimal unitPrice, Integer stockQuantity, Integer minStock, Boolean active) {
        if (name != null) this.name = name;
        if (sku != null) this.sku = normalizeSku(sku);
        if (unitPrice != null) this.unitPrice = unitPrice;
        if (stockQuantity != null) this.stockQuantity = stockQuantity;
        if (minStock != null) this.minStock = minStock;
        if (active != null) this.active = active;
    }

    public void decrementStock(int quantity) {
        if (quantity > this.stockQuantity) {
            throw new BusinessException("Estoque insuficiente para a peça " + name + ".",
                    HttpStatus.UNPROCESSABLE_CONTENT);
        }
        this.stockQuantity -= quantity;
    }

    private String normalizeSku(String sku) { return sku == null ? null : sku.trim().toUpperCase(); }

    public String getName() { return name; }
    public String getSku() { return sku; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Integer getMinStock() { return minStock; }
    public Boolean getActive() { return active; }
}
