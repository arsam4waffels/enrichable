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

public class ErrorRegistry {

    private static final Object REGISTRY_LOCK = new Object();
    private static final ErrorRegistry INSTANCE = new ErrorRegistry();

    private ErrorRegistry() {}

    public static ErrorRegistry getInstance() {
        return INSTANCE;
    }

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

    public Optional<String> lookup(String code, LogConfig config) {
        try {
            String content = Files.readString(
                    Path.of(config.registryPath()),
                    StandardCharsets.UTF_8
            );

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