package com.enrichable.formatter;

import java.time.format.DateTimeFormatter;
import com.enrichable.config.ConsoleConfig;
import com.enrichable.model.EnrichInformation;
import java.util.List;
import java.util.Map;
/**
 * Default implementation of {@link EnrichFormatter} for console output.
 *
 * <p>Formats a list of {@link EnrichInformation} entries into a compact,
 * human-readable string controlled by a {@link ConsoleConfig}. The output
 * starts with a summary header listing the total error count and all
 * context-code pairs, followed by one line per error entry with its
 * optional fields and metadata.</p>
 *
 * <h5>Example output:</h5>
 * <pre>{@code
 * [2-ERRORS][DATABASE:DB-001][CACHE:CACHE-001]
 * [ERROR-1][2026-09-06 14:23:01][CRITICAL][DATABASE:DB-001] Connection failed
 * [userId=1042][retryCount=3]
 * [ERROR-2][2026-09-06 14:23:01][WARNING][CACHE:CACHE-001] Cache unavailable
 * }</pre>
 *
 * <p>Which fields appear is controlled by the active {@link ConsoleConfig}:
 * error count, timestamp, error level, and metadata can each be toggled
 * independently.</p>
 *
 * @see EnrichFormatter
 * @see ConsoleConfig
 */
public class DefaultEnrichFormatter implements EnrichFormatter {
    private final ConsoleConfig consoleConfig;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * <b>Create a formatter with the given console configuration</b>
     *
     * @param consoleConfig the configuration controlling which fields appear
     *                      in the formatted output
     */
    public DefaultEnrichFormatter(ConsoleConfig consoleConfig) {
        this.consoleConfig = consoleConfig;
    }
    /**
     * <h5>Format a list of error entries into a console-ready string</h5>
     *
     * <p>The output has two sections:</p>
     * <ul>
     *   <li><b>Header</b> — total error count and all context-code pairs on one line</li>
     *   <li><b>Entries</b> — one line per error with optional index, timestamp,
     *       level, context, code, message, and metadata</li>
     * </ul>
     *
     * <p>Which optional fields appear is determined by the active
     * {@link ConsoleConfig}.</p>
     *
     * @param list the error entries to format; must not be {@code null}
     * @return a formatted string ready for console output
     */
    @Override
    public String format(List<EnrichInformation> list) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[").append(list.size()).append("-ERRORS]");
        for (EnrichInformation info : list) {
            stringBuilder.append("[").append(info.getContext());
            if (info.getCode() != null)
                stringBuilder.append(":").append(info.getCode());
            stringBuilder.append("]");
        }
        stringBuilder.append("\n");
        for (int i = 0; i < list.size(); i++) {
            EnrichInformation info = list.get(i);

            if (consoleConfig.showErrorCount())
                stringBuilder.append("[ERROR-")
                        .append(i + 1)
                        .append("]");

            if (consoleConfig.showTimestamp())
                stringBuilder.append("[")
                        .append(info.getDateTime().format(DATE_TIME_FORMATTER))
                        .append("]");

            if (consoleConfig.showErrorLevel())
                stringBuilder.append("[")
                        .append(info.getErrorLevel())
                        .append("]");

            stringBuilder.append("[")
                    .append(info.getContext())
                    .append(":")
                    .append(info.getCode())
                    .append("] ")
                    .append(info.getMessage())
                    .append("\n");

            if (consoleConfig.showMetadata())
                for (Map.Entry<String, String> stringEntry : info.getMetadata().entrySet())
                    stringBuilder.append("[")
                            .append(stringEntry.getKey())
                            .append("=")
                            .append(stringEntry.getValue())
                            .append("]\n");
        }
        return stringBuilder.toString();
    }
}