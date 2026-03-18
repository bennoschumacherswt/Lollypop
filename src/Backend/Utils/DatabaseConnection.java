package com.matsecom.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


 */
public class DatabaseConnection {

    private static final String URL  = System.getProperty(
            "db.url",
            "jdbc:mysql://localhost:3306/matsecom" +
            "?useSSL=false&serverTimezone=Europe/Berlin&allowPublicKeyRetrieval=true");
    private static final String USER = System.getProperty("db.user",     "root");
    private static final String PASS = System.getProperty("db.password", "");

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Tests the connection — used on application startup. */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }
}