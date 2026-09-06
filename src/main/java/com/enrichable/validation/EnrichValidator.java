package com.enrichable.validation;

import com.enrichable.config.ErrorLevel;

/**
 * <h5>Utility class for validating and normalizing input values across the library.</h5>
 *
 * <p>All methods are static and fail fast — they throw {@link IllegalArgumentException}
 * immediately when a constraint is violated, so invalid data never silently
 * makes its way into an exception entry.</p>
 *
 * <p>This class cannot be instantiated.</p>
 */
public final class EnrichValidator {
    private EnrichValidator() {}
    /**
     * <h5>Validates that a string field is neither {@code null} nor blank.</h5>
     *
     * <pre>{@code
     * EnrichValidator.requireNonBlank(context, "context");
     * }</pre>
     *
     * @param value     the string to validate
     * @param fieldName the name of the field, used in the exception message
     * @throws IllegalArgumentException if {@code value} is {@code null} or blank
     */
    public static void requireNonBlank(String value, String fieldName) {
        if (value == null)
            throw new IllegalArgumentException(fieldName + " cannot be null.");
        if (value.isBlank())
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
    }
    /**
     * <h5>Validates that an {@link ErrorLevel} is not {@code null}.</h5>
     *
     * <pre>{@code
     * EnrichValidator.requireNonNull(errorLevel);
     * }</pre>
     *
     * @param errorLevel the error level to validate
     * @throws IllegalArgumentException if {@code errorLevel} is {@code null}
     */
    public static void requireNonNull(ErrorLevel errorLevel) {
        if (errorLevel == null)
            throw new IllegalArgumentException("Error level cannot be null.");
    }
    /**
     * <h5>Validates that an object is not {@code null}.</h5>
     *
     * <pre>{@code
     * EnrichValidator.requireNonNull(consoleConfig, "Console configuration");
     * }</pre>
     *
     * @param obj       the object to validate
     * @param fieldName the name of the field, used in the exception message
     * @throws IllegalArgumentException if {@code obj} is {@code null}
     */
    public static void requireNonNull(Object obj, String fieldName) {
        if (obj == null)
            throw new IllegalArgumentException(fieldName + " cannot be null.");
    }
    /**
     * <h5>Normalizes a metadata key.</h5>
     *
     * <p>If the key is blank, it is replaced with {@code "BLANK"} rather than
     * rejected outright — this keeps metadata entries visible in output so
     * the problem is obvious rather than silently dropped.</p>
     *
     * <pre>{@code
     * EnrichValidator.normalizeMetadataKey("");    // → "BLANK"
     * EnrichValidator.normalizeMetadataKey("userId"); // → "userId"
     * }</pre>
     *
     * @param key the metadata key to normalize
     * @return the original key, or {@code "BLANK"} if it was blank
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public static String normalizeMetadataKey(String key) {
        if (key == null) throw new IllegalArgumentException("Metadata key cannot be null.");
        return key.isBlank() ? "BLANK" : key;
    }
    /**
     * <h5>Normalizes a metadata value.</h5>
     *
     * <p>If the value is blank, it is replaced with {@code "BLANK"} rather than
     * rejected outright — this keeps metadata entries visible in output so
     * the problem is obvious rather than silently dropped.</p>
     *
     * <pre>{@code
     * EnrichValidator.normalizeMetadataValue("");       // → "BLANK"
     * EnrichValidator.normalizeMetadataValue("active"); // → "active"
     * }</pre>
     *
     * @param value the metadata value to normalize
     * @return the original value, or {@code "BLANK"} if it was blank
     * @throws IllegalArgumentException if {@code value} is {@code null}
     */
    public static String normalizeMetadataValue(String value) {
        if (value == null) throw new IllegalArgumentException("Metadata value cannot be null.");
        return value.isBlank() ? "BLANK" : value;
    }
}