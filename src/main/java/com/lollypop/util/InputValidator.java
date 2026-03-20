package com.lollypop.util;

import java.util.regex.Pattern;

/**
 * Central input validation utility.
 * All user-facing input MUST pass through these methods before reaching the service/DAO layer.
 * PreparedStatements already prevent SQL injection; these validators also guard
 * against XSS-style characters and malformed data.
 */
public final class InputValidator {

    private static final Pattern NAME_PATTERN  = Pattern.compile("^[\\p{L}\\s'\\-]{1,100}$");
    private static final Pattern MSIN_PATTERN  = Pattern.compile("^\\d{10}$");
    private static final Pattern INT_PATTERN   = Pattern.compile("^\\d{1,9}$");

    private InputValidator() {}

    /**
     * Validates a subscriber name (first or last).
     * Only letters, spaces, hyphens and apostrophes; max 100 chars.
     */
    public static String validateName(String value, String label) {
        if (value == null || value.isBlank())
            throw new ValidationException(label + " must not be blank.");
        String trimmed = value.trim();
        if (!NAME_PATTERN.matcher(trimmed).matches())
            throw new ValidationException(label + " contains invalid characters. Use only letters, spaces, hyphens or apostrophes.");
        return trimmed;
    }

    /**
     * Validates an MSIN — must be exactly 10 digits.
     */
    public static long validateMsin(String value) {
        if (value == null || value.isBlank())
            throw new ValidationException("MSIN must not be blank.");
        String trimmed = value.trim();
        if (!MSIN_PATTERN.matcher(trimmed).matches())
            throw new ValidationException("MSIN must be exactly 10 digits (0–9 only).");
        return Long.parseLong(trimmed);
    }

    /**
     * Validates a positive integer field (e.g. subscriber ID, duration).
     */
    public static int validatePositiveInt(String value, String label) {
        if (value == null || value.isBlank())
            throw new ValidationException(label + " must not be blank.");
        String trimmed = value.trim();
        if (!INT_PATTERN.matcher(trimmed).matches())
            throw new ValidationException(label + " must be a positive whole number.");
        int n = Integer.parseInt(trimmed);
        if (n <= 0)
            throw new ValidationException(label + " must be greater than 0.");
        return n;
    }

    /**
     * Validates that an enum choice is non-null.
     */
    public static <T extends Enum<T>> T requireEnum(T value, String label) {
        if (value == null)
            throw new ValidationException(label + " must be selected.");
        return value;
    }

    /** Lightweight checked exception for validation errors. */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) { super(message); }
    }
}
