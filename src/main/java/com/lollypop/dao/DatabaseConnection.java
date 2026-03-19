//package com.lollypop.dao;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
///**
// * Provides a lazily-initialised JDBC connection singleton.
// * Replace the constants with your environment values or inject via config.
// * For production workloads, swap this for a HikariCP connection pool.
// */
//public class DatabaseConnection {
//
//    private static final String DB_URL   = "jdbc:mysql://localhost:3306/lollypop?useSSL=false&serverTimezone=UTC";
//    private static final String USER     = "root";
//    private static final String PASSWORD = "test";
//
//    private static Connection instance;
//
//    private DatabaseConnection() {}
//
//    public static synchronized Connection getInstance() throws SQLException {
//        if (instance == null || instance.isClosed()) {
//            instance = DriverManager.getConnection(DB_URL, USER, PASSWORD);
//        }
//        return instance;
//    }
//}

package com.lollypop.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class DatabaseConnection {

    private static Connection instance;

    private static String DB_URL;
    private static String USER;
    private static String PASSWORD;

    static {
        try {
            Properties props = new Properties();

//            InputStream input = new FileInputStream("src/main/java/com/lollypop/dao/dbconfig.properties");
            InputStream input = new FileInputStream("dbconfig.properties");

            props.load(input);

            DB_URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Laden der dbconfig.properties", e);
        }
    }

    private DatabaseConnection() {}

    public static synchronized Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        }
        return instance;
    }
}
