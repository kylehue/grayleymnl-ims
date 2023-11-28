package com.ims.model;

import com.ims.database.DBRoles;
import com.ims.database.DBUsers;
import com.ims.utils.AsyncCaller;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class RegisterModel {
    public static StringProperty emailProperty = new SimpleStringProperty("");
    public static StringProperty passwordProperty = new SimpleStringProperty("");
    public static StringProperty confirmPasswordProperty = new SimpleStringProperty("");
    
    public static void register() {
        String email = emailProperty.get();
        String password = passwordProperty.get();
        
        if (email.isEmpty() || password.isEmpty()) return;
        
        new AsyncCaller<Void>(task -> {
            DBUsers.add(email, Utils.hashPassword(password));
            return null;
        }, Utils.executor).onSucceeded(e -> {
            LoginModel.emailProperty.set(email);
            LoginModel.passwordProperty.set(password);
            SceneManager.setScene("login");
        }).execute();
    }
    
    public static AsyncCaller<Boolean> emailExists(String email) {
        return new AsyncCaller<>(task -> {
            return !DBUsers.get(DBUsers.Column.EMAIL, email).isEmpty();
        }, Utils.executor);
    }
    
    public static AsyncCaller<Boolean> emailNotExists(String email) {
        return new AsyncCaller<>(task -> {
            return DBUsers.get(DBUsers.Column.EMAIL, email).isEmpty();
        }, Utils.executor);
    }
}
