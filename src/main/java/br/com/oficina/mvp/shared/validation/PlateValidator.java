package br.com.oficina.mvp.shared.validation;

public final class PlateValidator {
    private PlateValidator() {}

    public static String normalize(String plate) {
        return plate == null ? "" : plate.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }

    public static boolean isValidBrazilianPlate(String plate) {
        var normalized = normalize(plate);
        return normalized.matches("^[A-Z]{3}[0-9]{4}$")
                || normalized.matches("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");
    }
}
