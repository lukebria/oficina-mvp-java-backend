package br.com.oficina.mvp.part.adapter.out.persistence;

import br.com.oficina.mvp.part.application.port.out.PartRepositoryPort;
import br.com.oficina.mvp.part.domain.Part;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class PartPersistenceAdapter implements PartRepositoryPort {
    private final PartJpaRepository jpaRepository;

    PartPersistenceAdapter(PartJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Part> findAll() {
        return jpaRepository.findAll().stream().map(PartMapper::toDomain).toList();
    }

    @Override
    public Optional<Part> findById(Long id) {
        return jpaRepository.findById(id).map(PartMapper::toDomain);
    }

    @Override
    public Part save(Part part) {
        PartJpaEntity entity;
        if (part.getId() == null) {
            entity = PartMapper.toNewEntity(part);
        } else {
            entity = jpaRepository.getReferenceById(part.getId());
            PartMapper.applyToEntity(part, entity);
        }
        return PartMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public void delete(Part part) {
        jpaRepository.deleteById(part.getId());
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
