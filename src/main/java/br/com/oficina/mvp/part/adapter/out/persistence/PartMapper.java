package br.com.oficina.mvp.part.adapter.out.persistence;

import br.com.oficina.mvp.part.domain.Part;

public final class PartMapper {
    private PartMapper() {}

    public static Part toDomain(PartJpaEntity entity) {
        return new Part(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getName(),
                entity.getSku(),
                entity.getUnitPrice(),
                entity.getStockQuantity(),
                entity.getMinStock(),
                entity.getActive()
        );
    }

    static PartJpaEntity toNewEntity(Part domain) {
        return new PartJpaEntity(
                domain.getName(), domain.getSku(), domain.getUnitPrice(), domain.getStockQuantity(), domain.getMinStock(), domain.getActive()
        );
    }

    static void applyToEntity(Part domain, PartJpaEntity entity) {
        entity.setName(domain.getName());
        entity.setSku(domain.getSku());
        entity.setUnitPrice(domain.getUnitPrice());
        entity.setStockQuantity(domain.getStockQuantity());
        entity.setMinStock(domain.getMinStock());
        entity.setActive(domain.getActive());
    }
}
