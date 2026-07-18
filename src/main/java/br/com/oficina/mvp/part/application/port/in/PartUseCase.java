package br.com.oficina.mvp.part.application.port.in;

import br.com.oficina.mvp.part.domain.Part;

import java.util.List;

public interface PartUseCase {
    List<Part> list();

    Part findById(Long id);

    Part create(PartCommand command);

    Part update(Long id, PartCommand command);

    void delete(Long id);
}
