package br.com.oficina.mvp.catalog.infra;

import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceCatalogItemRepository extends JpaRepository<ServiceCatalogItem, Long> {}
