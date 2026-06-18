package br.com.oficina.mvp.services;

import br.com.oficina.mvp.domains.ServiceCatalogItem;
import br.com.oficina.mvp.dtos.ServiceCatalogItemRequestDto;
import br.com.oficina.mvp.infra.ServiceCatalogItemRepository;
import br.com.oficina.mvp.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    ServiceCatalogItemRepository repository;

    @InjectMocks
    CatalogService service;

    @Test
    void shouldListCatalogItems() {
        when(repository.findAll()).thenReturn(List.of(
                new ServiceCatalogItem("Troca de óleo", "Completa", new BigDecimal("150.00"), 60, true)
        ));

        assertThat(service.list()).hasSize(1);
    }

    @Test
    void shouldCreateCatalogItem() {
        var request = new ServiceCatalogItemRequestDto("Troca de óleo", "Completa", new BigDecimal("150.00"), 60, true);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(request);

        assertThat(result.name()).isEqualTo("Troca de óleo");
    }

    @Test
    void shouldUpdateCatalogItem() {
        var item = new ServiceCatalogItem("Troca de óleo", "Completa", new BigDecimal("150.00"), 60, true);
        when(repository.findById(1L)).thenReturn(Optional.of(item));

        var request = new ServiceCatalogItemRequestDto("Alinhamento", "Completo", new BigDecimal("80.00"), 45, false);
        var result = service.update(1L, request);

        assertThat(result.name()).isEqualTo("Alinhamento");
        assertThat(result.active()).isFalse();
    }

    @Test
    void shouldFindById() {
        var item = new ServiceCatalogItem("Troca de óleo", "Completa", new BigDecimal("150.00"), 60, true);
        when(repository.findById(1L)).thenReturn(Optional.of(item));

        assertThat(service.findById(1L).name()).isEqualTo("Troca de óleo");
    }

    @Test
    void shouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Serviço não encontrado");
    }

    @Test
    void shouldDeleteCatalogItem() {
        var item = new ServiceCatalogItem("Troca de óleo", null, new BigDecimal("150.00"), 60, true);
        when(repository.findById(1L)).thenReturn(Optional.of(item));

        service.delete(1L);

        verify(repository).delete(item);
    }
}
