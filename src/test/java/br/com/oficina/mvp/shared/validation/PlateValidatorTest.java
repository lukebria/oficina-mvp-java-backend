package br.com.oficina.mvp.shared.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlateValidatorTest {

    @Test
    void shouldValidateOldAndMercosulPlate() {
        assertThat(PlateValidator.isValidBrazilianPlate("ABC-1234")).isTrue();
        assertThat(PlateValidator.isValidBrazilianPlate("ABC1234")).isTrue();
        assertThat(PlateValidator.isValidBrazilianPlate("ABC1D23")).isTrue();
    }

    @Test
    void shouldNormalizePlate() {
        assertThat(PlateValidator.normalize("abc-1d23")).isEqualTo("ABC1D23");
        assertThat(PlateValidator.normalize(null)).isEmpty();
    }

    @Test
    void shouldRejectInvalidPlate() {
        assertThat(PlateValidator.isValidBrazilianPlate("AB123")).isFalse();
        assertThat(PlateValidator.isValidBrazilianPlate("")).isFalse();
        assertThat(PlateValidator.isValidBrazilianPlate("ABCD1234")).isFalse();
    }
}
