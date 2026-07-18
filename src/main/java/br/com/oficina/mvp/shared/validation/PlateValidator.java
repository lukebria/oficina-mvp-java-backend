package br.com.oficina.mvp.shared.validation;

import br.com.oficina.mvp.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class PlateValidator {
    private PlateValidator() {}

    public static String normalize(String plate) {
        return plate == null ? "" : plate.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }

    public static String requireValid(String rawPlate) {
        var plate = normalize(rawPlate);
        if (!isValidBrazilianPlate(plate)) {
            throw new BusinessException("Placa de veículo inválida.", HttpStatus.BAD_REQUEST);
        }
        return plate;
    }

    public static boolean isValidBrazilianPlate(String plate) {
        var normalized = normalize(plate);
        return normalized.matches("^[A-Z]{3}[0-9]{4}$")
                || normalized.matches("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");
    }
}
