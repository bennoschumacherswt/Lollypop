package com.lollypop.dao;

import java.io.IOException;
import java.io.InputStream;
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

    private static String DB_URL;
    private static String USER;
    private static String PASSWORD;

    private static Connection instance;

    private DatabaseConnection() {}

    static {
        try (InputStream input = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("dbconfig.properties")) {

            if (input == null) {
                throw new RuntimeException("dbconfig.properties not found");
            }

            Properties props = new Properties();
            props.load(input);

            DB_URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

        } catch (IOException e) {
            throw new RuntimeException("Error loading database configuration", e);
        }
    }

    public static synchronized Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        }
        return instance;
    }
}