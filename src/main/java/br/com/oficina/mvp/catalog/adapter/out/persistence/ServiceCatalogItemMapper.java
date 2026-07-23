package br.com.oficina.mvp.catalog.adapter.out.persistence;

import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;

public final class ServiceCatalogItemMapper {
    private ServiceCatalogItemMapper() {}

    public static ServiceCatalogItem toDomain(ServiceCatalogItemJpaEntity entity) {
        return new ServiceCatalogItem(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getName(),
                entity.getDescription(),
                entity.getBasePrice(),
                entity.getEstimatedMinutes(),
                entity.getActive()
        );
    }

    static ServiceCatalogItemJpaEntity toNewEntity(ServiceCatalogItem domain) {
        return new ServiceCatalogItemJpaEntity(
                domain.getName(), domain.getDescription(), domain.getBasePrice(), domain.getEstimatedMinutes(), domain.getActive()
        );
    }

    static void applyToEntity(ServiceCatalogItem domain, ServiceCatalogItemJpaEntity entity) {
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setBasePrice(domain.getBasePrice());
        entity.setEstimatedMinutes(domain.getEstimatedMinutes());
        entity.setActive(domain.getActive());
    }
}
