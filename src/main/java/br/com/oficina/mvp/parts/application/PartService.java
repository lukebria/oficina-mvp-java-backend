package br.com.oficina.mvp.parts.application;

import br.com.oficina.mvp.parts.api.dto.PartRequest;
import br.com.oficina.mvp.parts.api.dto.PartResponse;
import br.com.oficina.mvp.parts.domain.Part;
import br.com.oficina.mvp.parts.infra.PartRepository;
import br.com.oficina.mvp.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartService {
    private final PartRepository repository;

    public PartService(PartRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PartResponse> list() {
        return repository.findAll()
                .stream()
                .map(PartResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PartResponse findById(Long id) {
        return PartResponse.from(findEntity(id));
    }

    @Transactional
    public PartResponse create(PartRequest request) {
        return PartResponse.from(repository.save(new Part(
                request.name(), request.sku(), request.unitPrice(), request.stockQuantity(), request.minStock(), request.active()
        )));
    }

    @Transactional
    public PartResponse update(Long id, PartRequest request) {
        var part = findEntity(id);
        part.update(request.name(), request.sku(), request.unitPrice(), request.stockQuantity(), request.minStock(), request.active());
        return PartResponse.from(part);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findEntity(id));
    }

    public Part findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Peça/insumo não encontrado.", HttpStatus.NOT_FOUND));
    }
}
