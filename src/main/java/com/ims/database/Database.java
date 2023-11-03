package com.ims.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    private static Connection connection;
    
    public static Connection getConnection() {
        return connection;
    }
    
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
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    public static void closeStuff(
        ResultSet resultSet,
        PreparedStatement preparedStatement
    ) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
        
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
    }
}
