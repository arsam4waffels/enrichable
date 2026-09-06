package com.enrichable.config;
/**
 * Configuration for console output formatting in {@link com.enrichable.EnrichableException}.
 *
 * <p>Controls which fields appear when the exception is printed to the console
 * via {@link com.enrichable.EnrichableException#toString()}. All options default
 * to {@code true} so the output is as informative as possible out of the box.</p>
 *
 * <h5>Usage:</h5>
 * <pre>{@code
 * ConsoleConfig config = new ConsoleConfig()
 *         .showTimestamp(true)
 *         .showErrorLevel(true)
 *         .showErrorCount(true)
 *         .showMetadata(false);
 *
 * exception.setConsoleConfig(config);
 * System.out.println(exception);
 * }</pre>
 *
 * <h5>Defaults:</h5>
 * <ul>
 *   <li>{@code showTimestamp} -> {@code true}</li>
 *   <li>{@code showErrorLevel} -> {@code true}</li>
 *   <li>{@code showErrorCount} -> {@code true}</li>
 *   <li>{@code showMetadata} -> {@code true}</li>
 * </ul>
 *
 * <p>Console and file logging configuration are independent — changing one
 * does not affect the other.</p>
 *
 * @see com.enrichable.EnrichableException#setConsoleConfig(ConsoleConfig)
 * @see com.enrichable.formatter.DefaultEnrichFormatter
 */
public class ConsoleConfig {
    private boolean showTimestamp = true;
    private boolean showErrorLevel = true;
    private boolean showErrorCount = true;
    private boolean showMetadata = true;
    /**
     * <b>Set whether timestamps appear in the console output</b>
     *
     * @param showTimestamp {@code true} to include timestamps, {@code false} to hide them
     * @return this config, for method chaining
     */
    public ConsoleConfig showTimestamp(boolean showTimestamp) {
        this.showTimestamp = showTimestamp;
        return this;
    }
    /**
     * <b>Return whether timestamps appear in the console output</b>
     *
     * @return {@code true} if timestamps are shown; {@code false} otherwise
     */
    public boolean showTimestamp() {
        return this.showTimestamp;
    }
    /**
     * <b>Set whether error levels appear in the console output</b>
     *
     * @param showErrorLevel {@code true} to include error levels, {@code false} to hide them
     * @return this config, for method chaining
     */
    public ConsoleConfig showErrorLevel(boolean showErrorLevel) {
        this.showErrorLevel = showErrorLevel;
        return this;
    }
    /**
     * <b>Return whether error levels appear in the console output</b>
     *
     * @return {@code true} if error levels are shown; {@code false} otherwise
     */
    public boolean showErrorLevel() {
        return this.showErrorLevel;
    }
    /**
     * <b>Set whether the total error count appears in the console output</b>
     *
     * @param showErrorCount {@code true} to include the error count, {@code false} to hide it
     * @return this config, for method chaining
     */
    public ConsoleConfig showErrorCount(boolean showErrorCount) {
        this.showErrorCount = showErrorCount;
        return this;
    }
    /**
     * <b>Return whether the total error count appears in the console output</b>
     *
     * @return {@code true} if the error count is shown; {@code false} otherwise
     */
    public boolean showErrorCount() {
        return this.showErrorCount;
    }
    /**
     * <b>Set whether metadata appears in the console output</b>
     *
     * @param showMetadata {@code true} to include metadata, {@code false} to hide it
     * @return this config, for method chaining
     */
    public ConsoleConfig showMetadata(boolean showMetadata) {
        this.showMetadata = showMetadata;
        return this;
    }
    /**
     * <b>Return whether metadata appears in the console output</b>
     *
     * @return {@code true} if metadata is shown; {@code false} otherwise
     */
    public boolean showMetadata() {
        return this.showMetadata;
    }
}
