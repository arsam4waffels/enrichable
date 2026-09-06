package com.enrichable.model;

import com.enrichable.config.ErrorLevel;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Represents a single enriched error entry within an {@link com.enrichable.EnrichableException}.
 *
 * <p>Each entry captures the full context of one specific error — where it occurred,
 * what went wrong, how severe it is, when it happened, and any extra metadata
 * attached to it. An {@code EnrichableException} can hold multiple entries,
 * making it possible to report several related errors in a single exception.</p>
 *
 * <h5>Fields captured per entry:</h5>
 * <ul>
 *   <li>{@code context} — the source or component where the error occurred</li>
 *   <li>{@code code} — an optional short identifier for the error</li>
 *   <li>{@code message} — a human-readable description of the error</li>
 *   <li>{@code errorLevel} — the severity of the error</li>
 *   <li>{@code dateTime} — the exact moment this entry was created</li>
 *   <li>{@code metadata} — optional key-value pairs for extra context</li>
 * </ul>
 *
 * <h5>Thread safety:</h5>
 * <p>Metadata is stored in a {@link ConcurrentHashMap}, so concurrent calls
 * to {@link #addMetadata} are safe. All other fields are final and set
 * at construction time.</p>
 *
 * @see com.enrichable.EnrichableException
 * @see ErrorLevel
 */
public class EnrichInformation {
    private final String context;
    private final String code;
    private final String message;
    private final LocalDateTime dateTime;
    private final ErrorLevel errorLevel;
    private final Map<String, String> metadata = new ConcurrentHashMap<>();
    /**
     * <h5>Constructs a new {@code EnrichInformation} entry.</h5>
     *
     * <p>The {@code dateTime} is captured automatically at construction time,
     * so it reflects the exact moment this entry was created — not when
     * the parent exception was thrown.</p>
     *
     * @param context    the source or component where the error occurred
     *                   (e.g. {@code "DATABASE"}, {@code "AUTH_SERVICE"})
     * @param code       an optional short identifier for the error
     *                   (e.g. {@code "DB-001"}); may be {@code null}
     * @param message    a human-readable description of the error
     * @param errorLevel the severity level of this entry
     */
    public EnrichInformation(String context,
                                String code,
                                String message,
                                ErrorLevel errorLevel) {
        this.context = context;
        this.code = code;
        this.message = message;
        this.errorLevel = errorLevel;
        this.dateTime = LocalDateTime.now();
    }
    /**
     * <h5>Attaches a key-value pair to this error entry.</h5>
     *
     * <p>Metadata is useful for carrying extra context that doesn't fit
     * neatly into the standard fields — for example, a user ID, a query
     * string, or a retry count.</p>
     *
     * <p>Keys and values should be pre-normalized before being passed here.
     * Use {@link com.enrichable.validation.EnrichValidator#normalizeMetadataKey}
     * and {@link com.enrichable.validation.EnrichValidator#normalizeMetadataValue}
     * to handle blank inputs.</p>
     *
     * <p>This method is thread-safe.</p>
     *
     * @param key   the metadata key
     * @param value the metadata value
     */
    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }
    /**
     * <b>Returns the source or component where the error occurred.</b>
     *
     * @return the context string (e.g. {@code "DATABASE"})
     */
    public String getContext() {
        return context;
    }
    /**
     * <b>Returns the optional short identifier for this error.</b>
     *
     * @return the error code, or {@code null} if none was provided
     */
    public String getCode() {
        return code;
    }
    /**
     * <b>Returns the human-readable description of this error.</b>
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }
    /**
     * <b>Returns the timestamp of when this entry was created.</b>
     *
     * <p>The timestamp is captured at construction time and reflects the
     * moment this specific entry was added, not when the parent exception
     * was thrown.</p>
     *
     * @return the creation timestamp
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    /**
     * <b>Returns the severity level of this error entry.</b>
     *
     * @return the {@link ErrorLevel}
     */
    public ErrorLevel getErrorLevel() {
        return errorLevel;
    }
    /**
     * <b>Returns an unmodifiable view of the metadata attached to this entry.</b>
     *
     * <p>The returned map reflects the current state of the metadata at the
     * time of the call. To add new entries, use {@link #addMetadata}.</p>
     *
     * @return an unmodifiable map of metadata key-value pairs;
     *         empty if no metadata has been added
     */
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
}