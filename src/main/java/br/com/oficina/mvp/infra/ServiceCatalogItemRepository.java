package br.com.oficina.mvp.infra;

import br.com.oficina.mvp.domains.ServiceCatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceCatalogItemRepository extends JpaRepository<ServiceCatalogItem, Long> {}
