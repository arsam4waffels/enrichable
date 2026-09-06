package com.enrichable.annotation;

import com.enrichable.config.ErrorLevel;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Marks a custom exception class with a predefined error code and level.
 *
 * <p>Apply this annotation to classes that extend {@link com.enrichable.EnrichableException}
 * to define their error code and severity once, at the class level, instead of
 * repeating them at every throw site.</p>
 *
 * <h5>Usage:</h5>
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
 * <h5>Annotation targets:</h5>
 * <ul>
 *   <li>{@link ElementType#TYPE} — applies to classes only</li>
 *   <li>{@link RetentionPolicy#RUNTIME} — readable via reflection at runtime</li>
 * </ul>
 *
 * @see EnrichableHandler
 * @see AnnotationProcessor#processCode(Class, String, String)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnrichableCode {
    /**
     * <b>The error code for this exception class</b>
     *
     * <p>A short identifier that describes the specific error
     * (e.g. {@code "DB-001"}, {@code "AUTH-403"}).</p>
     *
     * @return the error code
     */
    String code();
    /**
     * <b>The severity level for this exception class</b>
     *
     * <p>Defaults to {@link ErrorLevel#ERROR} if not specified.</p>
     *
     * @return the error level
     */
    ErrorLevel level() default ErrorLevel.ERROR;
}