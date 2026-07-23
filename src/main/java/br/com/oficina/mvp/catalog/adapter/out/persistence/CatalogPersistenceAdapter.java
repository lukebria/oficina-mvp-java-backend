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
        return jpaRepository.findAll().stream().map(ServiceCatalogItemMapper::toDomain).toList();
    }

    @Override
    public Optional<ServiceCatalogItem> findById(Long id) {
        return jpaRepository.findById(id).map(ServiceCatalogItemMapper::toDomain);
    }

    @Override
    public ServiceCatalogItem save(ServiceCatalogItem item) {
        ServiceCatalogItemJpaEntity entity;
        if (item.getId() == null) {
            entity = ServiceCatalogItemMapper.toNewEntity(item);
        } else {
            entity = jpaRepository.getReferenceById(item.getId());
            ServiceCatalogItemMapper.applyToEntity(item, entity);
        }
        return ServiceCatalogItemMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public void delete(ServiceCatalogItem item) {
        jpaRepository.deleteById(item.getId());
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
