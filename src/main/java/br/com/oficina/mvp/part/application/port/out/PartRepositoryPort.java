package br.com.oficina.mvp.part.application.port.out;

import br.com.oficina.mvp.part.domain.Part;

import java.util.List;
import java.util.Optional;

public interface PartRepositoryPort {
    List<Part> findAll();

    Optional<Part> findById(Long id);

    Part save(Part part);

    void delete(Part part);

    long count();
}
