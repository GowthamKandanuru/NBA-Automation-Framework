package com.veeva.framework.utils;

import com.opencsv.CSVWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.List;

/**
 * FileUtils - Reusable file I/O operations.
 * Supports text file writing and CSV generation.
 */
public class FileUtils {

    private static final Logger log = LogManager.getLogger(FileUtils.class);

    private FileUtils() {}

    /**
     * Writes lines to a text file. Creates parent directories if needed.
     */
    public static String writeToTextFile(String filePath, List<String> lines) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Written {} lines to: {}", lines.size(), filePath);
            return filePath;
        } catch (IOException e) {
            log.error("Failed to write text file: {}", filePath, e);
            throw new RuntimeException("Failed to write file: " + filePath, e);
        }
    }

    /**
     * Writes data to a CSV file.
     * @param filePath    destination path
     * @param headers     column headers
     * @param rows        data rows (each row is a String[])
     */
    public static String writeToCsvFile(String filePath, String[] headers, List<String[]> rows) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
                writer.writeNext(headers);
                writer.writeAll(rows);
            }
            log.info("Written CSV with {} rows to: {}", rows.size(), filePath);
            return filePath;
        } catch (IOException e) {
            log.error("Failed to write CSV file: {}", filePath, e);
            throw new RuntimeException("Failed to write CSV: " + filePath, e);
        }
    }

    /**
     * Reads all lines from a file.
     */
    public static List<String> readLines(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to read file: {}", filePath, e);
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }
}
