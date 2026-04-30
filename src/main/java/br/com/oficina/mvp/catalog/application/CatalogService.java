package br.com.oficina.mvp.catalog.application;

import br.com.oficina.mvp.catalog.api.dto.ServiceCatalogItemRequest;
import br.com.oficina.mvp.catalog.api.dto.ServiceCatalogItemResponse;
import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;
import br.com.oficina.mvp.catalog.infra.ServiceCatalogItemRepository;
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
    public List<ServiceCatalogItemResponse> list() {
        return repository.findAll()
                .stream()
                .map(ServiceCatalogItemResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceCatalogItemResponse findById(Long id) { return ServiceCatalogItemResponse.from(findEntity(id)); }

    @Transactional
    public ServiceCatalogItemResponse create(ServiceCatalogItemRequest request) {
        return ServiceCatalogItemResponse.from(repository.save(new ServiceCatalogItem(
                request.name(), request.description(), request.basePrice(), request.estimatedMinutes(), request.active()
        )));
    }

    @Transactional
    public ServiceCatalogItemResponse update(Long id, ServiceCatalogItemRequest request) {
        var item = findEntity(id);
        item.update(request.name(), request.description(), request.basePrice(), request.estimatedMinutes(), request.active());
        return ServiceCatalogItemResponse.from(item);
    }

    @Transactional
    public void delete(Long id) { repository.delete(findEntity(id)); }

    public ServiceCatalogItem findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado.", HttpStatus.NOT_FOUND));
    }
}
