package com.lollypop.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides a lazily-initialised JDBC connection singleton.
 * Replace the constants with your environment values or inject via config.
 * For production workloads, swap this for a HikariCP connection pool.
 */
public class DatabaseConnection {

    private static final String DB_URL   = "jdbc:mysql://localhost:3306/lollypop?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "hafid007";

    private static Connection instance;

    private DatabaseConnection() {}

    public static synchronized Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        }
        return instance;
    }
}
