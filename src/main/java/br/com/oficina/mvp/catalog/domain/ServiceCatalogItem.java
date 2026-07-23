package br.com.oficina.mvp.catalog.domain;

import br.com.oficina.mvp.shared.domain.BaseDomain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ServiceCatalogItem extends BaseDomain {
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer estimatedMinutes;
    private Boolean active;

    public ServiceCatalogItem(String name, String description, BigDecimal basePrice, Integer estimatedMinutes, Boolean active) {
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.estimatedMinutes = estimatedMinutes;
        this.active = active == null || active;
    }

    public ServiceCatalogItem(Long id, OffsetDateTime createdAt, OffsetDateTime updatedAt,
                               String name, String description, BigDecimal basePrice, Integer estimatedMinutes, Boolean active) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.estimatedMinutes = estimatedMinutes;
        this.active = active;
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
