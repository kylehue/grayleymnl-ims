package com.ims.utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseManager {
    private static Connection connection;
    
    public static void connect() {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/grayleyims";
            String user = "postgres";
            String password = "123";
            
            connection = DriverManager.getConnection(url, user, password);
            
            if (connection != null) {
                System.out.println("Successfully connected to the database.");
            } else {
                System.out.println("Database connection has failed.");
            }
        } catch(Exception e) {
            System.out.println(e);
        }
    }
    
    public static Connection getConnection() {
        return connection;
    }
}
