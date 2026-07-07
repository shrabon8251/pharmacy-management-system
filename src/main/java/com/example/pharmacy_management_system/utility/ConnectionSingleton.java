package com.example.pharmacy_management_system.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionSingleton {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Change this to your MySQL/MariaDB root password
    private static final String DB_NAME = "pharmacy_management_system";
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;

    private static ConnectionSingleton instance;
    private Connection connection;

    private ConnectionSingleton() throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static synchronized ConnectionSingleton getInstance() throws SQLException {
        if (instance == null || instance.connection == null || instance.connection.isClosed()) {
            instance = new ConnectionSingleton();
        }
        return instance;
    }

    public static Connection getConnection() throws SQLException {
        return getInstance().connection;
    }
}
