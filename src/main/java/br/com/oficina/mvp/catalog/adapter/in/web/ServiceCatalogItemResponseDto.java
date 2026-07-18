package br.com.oficina.mvp.catalog.adapter.in.web;

import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ServiceCatalogItemResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        Integer estimatedMinutes,
        Boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ServiceCatalogItemResponseDto from(ServiceCatalogItem item) {
        return new ServiceCatalogItemResponseDto(
                item.getId(), item.getName(), item.getDescription(), item.getBasePrice(), item.getEstimatedMinutes(),
                item.getActive(), item.getCreatedAt(), item.getUpdatedAt()
        );
    }
}
