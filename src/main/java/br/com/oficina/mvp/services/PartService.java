package br.com.oficina.mvp.services;

import br.com.oficina.mvp.dtos.PartRequestDto;
import br.com.oficina.mvp.dtos.PartResponseDto;
import br.com.oficina.mvp.domains.Part;
import br.com.oficina.mvp.infra.PartRepository;
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
    public List<PartResponseDto> list() {
        return repository.findAll()
                .stream()
                .map(PartResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PartResponseDto findById(Long id) {
        return PartResponseDto.from(findEntity(id));
    }

    @Transactional
    public PartResponseDto create(PartRequestDto request) {
        return PartResponseDto.from(repository.save(new Part(
                request.name(), request.sku(), request.unitPrice(), request.stockQuantity(), request.minStock(), request.active()
        )));
    }

    @Transactional
    public PartResponseDto update(Long id, PartRequestDto request) {
        var part = findEntity(id);
        part.update(request.name(), request.sku(), request.unitPrice(), request.stockQuantity(), request.minStock(), request.active());
        return PartResponseDto.from(part);
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
