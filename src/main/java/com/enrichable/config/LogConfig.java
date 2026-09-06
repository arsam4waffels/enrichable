package com.enrichable.config;

import com.enrichable.validation.EnrichValidator;
/**
 * Configuration for file logging behavior in {@link com.enrichable.EnrichableException}.
 *
 * <p>Controls which errors are written to the log file, what fields appear
 * in the report, where the file is written, and whether a unique error code
 * is generated for the session. All options have safe defaults so the logger
 * works out of the box without any configuration.</p>
 *
 * <h5>Usage:</h5>
 * <pre>{@code
 * LogConfig config = new LogConfig()
 *         .minimumLevel(ErrorLevel.ERROR)
 *         .showMetadata(true)
 *         .filePath("application.log")
 *         .generateCode(true)
 *         .registryPath("logs/registry.log");
 *
 * exception.setLogConfig(config);
 * String code = exception.writeLog(); // af45cb
 * }</pre>
 *
 * <h5>Defaults:</h5>
 * <ul>
 *   <li>{@code showTimestamp} -> {@code true}</li>
 *   <li>{@code showErrorLevel} -> {@code true}</li>
 *   <li>{@code showMetadata} -> {@code true}</li>
 *   <li>{@code filePath} -> {@code "enrichable.log"}</li>
 *   <li>{@code clearBeforeWrite} -> {@code false}</li>
 *   <li>{@code generateCode} -> {@code false}</li>
 *   <li>{@code registryPath} -> {@code "enrichable-registry.log"}</li>
 * </ul>
 *
 * @see com.enrichable.EnrichableException#setLogConfig(LogConfig)
 * @see com.enrichable.logging.FileEnrichLogger
 * @see com.enrichable.registry.ErrorRegistry
 */
public class LogConfig {

