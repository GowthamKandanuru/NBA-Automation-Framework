package com.veeva.framework.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * ConfigManager - Centralized configuration loader.
 * Priority order: System Properties > Environment Variables > config.yaml > default
 * All project configuration is externalized — nothing is hardcoded.
 */
public class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static final Map<String, String> configMap = new HashMap<>();

    static {
        loadYamlConfig();
    }

    @SuppressWarnings("unchecked")
    private static void loadYamlConfig() {
        try (InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream("config.yaml")) {
            if (is != null) {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                Map<String, Object> yaml = mapper.readValue(is, Map.class);
                flattenMap("", yaml, configMap);
                log.info("Loaded {} config properties from config.yaml", configMap.size());
            }
        } catch (Exception e) {
            log.warn("Could not load config.yaml: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void flattenMap(String prefix, Map<String, Object> map, Map<String, String> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                flattenMap(key, (Map<String, Object>) entry.getValue(), result);
            } else {
                result.put(key, String.valueOf(entry.getValue()));
            }
        }
    }

    /**
     * Resolves a config key using priority: System Property > Env Var > YAML > default
     */
    public static String get(String key, String defaultValue) {
        // 1. System property (e.g., -Dbrowser=firefox)
        String val = System.getProperty(key);
        if (val != null) return val;

        // 2. Environment variable (e.g., BROWSER=firefox)
        val = System.getenv(key.toUpperCase().replace(".", "_"));
        if (val != null) return val;

        // 3. YAML config
        val = configMap.get(key);
        if (val != null) return val;

        // 4. Default
        return defaultValue;
    }

    public static String get(String key) {
        return get(key, null);
    }
}
