package com.enrichable.logging;

import com.enrichable.config.LogConfig;
import com.enrichable.model.EnrichInformation;
import com.enrichable.registry.ErrorRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

/**
 * Writes enriched error information to a log file according to a
 * {@link LogConfig} configuration.
 *
 * <p>The logger is thread-safe and uses a shared lock to prevent
 * concurrent writes from interfering with each other.</p>
 */
public class FileEnrichLogger {

    private static final Object LOG_LOCK = new Object();
    private static final FileEnrichLogger INSTANCE = new FileEnrichLogger();

    /**
     * No instance creation is allowed
     */
    private FileEnrichLogger() {}

    /**
     * Writes the given enriched information to the configured log file.
     *
     * <p>The information is filtered according to {@link LogConfig}, then
     * formatted into a report and written to the configured file.</p>
     *
     * @param informationList enriched error information to be logged
     * @param thrownAt timestamp representing when the exception was thrown
     * @param config configuration controlling filtering, formatting, and file output
     */
    public String write(
            List<EnrichInformation> informationList,
            String thrownAt,
            LogConfig config) {

        synchronized (LOG_LOCK) {
            List<EnrichInformation> filteredInformation = filter(
                    informationList,
                    config
            );

            String report = buildReport(filteredInformation, thrownAt, config);

            writeToFile(report, config);

            if (config.generateCode()) {
                return ErrorRegistry.getInstance().register(
                        filteredInformation,
                        thrownAt,
                        report,
                        config
                );
            }
        }

        return null;
    }

    /**
     * Returns the shared instance of the file logger.
     *
     * @return the singleton {@code FileEnrichLogger} instance
     */
    public static FileEnrichLogger getInstance() {
        return INSTANCE;
    }

    /**
     * Filters error information according to the configured error level.
     *
     * <p>If {@code onlyLevel} is configured, only errors with that exact
     * level are included. Otherwise, {@code minimumLevel} is used as the
     * lower bound.</p>
     */
    private List<EnrichInformation> filter(
            List<EnrichInformation> informationList,
            LogConfig config) {

        if (config.onlyLevel() != null)
            return informationList.stream()
                    .filter(info ->
                            info.getErrorLevel() == config.onlyLevel())
                    .toList();

        if (config.minimumLevel() != null)
            return informationList.stream()
                    .filter(info ->
                            info.getErrorLevel()
                                    .isAtLeast(config.minimumLevel()))
                    .toList();

        return informationList;
    }

    /**
     * Builds the textual report that will be written to the log file.
     */
    private String buildReport(
            List<EnrichInformation> informationList,
            String thrownAt,
            LogConfig config) {

        StringBuilder report = new StringBuilder();

        appendHeader(report, informationList.size(), thrownAt, config);

        for (int i = 0; i < informationList.size(); i++) {
            appendError(report, informationList.get(i), i, config);
        }

        return report.toString();
    }

    private void appendHeader(
            StringBuilder report,
            int errorCount,
            String thrownAt,
            LogConfig config) {

        report.append("════════════════════════════════════════════════════\n");
        report.append("  ENRICHABLE EXCEPTION REPORT\n");
        report.append("  Total Errors : ")
                .append(errorCount)
                .append("\n");

        if (config.showTimestamp()) {
            report.append("  Thrown At    : ")
                    .append(thrownAt)
                    .append("\n");
        }

        report.append("════════════════════════════════════════════════════\n");
    }

    private void appendError(
            StringBuilder report,
            EnrichInformation info,
            int index,
            LogConfig config) {

        report.append("\n  [ERROR-")
                .append(index + 1)
                .append("] ");

        if (config.showErrorLevel()) {
            report.append("[")
                    .append(info.getErrorLevel())
                    .append("] ");
        }

        report.append("[")
                .append(info.getContext())
                .append(":")
                .append(info.getCode())
                .append("]\n");

        report.append("  ")
                .append(info.getMessage())
                .append("\n");

        if (config.showTimestamp()) {
            report.append("    └─ Time : ")
                    .append(info.getDateTime())
                    .append("\n");
        }

        if (config.showMetadata()) {
            appendMetadata(report, info);
        }
    }

    private void appendMetadata(
            StringBuilder report,
            EnrichInformation info) {

        for (Map.Entry<String, String> entry :
                info.getMetadata().entrySet()) {

            report.append("    └─ ")
                    .append(entry.getKey())
                    .append(" : ")
                    .append(entry.getValue())
                    .append("\n");
        }
    }

    /**
     * Writes the generated report to the configured file.
     *
     * <p>When {@code clearBeforeWrite} is enabled, the existing file
     * content is replaced. Otherwise, the report is appended.</p>
     *
     * <p>File writing failures are handled internally and do not propagate
     * to the caller. The failure is reported through {@code System.err}.</p>
     *
     * @param content report content to write
     * @param config configuration containing the file path and write mode
     */
    private void writeToFile(String content, LogConfig config) {
        try {
            Path path = Path.of(config.filePath());
            Path parent = path.getParent();

            if (parent != null)
                Files.createDirectories(parent);

            StandardOpenOption writeMode = config.clearBeforeWrite()
                    ? StandardOpenOption.TRUNCATE_EXISTING
                    : StandardOpenOption.APPEND;

            Files.writeString(
                    path,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    writeMode
            );
        } catch (IOException e) {
            System.err.println(
                    "[The error logger failed while logging an error.] "
                            + e.getMessage());
        }
    }
}