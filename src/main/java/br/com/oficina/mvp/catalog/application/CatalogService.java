package br.com.oficina.mvp.catalog.application;

import br.com.oficina.mvp.catalog.application.port.in.CatalogCommand;
import br.com.oficina.mvp.catalog.application.port.in.CatalogUseCase;
import br.com.oficina.mvp.catalog.application.port.out.CatalogRepositoryPort;
import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;
import br.com.oficina.mvp.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService implements CatalogUseCase {
    private final CatalogRepositoryPort catalog;

    public CatalogService(CatalogRepositoryPort catalog) {
        this.catalog = catalog;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceCatalogItem> list() {
        return catalog.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceCatalogItem findById(Long id) {
        return findEntity(id);
    }

    @Override
    @Transactional
    public ServiceCatalogItem create(CatalogCommand command) {
        return catalog.save(new ServiceCatalogItem(
                command.name(), command.description(), command.basePrice(), command.estimatedMinutes(), command.active()
        ));
    }

    @Override
    @Transactional
    public ServiceCatalogItem update(Long id, CatalogCommand command) {
        var item = findEntity(id);
        item.update(command.name(), command.description(), command.basePrice(), command.estimatedMinutes(), command.active());
        return item;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        catalog.delete(findEntity(id));
    }

    private ServiceCatalogItem findEntity(Long id) {
        return catalog.findById(id)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado.", HttpStatus.NOT_FOUND));
    }
}
