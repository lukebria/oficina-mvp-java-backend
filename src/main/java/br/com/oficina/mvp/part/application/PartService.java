package br.com.oficina.mvp.part.application;

import br.com.oficina.mvp.part.application.port.in.PartCommand;
import br.com.oficina.mvp.part.application.port.in.PartUseCase;
import br.com.oficina.mvp.part.application.port.out.PartRepositoryPort;
import br.com.oficina.mvp.part.domain.Part;
import br.com.oficina.mvp.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PartService implements PartUseCase {
    private final PartRepositoryPort parts;

    public PartService(PartRepositoryPort parts) {
        this.parts = parts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Part> list() {
        return parts.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Part findById(Long id) {
        return findEntity(id);
    }

    @Override
    @Transactional
    public Part create(PartCommand command) {
        return parts.save(new Part(
                command.name(), command.sku(), command.unitPrice(), command.stockQuantity(), command.minStock(), command.active()
        ));
    }

    @Override
    @Transactional
    public Part update(Long id, PartCommand command) {
        var part = findEntity(id);
        part.update(command.name(), command.sku(), command.unitPrice(), command.stockQuantity(), command.minStock(), command.active());
        parts.save(part);
        return part;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        parts.delete(findEntity(id));
    }

    private Part findEntity(Long id) {
        return parts.findById(id)
                .orElseThrow(() -> new BusinessException("Peça/insumo não encontrado.", HttpStatus.NOT_FOUND));
    }
}
