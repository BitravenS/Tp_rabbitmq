package com.tp3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

public class TextFileRepository {
    private final Path filePath;
    private static final String LINE_PATTERN = "\\d+\\s+.+";

    public TextFileRepository(Path filePath) {
        this.filePath = filePath;
    }

    public void ensureDirectory() throws IOException {
        Path dir = filePath.getParent();
        if (dir != null && !Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    public void appendLine(String line) throws IOException {
        if (!isValidLine(line)) {
            throw new IOException("Invalid line format");
        }
        String normalized = line.endsWith("\n") ? line : line + "\n";
        Files.writeString(filePath, normalized, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public boolean isValidLine(String line) {
        if (line == null) {
            return false;
        }
        String trimmed = line.trim();
        return !trimmed.isEmpty() && trimmed.matches(LINE_PATTERN);
    }

    public Optional<String> readLastLine() throws IOException {
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        return Files.lines(filePath, StandardCharsets.UTF_8).reduce((first, second) -> second);
    }

    public List<String> readAllLines() throws IOException {
        if (!Files.exists(filePath)) {
            return List.of();
        }
        return Files.readAllLines(filePath, StandardCharsets.UTF_8);
    }
}
