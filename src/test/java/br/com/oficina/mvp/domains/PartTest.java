package br.com.oficina.mvp.domains;

import br.com.oficina.mvp.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartTest {

    @Test
    void shouldNormalizeSkuAndApplyDefaults() {
        var part = new Part("Filtro", " flt-001 ", new BigDecimal("35.00"), null, null, null);

        assertThat(part.getSku()).isEqualTo("FLT-001");
        assertThat(part.getStockQuantity()).isZero();
        assertThat(part.getMinStock()).isZero();
        assertThat(part.getActive()).isTrue();
    }

    @Test
    void shouldUpdateFields() {
        var part = new Part("Filtro", "FLT-001", new BigDecimal("35.00"), 10, 2, true);

        part.update("Filtro Premium", "FLT-002", new BigDecimal("45.00"), 5, 1, false);

        assertThat(part.getName()).isEqualTo("Filtro Premium");
        assertThat(part.getSku()).isEqualTo("FLT-002");
        assertThat(part.getActive()).isFalse();
    }

    @Test
    void shouldDecrementStock() {
        var part = new Part("Filtro", "FLT-001", new BigDecimal("35.00"), 10, 2, true);

        part.decrementStock(3);

        assertThat(part.getStockQuantity()).isEqualTo(7);
    }

    @Test
    void shouldRejectDecrementWhenStockIsInsufficient() {
        var part = new Part("Filtro", "FLT-001", new BigDecimal("35.00"), 2, 0, true);

        assertThatThrownBy(() -> part.decrementStock(3))
                .isInstanceOf(BusinessException.class);

        assertThat(part.getStockQuantity()).isEqualTo(2);
    }
}