    private ErrorLevel onlyLevel;
    private ErrorLevel minimumLevel;
    private boolean showTimestamp = true;
    private boolean showErrorLevel = true;
    private boolean showMetadata = true;
    private String filePath = "enrichable.log";
    private boolean clearBeforeWrite = false;
    private boolean generateCode = false;
    private String registryPath = "enrichable-registry.log";
    /**
     * <h5>Filter log output to one specific error level only</h5>
     *
     * <p>Only entries matching {@code level} exactly are written to the log.
     * Calling this clears any active {@link #minimumLevel(ErrorLevel)} filter —
     * the two are mutually exclusive.</p>
     *
     * @param level the exact error level to include
     * @return this config, for method chaining
     * @throws IllegalArgumentException if {@code level} is {@code null}
     */
    public LogConfig onlyLevel(ErrorLevel level) {
        EnrichValidator.requireNonNull(level);
        this.onlyLevel = level;
        this.minimumLevel = null;
        return this;
    }
    /**
     * <h5>Filter log output to a minimum error level and above</h5>
     *
     * <p>Only entries at {@code level} or more severe are written to the log.
     * Calling this clears any active {@link #onlyLevel(ErrorLevel)} filter —
     * the two are mutually exclusive.</p>
     *
     * <p>Level ordering from least to most severe:</p>
     * <pre>{@code
     * INFO < WARNING < ERROR < CRITICAL
     * }</pre>
     *
     * @param level the minimum error level to include
     * @return this config, for method chaining
     * @throws IllegalArgumentException if {@code level} is {@code null}
     */
    public LogConfig minimumLevel(ErrorLevel level) {
        EnrichValidator.requireNonNull(level);
        this.minimumLevel = level;
        this.onlyLevel = null;
        return this;
    }
    /**
     * <b>Set whether timestamps appear in the log report</b>
     *
     * @param showTimestamp {@code true} to include timestamps, {@code false} to hide them
     * @return this config, for method chaining
     */
    public LogConfig showTimestamp(boolean showTimestamp) {
        this.showTimestamp = showTimestamp;
        return this;
    }
    /**
     * <b>Return whether timestamps appear in the log report</b>
     *
     * @return {@code true} if timestamps are shown; {@code false} otherwise
     */
    public boolean showTimestamp() {
        return this.showTimestamp;
    }
    /**
     * <b>Set whether error levels appear in the log report</b>
     *
     * @param showErrorLevel {@code true} to include error levels, {@code false} to hide them
     * @return this config, for method chaining
     */
    public LogConfig showErrorLevel(boolean showErrorLevel) {
        this.showErrorLevel = showErrorLevel;
        return this;
    }
    /**
     * <b>Return whether error levels appear in the log report</b>
     *
     * @return {@code true} if error levels are shown; {@code false} otherwise
     */
    public boolean showErrorLevel() {
        return this.showErrorLevel;
    }
    /**
     * <b>Set whether metadata appears in the log report</b>
     *
     * @param showMetadata {@code true} to include metadata, {@code false} to hide it
     * @return this config, for method chaining
     */
    public LogConfig showMetadata(boolean showMetadata) {
        this.showMetadata = showMetadata;
        return this;
    }
    /**
     * <b>Return whether metadata appears in the log report</b>
     *
     * @return {@code true} if metadata is shown; {@code false} otherwise
     */
    public boolean showMetadata() {
        return this.showMetadata;
    }
    /**
     * <b>Set the log file path</b>
     *
     * <p>Defaults to {@code "enrichable.log"} in the working directory.
     * Parent directories are created automatically if they do not exist.</p>
     *
     * @param filePath the path to the log file
     * @return this config, for method chaining
     * @throws IllegalArgumentException if {@code filePath} is null or blank
     */
    public LogConfig filePath(String filePath) {
        EnrichValidator.requireNonBlank(filePath, "Log file path");
        this.filePath = filePath;
        return this;
    }
    /**
     * <b>Return the log file path</b>
     *
     * @return the configured log file path
     */
    public String filePath() {
        return this.filePath;
    }
    /**
     * <b>Set whether the log file is cleared before each write</b>
     *
     * <p>When {@code true}, the existing file content is replaced on every
     * {@code writeLog()} call. When {@code false} (default), new reports
     * are appended.</p>
     *
     * @param clearBeforeWrite {@code true} to clear the file before writing
     * @return this config, for method chaining
     */
    public LogConfig clearBeforeWrite(boolean clearBeforeWrite) {
        this.clearBeforeWrite = clearBeforeWrite;
        return this;
    }
    /**
     * <b>Return whether the log file is cleared before each write</b>
     *
     * @return {@code true} if the file is cleared before writing; {@code false} otherwise
     */
    public boolean clearBeforeWrite() {
        return this.clearBeforeWrite;
    }
    /**
     * <b>Return the active onlyLevel filter</b>
     *
     * @return the configured level, or {@code null} if not set
     */
    public ErrorLevel onlyLevel() {
        return this.onlyLevel;
    }
    /**
     * <b>Return the active minimumLevel filter</b>
     *
     * @return the configured level, or {@code null} if not set
     */
    public ErrorLevel minimumLevel() {
        return this.minimumLevel;
    }
    /**
     * <h5>Enable or disable unique error code generation</h5>
     *
     * <p>When enabled, each call to {@code writeLog()} generates a unique
     * 6-character hex code for that exception session, stores the full report
     * in the registry file, and returns the code to the caller.</p>
     *
     * <pre>{@code
     * String code = exception.writeLog(); // af45cb
     * }</pre>
     *
     * @param generateCode {@code true} to enable code generation
     * @return this config, for method chaining
     * @see com.enrichable.registry.ErrorRegistry
     */
    public LogConfig generateCode(boolean generateCode) {
        this.generateCode = generateCode;
        return this;
    }
    /**
     * <b>Return whether error code generation is enabled</b>
     *
     * @return {@code true} if code generation is enabled; {@code false} otherwise
     */
    public boolean generateCode() {
        return this.generateCode;
    }
    /**
     * <b>Set the registry file path</b>
     *
     * <p>Defaults to {@code "enrichable-registry.log"} in the working directory.
     * Only used when {@link #generateCode(boolean) generateCode(true)} is set.</p>
     *
     * @param registryPath the path to the registry file
     * @return this config, for method chaining
     * @throws IllegalArgumentException if {@code registryPath} is null or blank
     */
    public LogConfig registryPath(String registryPath) {
        EnrichValidator.requireNonBlank(registryPath, "Registry path");
        this.registryPath = registryPath;
        return this;
    }
    /**
     * <b>Return the registry file path</b>
     *
     * @return the configured registry file path
     */
    public String registryPath() {
        return this.registryPath;
    }
}