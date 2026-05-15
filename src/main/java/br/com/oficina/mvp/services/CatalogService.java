package br.com.oficina.mvp.services;

import br.com.oficina.mvp.dtos.ServiceCatalogItemRequestDto;
import br.com.oficina.mvp.dtos.ServiceCatalogItemResponseDto;
import br.com.oficina.mvp.domains.ServiceCatalogItem;
import br.com.oficina.mvp.infra.ServiceCatalogItemRepository;
import br.com.oficina.mvp.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogService {
    private final ServiceCatalogItemRepository repository;

    public CatalogService(ServiceCatalogItemRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<ServiceCatalogItemResponseDto> list() {
        return repository.findAll()
                .stream()
                .map(ServiceCatalogItemResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceCatalogItemResponseDto findById(Long id) { return ServiceCatalogItemResponseDto.from(findEntity(id)); }

    @Transactional
    public ServiceCatalogItemResponseDto create(ServiceCatalogItemRequestDto request) {
        return ServiceCatalogItemResponseDto.from(repository.save(new ServiceCatalogItem(
                request.name(), request.description(), request.basePrice(), request.estimatedMinutes(), request.active()
        )));
    }

    @Transactional
    public ServiceCatalogItemResponseDto update(Long id, ServiceCatalogItemRequestDto request) {
        var item = findEntity(id);
        item.update(request.name(), request.description(), request.basePrice(), request.estimatedMinutes(), request.active());
        return ServiceCatalogItemResponseDto.from(item);
    }

    @Transactional
    public void delete(Long id) { repository.delete(findEntity(id)); }

    public ServiceCatalogItem findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado.", HttpStatus.NOT_FOUND));
    }
}
