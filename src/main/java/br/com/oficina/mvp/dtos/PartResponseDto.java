package br.com.oficina.mvp.dtos;

import br.com.oficina.mvp.domains.Part;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PartResponseDto(
        Long id,
        String name,
        String sku,
        BigDecimal unitPrice,
        Integer stockQuantity,
        Integer minStock,
        Boolean active,
        Boolean belowMinStock,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static PartResponseDto from(Part part) {
        return new PartResponseDto(
                part.getId(), part.getName(), part.getSku(), part.getUnitPrice(), part.getStockQuantity(), part.getMinStock(),
                part.getActive(), part.getStockQuantity() <= part.getMinStock(), part.getCreatedAt(), part.getUpdatedAt()
        );
    }
}
