package br.com.oficina.mvp.catalog.adapter.out.persistence;

import br.com.oficina.mvp.shared.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "service_catalog_items")
public class ServiceCatalogItemJpaEntity extends BaseJpaEntity {
    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "estimated_minutes", nullable = false)
    private Integer estimatedMinutes;

    @Column(nullable = false)
    private Boolean active = true;

    protected ServiceCatalogItemJpaEntity() {}

    ServiceCatalogItemJpaEntity(String name, String description, BigDecimal basePrice, Integer estimatedMinutes, Boolean active) {
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.estimatedMinutes = estimatedMinutes;
        this.active = active;
    }

    String getName() { return name; }
    String getDescription() { return description; }
    BigDecimal getBasePrice() { return basePrice; }
    Integer getEstimatedMinutes() { return estimatedMinutes; }
    Boolean getActive() { return active; }

    void setName(String name) { this.name = name; }
    void setDescription(String description) { this.description = description; }
    void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    void setActive(Boolean active) { this.active = active; }
}
