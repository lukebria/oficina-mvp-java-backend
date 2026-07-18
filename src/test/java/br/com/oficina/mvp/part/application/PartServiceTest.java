package br.com.oficina.mvp.part.application;

import br.com.oficina.mvp.part.application.port.in.PartCommand;
import br.com.oficina.mvp.part.application.port.out.PartRepositoryPort;
import br.com.oficina.mvp.part.domain.Part;
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
class PartServiceTest {

    @Mock
    PartRepositoryPort parts;

    @InjectMocks
    PartService service;

    @Test
    void shouldListParts() {
        when(parts.findAll()).thenReturn(List.of(
                new Part("Filtro", "flt-001", new BigDecimal("35.00"), 10, 2, true)
        ));

        assertThat(service.list()).hasSize(1);
        assertThat(service.list().getFirst().getSku()).isEqualTo("FLT-001");
    }

    @Test
    void shouldCreatePart() {
        var command = new PartCommand("Filtro", "flt-001", new BigDecimal("35.00"), 10, 2, true);
        when(parts.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(command);

        assertThat(result.getName()).isEqualTo("Filtro");
        assertThat(result.getSku()).isEqualTo("FLT-001");
    }

    @Test
    void shouldUpdatePart() {
        var part = new Part("Filtro", "FLT-001", new BigDecimal("35.00"), 10, 2, true);
        when(parts.findById(1L)).thenReturn(Optional.of(part));

        var command = new PartCommand("Filtro Premium", "FLT-002", new BigDecimal("45.00"), 5, 1, false);
        var result = service.update(1L, command);

        assertThat(result.getName()).isEqualTo("Filtro Premium");
        assertThat(result.getActive()).isFalse();
    }

    @Test
    void shouldFindById() {
        var part = new Part("Filtro", "FLT-001", new BigDecimal("35.00"), 10, 2, true);
        when(parts.findById(1L)).thenReturn(Optional.of(part));

        assertThat(service.findById(1L).getName()).isEqualTo("Filtro");
    }

    @Test
    void shouldThrowWhenNotFound() {
        when(parts.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Peça/insumo não encontrado");
    }

    @Test
    void shouldDeletePart() {
        var part = new Part("Filtro", "FLT-001", new BigDecimal("35.00"), 10, 2, true);
        when(parts.findById(1L)).thenReturn(Optional.of(part));

        service.delete(1L);

        verify(parts).delete(part);
    }
}
