package com.enrichable.annotation;

import com.enrichable.EnrichableException;
/**
 * Reads {@link EnrichableHandler} and {@link EnrichableCode} annotations
 * via reflection and constructs the corresponding {@link EnrichableException}.
 *
 * <p>Instead of repeating context, codes, and levels at every throw site,
 * you define them once on the class itself and let the processor pick them up
 * automatically. This keeps exception creation consistent and reduces
 * boilerplate across the codebase.</p>
 *
 * <h5>Usage with {@link EnrichableHandler}:</h5>
 * <pre>{@code
 * @EnrichableHandler(context = "Database", defaultLevel = ErrorLevel.CRITICAL)
 * public class DatabaseService {
 *
 *     public void connect() {
 *         throw AnnotationProcessor.processHandler(
 *                 DatabaseService.class,
 *                 "DB-001",
 *                 "Connection failed"
 *         );
 *     }
 * }
 * }</pre>
 *
 * <h5>Usage with {@link EnrichableCode}:</h5>
 * <pre>{@code
 * @EnrichableCode(code = "DB-001", level = ErrorLevel.CRITICAL)
 * public class DatabaseConnectionException extends EnrichableException { ... }
 *
 * throw AnnotationProcessor.processCode(
 *         DatabaseConnectionException.class,
 *         "Database",
 *         "Connection failed"
 * );
 * }</pre>
 *
 * <p>This class cannot be instantiated.</p>
 *
 * @see EnrichableHandler
 * @see EnrichableCode
 */
public final class AnnotationProcessor {
    private AnnotationProcessor() {}
    /**
     * <h5>Build an exception from a {@link EnrichableHandler} annotated class</h5>
     *
     * <p>Reads {@code context} and {@code defaultLevel} from the annotation
     * on {@code clazz} and combines them with the provided {@code code} and
     * {@code message} to construct an {@link EnrichableException}.</p>
     *
     * @param clazz   the service class annotated with {@link EnrichableHandler}
     * @param code    a short identifier for the error (e.g. {@code "DB-001"})
     * @param message a human-readable description of the error
     * @return a new {@link EnrichableException} populated from the annotation
     * @throws IllegalArgumentException if {@code clazz} is not annotated
     *                                  with {@link EnrichableHandler}
     */
    public static EnrichableException processHandler(Class<?> clazz,
                                                     String code,
                                                     String message) {
        if (!clazz.isAnnotationPresent(EnrichableHandler.class))
            throw new IllegalArgumentException(
                    clazz.getSimpleName() + " is not annotated with @EnrichableHandler"
            );
        EnrichableHandler annotation = clazz.getAnnotation(EnrichableHandler.class);
        return new EnrichableException(
                annotation.context(),
                code,
                message,
                annotation.defaultLevel(),
                null
        );
    }
    /**
     * <h5>Build an exception from an {@link EnrichableCode} annotated class</h5>
     *
     * <p>Reads {@code code} and {@code level} from the annotation on {@code clazz}
     * and combines them with the provided {@code context} and {@code message}
     * to construct an {@link EnrichableException}.</p>
     *
     * @param clazz   the exception class annotated with {@link EnrichableCode};
     *                must extend {@link EnrichableException}
     * @param context the source or component where the error occurred
     * @param message a human-readable description of the error
     * @return a new {@link EnrichableException} populated from the annotation
     * @throws IllegalArgumentException if {@code clazz} is not annotated
     *                                  with {@link EnrichableCode}
     */
    public static EnrichableException processCode(Class<? extends EnrichableException> clazz,
                                                  String context,
                                                  String message) {
        if (!clazz.isAnnotationPresent(EnrichableCode.class))
            throw new IllegalArgumentException(
                    clazz.getSimpleName() + " is not annotated with @EnrichableCode"
            );
        EnrichableCode annotation = clazz.getAnnotation(EnrichableCode.class);
        return new EnrichableException(
                context,
                annotation.code(),
                message,
                annotation.level(),
                null
        );
    }
}