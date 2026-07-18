package br.com.oficina.mvp.catalog.adapter.out.persistence;

import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;

interface ServiceCatalogItemJpaRepository extends JpaRepository<ServiceCatalogItem, Long> {}
