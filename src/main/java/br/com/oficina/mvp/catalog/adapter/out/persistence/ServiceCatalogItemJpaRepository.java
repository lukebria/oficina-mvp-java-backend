package br.com.oficina.mvp.catalog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ServiceCatalogItemJpaRepository extends JpaRepository<ServiceCatalogItemJpaEntity, Long> {}
