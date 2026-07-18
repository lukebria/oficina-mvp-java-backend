package br.com.oficina.mvp.catalog.adapter.out.persistence;

import br.com.oficina.mvp.catalog.application.port.out.CatalogRepositoryPort;
import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class CatalogPersistenceAdapter implements CatalogRepositoryPort {
    private final ServiceCatalogItemJpaRepository jpaRepository;

    CatalogPersistenceAdapter(ServiceCatalogItemJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ServiceCatalogItem> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<ServiceCatalogItem> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public ServiceCatalogItem save(ServiceCatalogItem item) {
        return jpaRepository.save(item);
    }

    @Override
    public void delete(ServiceCatalogItem item) {
        jpaRepository.delete(item);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
