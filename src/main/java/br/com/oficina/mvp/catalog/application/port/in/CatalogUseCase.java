package br.com.oficina.mvp.catalog.application.port.in;

import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;

import java.util.List;

public interface CatalogUseCase {
    List<ServiceCatalogItem> list();

    ServiceCatalogItem findById(Long id);

    ServiceCatalogItem create(CatalogCommand command);

    ServiceCatalogItem update(Long id, CatalogCommand command);

    void delete(Long id);
}
