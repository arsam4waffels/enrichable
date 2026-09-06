package com.enrichable.registry;

import com.enrichable.config.ErrorLevel;
import com.enrichable.config.LogConfig;
import com.enrichable.model.EnrichInformation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ErrorRegistryTest {

    @TempDir
    Path tempDir;

    // ==================== Singleton ====================

    /**
     * Should return the same registry instance on every access.
     */
    @Test
    void shouldReturnSameInstance() {
        ErrorRegistry first = ErrorRegistry.getInstance();
        ErrorRegistry second = ErrorRegistry.getInstance();

        assertSame(first, second);
    }

    // ==================== Code Generation ====================

    /**
     * Should generate a six-character hexadecimal code.
     */
    @Test
    void shouldGenerateSixCharacterHexCode() {
        ErrorRegistry registry = ErrorRegistry.getInstance();
        LogConfig config = createConfig("registry.log");

        String code = registry.register(
                createInformationList(),
                "2026-09-06 15:00:00",
                "Test report",
                config
        );

        assertEquals(6, code.length());
        assertTrue(code.matches("[0-9a-f]{6}"));
    }

    /**
     * Should generate the same code for identical information and timestamp.
     */
    @Test
    void shouldGenerateDeterministicCode() {
        ErrorRegistry registry = ErrorRegistry.getInstance();
        LogConfig config = createConfig("registry.log");

        List<EnrichInformation> information = createInformationList();
        String thrownAt = "2026-09-06 15:00:00";

        String firstCode = registry.register(
                information,
                thrownAt,
                "First report",
                config
        );

        String secondCode = registry.register(
                information,
                thrownAt,
                "Second report",
                config
        );

        assertEquals(firstCode, secondCode);
    }

    /**
     * Should generate different codes when the timestamp changes.
     */
    @Test
    void shouldGenerateDifferentCodeForDifferentTimestamps() {
        ErrorRegistry registry = ErrorRegistry.getInstance();
        LogConfig config = createConfig("registry.log");

        List<EnrichInformation> information = createInformationList();

        String firstCode = registry.register(
                information,
                "2026-09-06 15:00:00",
                "First report",
                config
        );

        String secondCode = registry.register(
                information,
                "2026-09-06 15:00:01",
                "Second report",
                config
        );

        assertNotEquals(firstCode, secondCode);
    }

    /**
     * Should generate different codes when exception information changes.
     */
    @Test
    void shouldGenerateDifferentCodeForDifferentInformation() {
        ErrorRegistry registry = ErrorRegistry.getInstance();
        LogConfig config = createConfig("registry.log");

        String thrownAt = "2026-09-06 15:00:00";

        String firstCode = registry.register(
                createInformationList(),
                thrownAt,
                "First report",
                config
        );

        String secondCode = registry.register(
                List.of(new EnrichInformation(
                        "PAYMENT",
                        "PAY-002",
                        "Payment failed",
                        ErrorLevel.CRITICAL
                )),
                thrownAt,
                "Second report",
                config
        );

        assertNotEquals(firstCode, secondCode);
    }

    // ==================== Registration ====================

    /**
     * Should create the registry file when it does not exist.
     */
    @Test
    void shouldCreateRegistryFile() {
        ErrorRegistry registry = ErrorRegistry.getInstance();
        LogConfig config = createConfig("registry.log");

        Path registryPath = Path.of(config.registryPath());

        assertFalse(Files.exists(registryPath));

        registry.register(
                createInformationList(),
                "2026-09-06 15:00:00",
                "Test report",
                config
        );

        assertTrue(Files.exists(registryPath));
    }

    /**
     * Should store the generated code in the registry file.
     */
    @Test
    void shouldStoreGeneratedCodeInRegistry() throws Exception {
        ErrorRegistry registry = ErrorRegistry.getInstance();
        LogConfig config = createConfig("registry.log");

        String code = registry.register(
                createInformationList(),
                "2026-09-06 15:00:00",
                "Test report",
                config
        );

        String content = Files.readString(
                Path.of(config.registryPath())
        );

        assertTrue(content.contains("[CODE: " + code + "]"));
    }

    /**
     * Should store the complete report in the registry file.
     */
    @Test
    void shouldStoreCompleteReport() throws Exception {
        ErrorRegistry registry = ErrorRegistry.getInstance();
        LogConfig config = createConfig("registry.log");

        String report = """
                ENRICHABLE EXCEPTION REPORT
                Total Errors : 1
                [ERROR-1] [ERROR] [PAYMENT:PAY-001]
                Payment failed
                """;

        String code = registry.register(
                createInformationList(),
                "2026-09-06 15:00:00",
                report,
                config
        );

        String content = Files.readString(
                Path.of(config.registryPath())
        );

        assertTrue(content.contains("[CODE: " + code + "]"));
        assertTrue(content.contains(report));
    }

    /**
     * Should create missing parent directories for the registry file.
     */
    @Test
    void shouldCreateMissingParentDirectories() {
        ErrorRegistry registry = ErrorRegistry.getInstance();

        LogConfig config = createConfig(
                "nested/registry/errors/registry.log"
        );

        Path registryPath = Path.of(config.registryPath());

        assertFalse(Files.exists(registryPath));

        registry.register(
                createInformationList(),
                "2026-09-06 15:00:00",
                "Test report",
                config
        );

        assertTrue(Files.exists(registryPath));
    }

    /**
     * Should append new entries without removing existing entries.
     */
    @Test
    void shouldAppendEntries() throws Exception {
        ErrorRegistry registry = ErrorRegistry.getInstance();
        LogConfig config = createConfig("registry.log");

        String firstReport = "First report";
        String secondReport = "Second report";

        String firstCode = registry.register(
                createInformationList(),
                "2026-09-06 15:00:00",
                firstReport,
                config
        );

        String secondCode = registry.register(
                List.of(new EnrichInformation(
                        "DATABASE",
                        "DB-001",
                        "Connection failed",
                        ErrorLevel.ERROR
                )),
                "2026-09-06 15:00:01",
                secondReport,
                config
        );

        String content = Files.readString(
                Path.of(config.registryPath())
        );

        assertTrue(content.contains("[CODE: " + firstCode + "]"));
        assertTrue(content.contains(firstReport));
        assertTrue(content.contains("[CODE: " + secondCode + "]"));
        assertTrue(content.contains(secondReport));
    }

    // ==================== Lookup ====================

    /**
     * Should return the registered report for an existing code.
     */
    @Test
    void shouldLookupRegisteredReport() {
        ErrorRegistry registry = ErrorRegistry.getInstance();
        LogConfig config = createConfig("registry.log");

        String report = "Test exception report";

        String code = registry.register(
                createInformationList(),
                "2026-09-06 15:00:00",
                report,
                config
        );

        Optional<String> result = registry.lookup(code, config);

        assertTrue(result.isPresent());
        assertTrue(result.get().contains("[CODE: " + code + "]"));
        assertTrue(result.get().contains(report));
    }

    /**
     * Should return an empty optional for an unknown code.
     */
    @Test
    void shouldReturnEmptyForUnknownCode() {
        ErrorRegistry registry = ErrorRegistry.getInstance();
        LogConfig config = createConfig("registry.log");

        registry.register(
                createInformationList(),
                "2026-09-06 15:00:00",
                "Test report",
                config
        );

        Optional<String> result = registry.lookup("ffffff", config);

        assertTrue(result.isEmpty());
    }

    /**
     * Should return only the requested report block.
     */
    @Test
    void shouldReturnOnlyRequestedReportBlock() {
        ErrorRegistry registry = ErrorRegistry.getInstance();
        LogConfig config = createConfig("registry.log");

        String firstReport = "First report";
        String secondReport = "Second report";

        String firstCode = registry.register(
                createInformationList(),
                "2026-09-06 15:00:00",
                firstReport,
                config
        );

        registry.register(
                List.of(new EnrichInformation(
                        "DATABASE",
                        "DB-001",
                        "Connection failed",
                        ErrorLevel.ERROR
                )),
                "2026-09-06 15:00:01",
                secondReport,
                config
        );

        Optional<String> result = registry.lookup(firstCode, config);

        assertTrue(result.isPresent());
        assertTrue(result.get().contains(firstReport));
        assertFalse(result.get().contains(secondReport));
    }

    /**
     * Should return the last report block when looking up the final entry.
     */
    @Test
    void shouldLookupLastReportBlock() {
        ErrorRegistry registry = ErrorRegistry.getInstance();
        LogConfig config = createConfig("registry.log");

        String firstReport = "First report";
        String secondReport = "Last report";

        registry.register(
                createInformationList(),
                "2026-09-06 15:00:00",
                firstReport,
                config
        );

        String secondCode = registry.register(
                List.of(new EnrichInformation(
                        "DATABASE",
                        "DB-001",
                        "Connection failed",
                        ErrorLevel.ERROR
                )),
                "2026-09-06 15:00:01",
                secondReport,
                config
        );

        Optional<String> result = registry.lookup(secondCode, config);

        assertTrue(result.isPresent());
        assertTrue(result.get().contains(secondReport));
        assertFalse(result.get().contains(firstReport));
    }

    /**
     * Should return an empty optional when the registry file does not exist.
     */
    @Test
    void shouldReturnEmptyWhenRegistryFileDoesNotExist() {
        ErrorRegistry registry = ErrorRegistry.getInstance();
        LogConfig config = createConfig("missing/registry.log");

        Optional<String> result = registry.lookup("af45cb", config);

        assertTrue(result.isEmpty());
    }

    // ==================== Failure Handling ====================

    /**
     * Should not propagate an exception when registry writing fails.
     */
    @Test
    void shouldNotPropagateWriteFailure() throws Exception {
        ErrorRegistry registry = ErrorRegistry.getInstance();

        Path directory = tempDir.resolve("registry-directory");
        Files.createDirectory(directory);

        LogConfig config = createConfig(directory.toString());

        assertDoesNotThrow(() ->
                registry.register(
                        createInformationList(),
                        "2026-09-06 15:00:00",
                        "Test report",
                        config
                )
        );
    }

    /**
     * Should return an empty optional when registry reading fails.
     */
    @Test
    void shouldReturnEmptyWhenRegistryReadFails() throws Exception {
        ErrorRegistry registry = ErrorRegistry.getInstance();

        Path directory = tempDir.resolve("registry-directory");
        Files.createDirectory(directory);

        LogConfig config = createConfig(directory.toString());

        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        PrintStream originalError = System.err;

        try {
            System.setErr(new PrintStream(errorOutput));

            Optional<String> result = registry.lookup("af45cb", config);

            assertTrue(result.isEmpty());
        } finally {
            System.setErr(originalError);
        }

        assertTrue(
                errorOutput.toString().contains("[Registry lookup failed]")
        );
    }

    // ==================== Test Helpers ====================

    private LogConfig createConfig(String fileName) {
        return new LogConfig()
                .registryPath(tempDir.resolve(fileName).toString());
    }

    private List<EnrichInformation> createInformationList() {
        return List.of(
                new EnrichInformation(
                        "PAYMENT",
                        "PAY-001",
                        "Payment failed",
                        ErrorLevel.ERROR
                )
        );
    }
}