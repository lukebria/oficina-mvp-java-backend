package br.com.oficina.mvp.catalog.application.port.out;

import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;

import java.util.List;
import java.util.Optional;

public interface CatalogRepositoryPort {
    List<ServiceCatalogItem> findAll();

    Optional<ServiceCatalogItem> findById(Long id);

    ServiceCatalogItem save(ServiceCatalogItem item);

    void delete(ServiceCatalogItem item);

    long count();
}
