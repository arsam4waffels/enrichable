package com.enrichable.annotation;

import com.enrichable.config.ErrorLevel;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Marks a service class with a predefined error context and default level.
 *
 * <p>Apply this annotation to service or handler classes to define their
 * error context and default severity once, at the class level, instead of
 * repeating them at every throw site.</p>
 *
 * <h5>Usage:</h5>
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
 * <h5>Annotation targets:</h5>
 * <ul>
 *   <li>{@link ElementType#TYPE} — applies to classes only</li>
 *   <li>{@link RetentionPolicy#RUNTIME} — readable via reflection at runtime</li>
 * </ul>
 *
 * @see EnrichableCode
 * @see AnnotationProcessor#processHandler(Class, String, String)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnrichableHandler {
    /**
     * <b>The error context for this service class</b>
     *
     * <p>Identifies the source or component where errors originate
     * (e.g. {@code "Database"}, {@code "AuthService"}).</p>
     *
     * @return the error context
     */
    String context();
    /**
     * <b>The default severity level for errors thrown from this class</b>
     *
     * <p>Defaults to {@link ErrorLevel#ERROR} if not specified.</p>
     *
     * @return the default error level
     */
    ErrorLevel defaultLevel() default ErrorLevel.ERROR;
}