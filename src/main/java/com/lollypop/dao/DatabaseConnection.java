package com.lollypop.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Provides a lazily-initialised JDBC connection singleton.
 * Replace the constants with your environment values or inject via config.
 * For production workloads, swap this for a HikariCP connection pool.
 */
public class DatabaseConnection {

    private static final String CONFIG_FILE_NAME = "dbconfig.properties";
    private static final Path[] CONFIG_CANDIDATES = new Path[] {
            Paths.get(CONFIG_FILE_NAME),
            Paths.get("src", "main", "resources", CONFIG_FILE_NAME),
            Paths.get("src", "main", "java", "com", "lollypop", "dao", CONFIG_FILE_NAME)
    };

    private static volatile DbConfig config;
    private static Connection instance;

    private DatabaseConnection() {}

    static {
        config = loadConfig();
    }

    public static synchronized Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            DbConfig activeConfig = getCurrentConfig();
            instance = DriverManager.getConnection(activeConfig.url(), activeConfig.user(), activeConfig.password());
        }
        return instance;
    }

    public static synchronized DbConfig getCurrentConfig() {
        return config;
    }

    public static synchronized void configure(DbConfig newConfig) {
        config = newConfig;
        closeCurrentConnection();
    }

    public static synchronized void saveConfig(DbConfig newConfig) {
        Path configPath = resolveWritableConfigPath();
        Properties props = new Properties();
        props.setProperty("db.url", newConfig.url());
        props.setProperty("db.user", newConfig.user());
        props.setProperty("db.password", newConfig.password());

        try {
            if (configPath.getParent() != null) {
                Files.createDirectories(configPath.getParent());
            }
            try (OutputStream output = Files.newOutputStream(configPath)) {
                props.store(output, "Database configuration");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving database configuration to " + configPath, e);
        }

        configure(newConfig);
    }

    public static void testConnection(DbConfig testConfig) throws SQLException {
        try (Connection ignored = DriverManager.getConnection(testConfig.url(), testConfig.user(), testConfig.password())) {
            // Successful connection is enough.
        }
    }

    private static DbConfig loadConfig() {
        Path fileConfig = resolveReadableConfigPath();
        if (fileConfig != null) {
            try (InputStream input = Files.newInputStream(fileConfig)) {
                return fromProperties(input);
            } catch (IOException e) {
                throw new RuntimeException("Error loading database configuration from " + fileConfig, e);
            }
        }

        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            if (input == null) {
                throw new RuntimeException("dbconfig.properties not found");
            }
            return fromProperties(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading database configuration", e);
        }
    }

    private static DbConfig fromProperties(InputStream input) throws IOException {
        Properties props = new Properties();
        props.load(input);

        return new DbConfig(
                props.getProperty("db.url", "").trim(),
                props.getProperty("db.user", "").trim(),
                props.getProperty("db.password", "")
        );
    }

    private static Path resolveReadableConfigPath() {
        for (Path candidate : CONFIG_CANDIDATES) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static Path resolveWritableConfigPath() {
        Path readable = resolveReadableConfigPath();
        if (readable != null) {
            return readable;
        }
        return CONFIG_CANDIDATES[0];
    }

    private static void closeCurrentConnection() {
        if (instance == null) {
            return;
        }

        try {
            if (!instance.isClosed()) {
                instance.close();
            }
        } catch (SQLException ignored) {
            // Best effort only; next connect attempt will recreate the connection.
        } finally {
            instance = null;
        }
    }

    public record DbConfig(String url, String user, String password) {}
}
