package br.com.oficina.mvp.catalog.api.dto;

import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ServiceCatalogItemResponse(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        Integer estimatedMinutes,
        Boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ServiceCatalogItemResponse from(ServiceCatalogItem item) {
        return new ServiceCatalogItemResponse(
                item.getId(), item.getName(), item.getDescription(), item.getBasePrice(), item.getEstimatedMinutes(),
                item.getActive(), item.getCreatedAt(), item.getUpdatedAt()
        );
    }
}
