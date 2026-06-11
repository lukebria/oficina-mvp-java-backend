package br.com.oficina.mvp.shared.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentValidatorTest {

    @Test
    void shouldReturnEmptyWhenNull() {
        assertThat(DocumentValidator.onlyDigits(null)).isEmpty();
        assertThat(DocumentValidator.normalize(null)).isEmpty();
    }

    @Test
    void shouldValidateCpf() {
        assertThat(DocumentValidator.isValidCpfOrCnpj("529.982.247-25")).isTrue();
        assertThat(DocumentValidator.isValidCpf("529.982.247-25")).isTrue();
        assertThat(DocumentValidator.normalize("529.982.247-25")).isEqualTo("52998224725");
    }

    @Test
    void shouldRejectInvalidCpf() {
        assertThat(DocumentValidator.isValidCpfOrCnpj("111.111.111-11")).isFalse();
        assertThat(DocumentValidator.isValidCpfOrCnpj("123")).isFalse();
        assertThat(DocumentValidator.isValidCpf("111.111.111-11")).isFalse();
        assertThat(DocumentValidator.isValidCpf("1234567890")).isFalse();
        assertThat(DocumentValidator.isValidCpf("529.982.247-26")).isFalse();
        assertThat(DocumentValidator.isValidCpf("529.982.247-15")).isFalse();
    }

    @Test
    void shouldValidateCnpj() {
        assertThat(DocumentValidator.isValidCpfOrCnpj("04.252.011/0001-10")).isTrue();
        assertThat(DocumentValidator.isValidCnpj("04.252.011/0001-10")).isTrue();
    }

    @Test
    void shouldRejectInvalidCnpj() {
        assertThat(DocumentValidator.isValidCpfOrCnpj("00.000.000/0000-00")).isFalse();
        assertThat(DocumentValidator.isValidCnpj("00.000.000/0000-00")).isFalse();
        assertThat(DocumentValidator.isValidCnpj("04.252.011/0001-11")).isFalse();
        assertThat(DocumentValidator.isValidCnpj("1234567890123")).isFalse();
    }

    @Test
    void shouldRejectInvalidLength() {
        assertThat(DocumentValidator.isValidCpfOrCnpj("123456789012")).isFalse();
        assertThat(DocumentValidator.isValidCpfOrCnpj("12345678901234")).isFalse();
    }
}
