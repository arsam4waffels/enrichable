package com.enrichable.registry;

import com.enrichable.config.LogConfig;
import com.enrichable.model.EnrichInformation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * <h2>Manages the enrichable error code registry.</h2>
 *
 * <p>Every time an {@code EnrichableException} is logged with
 * {@link LogConfig#generateCode(boolean) generateCode(true)}, a unique
 * 6-character hex code is generated for that exception session and stored
 * alongside its full report in a registry file. The code can later be used
 * to retrieve the exact report via {@link #lookup(String, LogConfig)}.</p>
 *
 * <h5>Registry file structure:</h5>
 * <pre>{@code
 * [CODE: af45cb]
 * ════════════════════════════════════════════════════
 *   ENRICHABLE EXCEPTION REPORT
 *   Total Errors : 3
 *   Thrown At    : 2026-09-06 14:23:01
 * ════════════════════════════════════════════════════
 *
 *   [ERROR-1] [ERROR] [Database:DB-001]
 *   Connection failed
 *     └─ Time : 2026-09-06 14:23:01
 *
 * [CODE: 3d9f12]
 * ════════════════════════════════════════════════════
 * ...
 * }</pre>
 *
 * <h5>Code generation:</h5>
 * <p>Codes are derived from the exception content and timestamp using SHA-256,
 * truncated to 6 hex characters. The timestamp ensures that two runs with
 * identical errors still produce different codes.</p>
 *
 * <h5>Thread safety:</h5>
 * <p>All registry writes are synchronized on a dedicated internal lock.
 * Reads ({@link #lookup(String, LogConfig)}) are not synchronized (yet) — the
 * registry file is append-only, so a lookup will never observe a partially
 * written block from a concurrent write.</p>
 *
 * <h5>Usage:</h5>
 * <pre>{@code
 * // logging side — code is returned by writeLog()
 * String code = exception.writeLog();
 * System.out.println("Error code: " + code); // af45cb
 *
 * // lookup side — retrieve the full report by code
 * ErrorRegistry.getInstance()
 *         .lookup("af45cb", logConfig)
 *         .ifPresent(System.out::println);
 * }</pre>
 *
 * @see LogConfig#generateCode(boolean)
 * @see LogConfig#registryPath(String)
 */
public class ErrorRegistry {

    private static final Object REGISTRY_LOCK = new Object();
    private static final ErrorRegistry INSTANCE = new ErrorRegistry();

    /**
     * No instance creation is allowed outside this class.
     * Use {@link #getInstance()} to obtain the shared instance.
     */
    private ErrorRegistry() {}

    public static ErrorRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers an exception session in the registry.
     *
     * <p>Generates a unique 6-character code from the given information and
     * timestamp, then appends the code and the pre-built report to the
     * registry file configured in {@code config}.</p>
     *
     * <p>If the registry file does not exist, it is created automatically.
     * If writing fails, the error is reported through {@code System.err}
     * and does not propagate to the caller.</p>
     *
     * @param informationList the enriched error entries for this session
     * @param thrownAt        the timestamp of the exception
     * @param report          the formatted report already built by the logger
     * @param config          configuration containing the registry file path
     * @return the generated 6-character hex code (e.g. {@code "af45cb"})
     */
    public String register(
            List<EnrichInformation> informationList,
            String thrownAt,
            String report,
            LogConfig config) {

        String code = generateCode(informationList, thrownAt);

        synchronized (REGISTRY_LOCK) {
            writeToRegistry(code, report, config);
        }

        return code;
    }

    /**
     * Looks up a previously registered code in the registry file.
     *
     * <p>Reads the entire registry file and extracts the report block
     * associated with the given code. The block starts at the
     * {@code [CODE: xxxxxx]} marker and ends at the next marker or
     * the end of the file.</p>
     *
     * <p>Returns {@link Optional#empty()} if:</p>
     * <ul>
     *   <li>the code does not exist in the registry</li>
     *   <li>the registry file does not exist or cannot be read</li>
     * </ul>
     *
     * @param code   the 6-character hex code to look up (e.g. {@code "af45cb"})
     * @param config configuration containing the registry file path
     * @return an {@link Optional} containing the full report block,
     *         or {@link Optional#empty()} if not found
     */
    public Optional<String> lookup(String code, LogConfig config) {
        try {
            String content = Files.readString(
                    Path.of(config.registryPath()),
                    StandardCharsets.UTF_8
            );

            // Each block starts with [CODE: af45cb]
            String marker = "[CODE: " + code + "]";
            int start = content.indexOf(marker);

            if (start == -1) return Optional.empty();

            // Find next block or end of file
            int nextBlock = content.indexOf("[CODE: ", start + marker.length());
            String block = nextBlock == -1
                    ? content.substring(start)
                    : content.substring(start, nextBlock);

            return Optional.of(block.trim());

        } catch (IOException e) {
            System.err.println("[Registry lookup failed] " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * <h4>Generates a unique 6-character hex code for an exception session.</h4>
     *
     * <p>The code is derived from the content of all {@link EnrichInformation}
     * entries combined with the {@code thrownAt} timestamp, passed through
     * SHA-256 and truncated to the first 6 hex characters.</p>
     *
     * <p>Including the timestamp ensures that two runs with identical errors
     * still produce different codes. The same run will always produce the
     * same code (deterministic).</p>
     *
     * <h5>Example output:</h5>
     * <pre>{@code
     * "af45cb"
     * }</pre>
     *
     * <h5>Collision probability:</h5>
     * <p>6 hex characters yield ~16 million possible codes.
     * Collisions are unlikely in normal usage but not impossible.
     * This is intentional for readability — not a security guarantee.</p>
     *
     * @param informationList the list of enriched error entries to hash
     * @param thrownAt        the timestamp of the exception, included to ensure
     *                        per-run uniqueness
     * @return a 6-character lowercase hex string (e.g. {@code "af45cb"})
     * @throws IllegalStateException if SHA-256 is unavailable in the runtime,
     *                               which should never occur in standard Java
     */
    private String generateCode(
            List<EnrichInformation> informationList,
            String thrownAt) {

        StringBuilder raw = new StringBuilder(thrownAt);

        for (EnrichInformation info : informationList) {
            raw.append(info.getContext())
                    .append(info.getCode())
                    .append(info.getMessage())
                    .append(info.getErrorLevel());
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    raw.toString().getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(hash).substring(0, 6);

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available in Java... but we can't be sure.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Appends a registry entry to the configured registry file.
     *
     * <p>Each entry starts with a {@code [CODE: xxxxxx]} marker followed
     * by the full exception report. Entries are always appended — existing
     * registry content is never modified or removed.</p>
     *
     * <p>If the file or its parent directories do not exist, they are
     * created automatically. Write failures are reported through
     * {@code System.err} and do not propagate to the caller.</p>
     *
     * @param code   the generated code to use as the entry marker
     * @param report the formatted exception report to store
     * @param config configuration containing the registry file path
     */
    private void writeToRegistry(
            String code,
            String report,
            LogConfig config) {

        try {
            Path path = Path.of(config.registryPath());
            Path parent = path.getParent();

            if (parent != null)
                Files.createDirectories(parent);

            String entry = "[CODE: " + code + "]\n" + report + "\n";

            Files.writeString(
                    path,
                    entry,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (IOException e) {
            System.err.println("[Registry write failed] " + e.getMessage());
        }
    }
}