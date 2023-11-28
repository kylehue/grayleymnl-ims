package com.ims.database;

import com.ims.Config;
import com.ims.utils.Env;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.sql.*;
import java.util.Properties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Database {
    private static Connection connection;
    private static final BooleanProperty isConnected = new SimpleBooleanProperty(false);
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public static BooleanProperty isConnectedProperty() {
        return isConnected;
    }
    
    public static Connection getConnection() {
        return connection;
    }
    
    public static void shutdown() {
        try {
            getConnection().close();
            scheduler.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void connect() {
        try {
            Class.forName("org.postgresql.Driver");
            
            Properties env = Env.get();
            String urlEnvKey = "database.url";
            String usernameEnvKey = "database.username";
            String passwordEnvKey = "database.password";
            
            if (Config.isProductionMode) {
                urlEnvKey += ".hosted";
                usernameEnvKey += ".hosted";
                passwordEnvKey += ".hosted";
            }
            
            String url = env.getProperty(urlEnvKey);
            String username = env.getProperty(usernameEnvKey);
            String password = env.getProperty(passwordEnvKey);
            
            connection = DriverManager.getConnection(url, username, password);
            
            if (connection != null) {
                System.out.println("Successfully connected to the database.");
                isConnected.set(true);
                initializeReconnector();
            } else {
                System.out.println("Database connection has failed.");
                isConnected.set(false);
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
    
    private static boolean isInitializedReconnector = false;
    
    private static void initializeReconnector() {
        if (isInitializedReconnector) {
            return;
        }
        isInitializedReconnector = true;
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Statement statement = connection.createStatement();
                statement.execute("SELECT 1");
                isConnected.set(!connection.isClosed());
            } catch (SQLException e) {
                isConnected.set(false);
                Database.connect();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
}
