package com.gabut.pingguard.util;

import com.gabut.pingguard.model.AppConfig;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Utility class for saving and loading application configuration
 * to/from a .properties file.
 *
 * The config file is stored in the user's home directory under
 * .pingguard/config.properties for persistence across sessions.
 */
public class ConfigManager {

    private static final String CONFIG_DIR = ".pingguard";
    private static final String CONFIG_FILE = "config.properties";

    private static final String KEY_TARGET_IP = "target.ip";
    private static final String KEY_THRESHOLD_MS = "threshold.ms";
    private static final String KEY_INTERVAL_SEC = "interval.seconds";

    /**
     * Returns the path to the configuration file.
     */
    private static Path getConfigPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, CONFIG_DIR, CONFIG_FILE);
    }

    /**
     * Saves the given AppConfig to the properties file.
     *
     * @param config the configuration to save
     */
    public static void save(AppConfig config) {
        try {
            Path configPath = getConfigPath();
            Files.createDirectories(configPath.getParent());

            Properties props = new Properties();
            props.setProperty(KEY_TARGET_IP, config.getTargetIp());
            props.setProperty(KEY_THRESHOLD_MS, String.valueOf(config.getThresholdMs()));
            props.setProperty(KEY_INTERVAL_SEC, String.valueOf(config.getIntervalSeconds()));

            try (OutputStream out = Files.newOutputStream(configPath)) {
                props.store(out, "PingGuard Configuration");
            }

            System.out.println("ConfigManager: Saved config to " + configPath);
        } catch (IOException e) {
            System.err.println("ConfigManager: Failed to save config - " + e.getMessage());
        }
    }

    /**
     * Loads the AppConfig from the properties file.
     * Returns a default AppConfig if the file does not exist or is invalid.
     *
     * @return the loaded configuration, or defaults
     */
    public static AppConfig load() {
        Path configPath = getConfigPath();
        AppConfig config = new AppConfig();

        if (!Files.exists(configPath)) {
            System.out.println("ConfigManager: No config file found, using defaults.");
            return config;
        }

        try (InputStream in = Files.newInputStream(configPath)) {
            Properties props = new Properties();
            props.load(in);

            config.setTargetIp(
                    props.getProperty(KEY_TARGET_IP, config.getTargetIp()));
            config.setThresholdMs(
                    Integer.parseInt(props.getProperty(KEY_THRESHOLD_MS,
                            String.valueOf(config.getThresholdMs()))));
            config.setIntervalSeconds(
                    Integer.parseInt(props.getProperty(KEY_INTERVAL_SEC,
                            String.valueOf(config.getIntervalSeconds()))));

            System.out.println("ConfigManager: Loaded config from " + configPath);
        } catch (IOException | NumberFormatException e) {
            System.err.println("ConfigManager: Error loading config, using defaults - " + e.getMessage());
        }

        return config;
    }
}
