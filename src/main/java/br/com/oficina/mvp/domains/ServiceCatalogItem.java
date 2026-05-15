package br.com.oficina.mvp.domains;

import br.com.oficina.mvp.domains.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "service_catalog_items")
public class ServiceCatalogItem extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "estimated_minutes", nullable = false)
    private Integer estimatedMinutes;

    @Column(nullable = false)
    private Boolean active = true;

    protected ServiceCatalogItem() {}

    public ServiceCatalogItem(String name, String description, BigDecimal basePrice, Integer estimatedMinutes, Boolean active) {
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.estimatedMinutes = estimatedMinutes;
        this.active = active == null || active;
    }

    public void update(String name, String description, BigDecimal basePrice, Integer estimatedMinutes, Boolean active) {
        if (name != null) this.name = name;
        this.description = description;
        if (basePrice != null) this.basePrice = basePrice;
        if (estimatedMinutes != null) this.estimatedMinutes = estimatedMinutes;
        if (active != null) this.active = active;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getBasePrice() { return basePrice; }
    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public Boolean getActive() { return active; }
}
