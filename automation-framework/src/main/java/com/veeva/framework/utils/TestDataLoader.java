package com.veeva.framework.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

/**
 * TestDataLoader - Loads test data from JSON or YAML files.
 * Files are read from the classpath (src/test/resources/testdata/).
 */
public class TestDataLoader {

    private static final Logger log = LogManager.getLogger(TestDataLoader.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    private TestDataLoader() {}

    /**
     * Loads test data from a JSON file and maps it to the given POJO class.
     */
    public static <T> T loadJson(String resourcePath, Class<T> clazz) {
        return load(resourcePath, clazz, JSON_MAPPER);
    }

    /**
     * Loads test data from a YAML file and maps it to the given POJO class.
     */
    public static <T> T loadYaml(String resourcePath, Class<T> clazz) {
        return load(resourcePath, clazz, YAML_MAPPER);
    }

    private static <T> T load(String resourcePath, Class<T> clazz, ObjectMapper mapper) {
        try (InputStream is = TestDataLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Test data file not found: " + resourcePath);
            }
            T data = mapper.readValue(is, clazz);
            log.info("Loaded test data from: {}", resourcePath);
            return data;
        } catch (Exception e) {
            log.error("Failed to load test data: {}", resourcePath, e);
            throw new RuntimeException("Failed to load test data: " + resourcePath, e);
        }
    }
}
