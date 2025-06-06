package com.rei.permit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final Properties properties = new Properties();
    
    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            logger.error("Error loading configuration", e);
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static int getIntProperty(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public static long getLongProperty(String key) {
        return Long.parseLong(properties.getProperty(key));
    }

    // Specific configuration getters
    public static int getCheckIntervalMinutes() {
        return getIntProperty("check.interval.minutes");
    }

    public static int getNotificationCooldownMinutes() {
        return getIntProperty("notification.cooldown.minutes");
    }

    public static String getApiBaseUrl() {
        return getProperty("api.base.url");
    }

    public static int getApiTimeoutSeconds() {
        return getIntProperty("api.timeout.seconds");
    }

    public static String getLoggingFilePath() {
        return getProperty("logging.file.path");
    }

    public static int getLoggingMaxHistoryDays() {
        return getIntProperty("logging.max.history.days");
    }
} 