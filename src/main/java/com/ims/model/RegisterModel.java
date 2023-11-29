package com.ims.model;

import com.ims.database.DBRoles;
import com.ims.database.DBUsers;
import com.ims.utils.AsyncCaller;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class RegisterModel {
    public static final BooleanProperty isBusyRegister = new SimpleBooleanProperty(false);
    public static final StringProperty emailProperty = new SimpleStringProperty("");
    public static final StringProperty passwordProperty = new SimpleStringProperty("");
    public static final StringProperty confirmPasswordProperty = new SimpleStringProperty("");
    
    public static void register() {
        if (isBusyRegister.get()) {
            System.out.println("Action is taken too fast.");
            return;
        }
        
        isBusyRegister.set(true);
        String email = emailProperty.get();
        String password = passwordProperty.get();
        
        if (email.isEmpty() || password.isEmpty()) {
            isBusyRegister.set(false);
            return;
        }
        
        new AsyncCaller<Void>(task -> {
            DBUsers.add(email, Utils.hashPassword(password));
            return null;
        }, Utils.executor).onSucceeded(e -> {
            LoginModel.emailProperty.set(email);
            LoginModel.passwordProperty.set(password);
            SceneManager.setScene("login");
            isBusyRegister.set(false);
        }).onFailed(e -> {
            isBusyRegister.set(false);
        }).execute();
    }
    
    public static AsyncCaller<Boolean> emailExists(String email) {
        return new AsyncCaller<Boolean>(task -> {
            if (isBusyRegister.get()) {
                System.out.println("Action is taken too fast.");
                return null;
            }
            isBusyRegister.set(true);
            
            return !DBUsers.get(DBUsers.Column.EMAIL, email).isEmpty();
        }, Utils.executor).onSucceeded(e -> {
            isBusyRegister.set(false);
        }).onFailed(e -> {
            isBusyRegister.set(false);
        });
    }
}
