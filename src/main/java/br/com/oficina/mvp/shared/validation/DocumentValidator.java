package br.com.oficina.mvp.shared.validation;

import br.com.oficina.mvp.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class DocumentValidator {
    private DocumentValidator() {}

    public static String onlyDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    public static String normalize(String value) {
        return onlyDigits(value);
    }

    public static String requireValid(String rawDocument) {
        var document = normalize(rawDocument);
        if (!isValidCpfOrCnpj(document)) {
            throw new BusinessException("CPF/CNPJ inválido.", HttpStatus.BAD_REQUEST);
        }
        return document;
    }

    public static boolean isValidCpfOrCnpj(String document) {
        var digits = onlyDigits(document);
        if (digits.length() == 11) return isValidCpf(digits);
        if (digits.length() == 14) return isValidCnpj(digits);
        return false;
    }

    public static boolean isValidCpf(String cpf) {
        var digits = onlyDigits(cpf);
        if (digits.length() != 11 || digits.matches("(\\d)\\1{10}")) return false;

        int first = cpfDigit(digits.substring(0, 9), 10);
        int second = cpfDigit(digits.substring(0, 9) + first, 11);
        return first == Character.getNumericValue(digits.charAt(9))
                && second == Character.getNumericValue(digits.charAt(10));
    }

    private static int cpfDigit(String base, int weightStart) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            sum += Character.getNumericValue(base.charAt(i)) * (weightStart - i);
        }
        int check = (sum * 10) % 11;
        return check == 10 ? 0 : check;
    }

    public static boolean isValidCnpj(String cnpj) {
        var digits = onlyDigits(cnpj);
        if (digits.length() != 14 || digits.matches("(\\d)\\1{13}")) return false;

        int first = cnpjDigit(digits, new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        int second = cnpjDigit(digits, new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        return first == Character.getNumericValue(digits.charAt(12))
                && second == Character.getNumericValue(digits.charAt(13));
    }

    private static int cnpjDigit(String digits, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * weights[i];
        }
        int mod = sum % 11;
        return mod < 2 ? 0 : 11 - mod;
    }
}
