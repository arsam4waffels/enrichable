package com.enrichable;

import com.enrichable.config.ErrorLevel;
import com.enrichable.config.LogConfig;
import com.enrichable.config.ConsoleConfig;
import com.enrichable.formatter.DefaultEnrichFormatter;
import com.enrichable.logging.FileEnrichLogger;
import com.enrichable.model.EnrichInformation;
import com.enrichable.validation.EnrichValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
/**
 * A {@link RuntimeException} that carries structured, enriched error information.
 *
 * <p>Unlike a standard exception that holds only a message and a stack trace,
 * an {@code EnrichableException} can hold multiple {@link EnrichInformation}
 * entries — each with its own context, code, message, error level, timestamp,
 * and metadata. This makes it easier to understand what went wrong, where,
 * and why, without digging through a wall of stack frames.</p>
 *
 * <h5>Creating an exception:</h5>
 * <p>Use the {@link Builder} API — it is the recommended way to construct
 * an {@code EnrichableException}:</p>
 *
 * <pre>{@code
 * EnrichableException exception =
 *         new EnrichableException.Builder("DATABASE", "Connection failed")
 *                 .code("DB-001")
 *                 .level(ErrorLevel.CRITICAL)
 *                 .cause(new IllegalStateException("Connection refused."))
 *                 .build();
 * }</pre>
 *
 * <h5>Adding more information:</h5>
 * <pre>{@code
 * exception.addInformation("CACHE", "CACHE-001", "Cache unavailable", ErrorLevel.WARNING);
 * }</pre>
 *
 * <h5>Logging:</h5>
 * <pre>{@code
 * exception.setLogConfig(
 *         new LogConfig()
 *                 .generateCode(true)
 *                 .filePath("application.log")
 * );
 *
 * String code = exception.writeLog(); // af45cb
 * }</pre>
 *
 * <h5>Thread safety:</h5>
 * <p>{@link #addInformation} and {@link #addMetadata} are synchronized.
 * {@link #consoleConfig} and {@link #logConfig} are declared {@code volatile}
 * so visibility across threads is guaranteed. File logging is synchronized
 * separately inside {@link FileEnrichLogger}.</p>
 *
 * @see Builder
 * @see EnrichInformation
 * @see LogConfig
 * @see ConsoleConfig
 */
