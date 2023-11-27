package com.ims.model;

import com.ims.database.DBRoles;
import com.ims.database.DBUsers;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class RegisterModel {
    public static ExecutorService executor = Executors.newFixedThreadPool(2);
    
    public static StringProperty emailProperty = new SimpleStringProperty("");
    public static StringProperty passwordProperty = new SimpleStringProperty("");
    public static StringProperty confirmPasswordProperty = new SimpleStringProperty("");
    
    public static void register() {
        String email = emailProperty.get();
        String password = passwordProperty.get();
        
        if (email.isEmpty() || password.isEmpty()) return;
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                DBUsers.add(email, Utils.hashPassword(password));
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            LoginModel.emailProperty.set(email);
            LoginModel.passwordProperty.set(password);
            SceneManager.setScene("login");
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        executor.submit(task);
    }
    
    public static boolean emailExists(String email) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return !DBUsers.get(DBUsers.Column.EMAIL, email).isEmpty();
            }
        };
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        executor.submit(task);
        
        boolean emailExists = false;
        try {
            emailExists = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return emailExists;
    }
}
