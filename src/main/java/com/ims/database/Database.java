package com.ims.database;

import com.ims.Config;
import com.ims.utils.Env;
import com.ims.utils.SceneManager;
import javafx.application.Platform;
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
import java.util.concurrent.atomic.AtomicReference;

public class Database {
    private static Connection connection = null;
    private static final BooleanProperty isConnected = new SimpleBooleanProperty(true);
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public static BooleanProperty isConnectedProperty() {
        return isConnected;
    }
    
    public static Connection getConnection() {
        try {
            if (connection != null) {
                isConnected.set(!connection.isClosed());
            } else {
                isConnected.set(false);
            }
        } catch (SQLException e) {
            isConnected.set(false);
            e.printStackTrace();
        }
        
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
        initializeReconnector();
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
        
        AtomicReference<String> oldScene = new AtomicReference<>();
        Database.isConnectedProperty().addListener(e -> {
            Platform.runLater(() -> {
                if (!Database.isConnectedProperty().get()) {
                    oldScene.set(SceneManager.getCurrentSceneID());
                    SceneManager.setScene("disconnected");
                } else {
                    if (oldScene.get() != null) {
                        SceneManager.setScene(oldScene.get());
                    }
                }
            });
        });
        
        scheduler.scheduleAtFixedRate(() -> {
            boolean _isConnected = true;
            try {
                if (connection == null)  {
                    _isConnected = false;
                } else {
                    Statement statement = connection.createStatement();
                    statement.execute("SELECT 1");
                    _isConnected = !connection.isClosed();
                }
            } catch (SQLException e) {
                _isConnected = false;
                System.out.println(e);
            }
            
            isConnected.set(_isConnected);
            if (!_isConnected) {
                Database.connect();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
}