public class EnrichableException extends RuntimeException {
    private volatile ConsoleConfig consoleConfig = new ConsoleConfig();
    private volatile LogConfig logConfig = new LogConfig();
    private final String thrownAt = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    private final List<EnrichInformation> informationList = new CopyOnWriteArrayList<>();
    /**
     * @deprecated Use {@link Builder} instead. This constructor will be removed
     *             in a future version.
     */
    @Deprecated
    public EnrichableException(String context,
                               String code,
                               String message,
                               ErrorLevel level,
                               Throwable cause) {
        super(message, cause);
        addInformation(context, code, message, level);
    }
    /**
     * Constructs an {@code EnrichableException} from a {@link Builder}.
     *
     * <p>This constructor is not meant to be called directly.
     * Use {@link Builder#build()} instead.</p>
     *
     * @param builder the builder containing the initial error information
     */
    public EnrichableException(Builder builder) {
        super(builder.message, builder.throwable);
        addInformation(
                builder.context,
                builder.code,
                builder.message,
                builder.errorLevel
        );
    }
    /**
     * Builder for constructing {@link EnrichableException} instances.
     *
     * <p>{@code context} and {@code message} are required.
     * All other fields are optional and fall back to safe defaults.</p>
     *
     * <h5>Usage:</h5>
     * <pre>{@code
     * EnrichableException exception =
     *         new EnrichableException.Builder("DATABASE", "Connection failed")
     *                 .code("DB-001")
     *                 .level(ErrorLevel.CRITICAL)
     *                 .cause(new IllegalStateException("Connection refused."))
     *                 .build();
     * }</pre>
     *
     * <h5>Defaults:</h5>
     * <ul>
     *   <li>{@code level} — {@link ErrorLevel#ERROR}</li>
     *   <li>{@code code} — {@code null}</li>
     *   <li>{@code cause} — {@code null}</li>
     * </ul>
     */
    public static class Builder {
        private final String context;
        private String code;
        private final String message;
        private ErrorLevel errorLevel = ErrorLevel.ERROR;
        private Throwable throwable;
        /**
         * Creates a new Builder with the required fields.
         *
         * @param context the source or component where the error occurred
         *                (e.g. {@code "DATABASE"}, {@code "AUTH_SERVICE"})
         * @param message a human-readable description of the error
         * @throws IllegalArgumentException if {@code context} or {@code message}
         *                                  is null or blank
         */
        public Builder(String context, String message) {
            EnrichValidator.requireNonBlank(context, "context");
            EnrichValidator.requireNonBlank(message, "message");
            this.context = context;
            this.message = message;
        }
        /**
         * Sets an optional error code for this exception.
         *
         * @param code a short identifier for the error (e.g. {@code "DB-001"})
         * @return this Builder
         * @throws IllegalArgumentException if {@code code} is blank
         */
        public Builder code(String code) {
            EnrichValidator.requireNonBlank(code, "code");
            this.code = code;
            return this;
        }
        /**
         * Sets the error level for this exception.
         *
         * <p>Defaults to {@link ErrorLevel#ERROR} if not specified.</p>
         *
         * @param errorLevel the severity level
         * @return this Builder
         * @throws IllegalArgumentException if {@code errorLevel} is null
         */
        public Builder level(ErrorLevel errorLevel) {
            EnrichValidator.requireNonNull(errorLevel);
            this.errorLevel = errorLevel;
            return this;
        }
        /**
         * Sets the underlying cause of this exception.
         *
         * @param throwable the original exception that triggered this error
         * @return this Builder
         * @throws IllegalArgumentException if {@code throwable} is null
         */
        public Builder cause(Throwable throwable) {
            EnrichValidator.requireNonNull(throwable, "cause");
            this.throwable = throwable;
            return this;
        }
        /**
         * Builds and returns the {@link EnrichableException}.
         *
         * @return a new {@code EnrichableException} with the configured fields
         */
        public EnrichableException build() {
            return new EnrichableException(this);
        }
    }
    /**
     * Sets the console output configuration for this exception.
     *
     * <p>Controls what is shown when the exception is printed to the console,
     * such as timestamps, error levels, error count, and metadata.</p>
     *
     * @param consoleConfig the console configuration to apply
     * @return this exception, for method chaining
     * @throws IllegalArgumentException if {@code consoleConfig} is null
     * @see ConsoleConfig
     */
    public EnrichableException setConsoleConfig(ConsoleConfig consoleConfig) {
        EnrichValidator.requireNonNull(consoleConfig, "Console configuration");
        this.consoleConfig = consoleConfig;
        return this;
    }
    /**
     * Sets the file logging configuration for this exception.
     *
     * <p>Controls log file path, filtering, formatting, and whether a unique
     * error code is generated for this session.</p>
     *
     * @param logConfig the log configuration to apply
     * @return this exception, for method chaining
     * @throws IllegalArgumentException if {@code logConfig} is null
     * @see LogConfig
     */
    public EnrichableException setLogConfig(LogConfig logConfig) {
        EnrichValidator.requireNonNull(logConfig, "Log configuration");
        this.logConfig = logConfig;
        return this;
    }
    /**
     * Adds a new error entry to this exception.
     *
     * <p>Each entry carries its own context, code, message, error level,
     * timestamp, and metadata. Entries are appended in the order they are added
     * and appear in that order in both console output and log reports.</p>
     *
     * <p>This method is thread-safe.</p>
     *
     * @param context the source or component where the error occurred
     * @param code    an optional short identifier for the error; may be
     *                {@code null}, but must not be blank if provided
     * @param message a human-readable description of the error
     * @param level   the severity level of this error entry
     * @return this exception, for method chaining
     * @throws IllegalArgumentException if {@code context} or {@code message}
     *                                  is null or blank, if {@code level} is
     *                                  null, or if {@code code} is blank
     */
    public synchronized EnrichableException addInformation(String context,
                                              String code,
                                              String message,
                                              ErrorLevel level) {
        EnrichValidator.requireNonBlank(context, "Exception context");
        EnrichValidator.requireNonBlank(message, "Exception message");
        EnrichValidator.requireNonNull(level);
        if (code != null) EnrichValidator.requireNonBlank(code, "Exception code");
        informationList.add(new EnrichInformation(context, code, message, level));
        return this;
    }
    /**
     * @deprecated Use {@link EnrichInformation#addMetadata(String, String)} directly
     *             through {@link #getInformationList()} instead. This method will be
     *             removed in a future version.
     */
    @Deprecated
    public synchronized EnrichableException addMetadata(String key, String value) {
        if (informationList.isEmpty())
            throw new IllegalStateException("Cannot add metadata without exception information.");
        informationList.getLast().addMetadata(
                EnrichValidator.normalizeMetadataKey(key),
                EnrichValidator.normalizeMetadataValue(value)
        );
        return this;
    }
    /**
     * @deprecated Use {@link #setLogConfig(LogConfig)} with
     *             {@link LogConfig#onlyLevel(ErrorLevel)} instead.
     *             This method will be removed in a future version.
     */
    @Deprecated
    public EnrichableException onlyLog(ErrorLevel level) {
        EnrichValidator.requireNonNull(level);
        this.logConfig.onlyLevel(level);
        return this;
    }
    /**
     * Writes the exception report to the configured log file.
     *
     * <p>The report includes all error entries that pass the configured
     * level filter, formatted according to the active {@link LogConfig}.</p>
     *
     * <p>If {@link LogConfig#generateCode(boolean) generateCode(true)} is set,
     * a unique 6-character hex code is generated for this session, stored in
     * the registry file, and returned. Otherwise, {@code null} is returned.</p>
     *
     * <h5>Example:</h5>
     * <pre>{@code
     * String code = exception.writeLog();
     * System.out.println("Error code: " + code); // af45cb
     * }</pre>
     *
     * @return the generated error code if {@code generateCode} is enabled,
     *         {@code null} otherwise
     * @see LogConfig#generateCode(boolean)
     * @see LogConfig#registryPath(String)
     */
    public String writeLog() {
        return FileEnrichLogger.getInstance().write(
                informationList,
                thrownAt,
                logConfig);
    }
    /**
     * Returns a formatted string representation of this exception.
     *
     * <p>The output is controlled by the active {@link ConsoleConfig} and
     * includes all error entries with their context, code, message, level,
     * timestamps, and metadata based on the current configuration.</p>
     *
     * <p>This method is thread-safe.</p>
     *
     * @return a formatted, human-readable representation of this exception
     */
    @Override
    public synchronized String toString() {
        return new DefaultEnrichFormatter(consoleConfig).format(informationList);
    }
    /**
     * Returns an unmodifiable view of the error information list.
     *
     * <p>The returned list reflects the current state of the exception at the
     * time of the call. It cannot be modified directly — use
     * {@link #addInformation} to add new entries.</p>
     *
     * @return an unmodifiable list of {@link EnrichInformation} entries
     */
    public List<EnrichInformation> getInformationList() {
        return Collections.unmodifiableList(informationList);
    }
}
