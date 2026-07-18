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
        return jpaRepository.findAll();
    }

    @Override
    public Optional<Part> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Part save(Part part) {
        return jpaRepository.save(part);
    }

    @Override
    public void delete(Part part) {
        jpaRepository.delete(part);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
