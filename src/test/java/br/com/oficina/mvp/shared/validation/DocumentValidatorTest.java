package br.com.oficina.mvp.shared.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentValidatorTest {
    @Test
    void shouldValidateCpf() {
        assertThat(DocumentValidator.isValidCpfOrCnpj("529.982.247-25")).isTrue();
        assertThat(DocumentValidator.normalize("529.982.247-25")).isEqualTo("52998224725");
    }

    @Test
    void shouldRejectInvalidCpf() {
        assertThat(DocumentValidator.isValidCpfOrCnpj("111.111.111-11")).isFalse();
        assertThat(DocumentValidator.isValidCpfOrCnpj("123")).isFalse();
    }

    @Test
    void shouldValidateCnpj() {
        assertThat(DocumentValidator.isValidCpfOrCnpj("04.252.011/0001-10")).isTrue();
    }

    @Test
    void shouldRejectInvalidCnpj() {
        assertThat(DocumentValidator.isValidCpfOrCnpj("00.000.000/0000-00")).isFalse();
    }
}
