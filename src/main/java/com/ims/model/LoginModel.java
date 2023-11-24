package com.ims.model;

import com.ims.database.DBUsers;
import com.ims.model.objects.UserObject;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class LoginModel {
    public static StringProperty emailProperty = new SimpleStringProperty("");
    public static StringProperty passwordProperty = new SimpleStringProperty("");
    public static BooleanProperty validProperty = new SimpleBooleanProperty(true);
    
    public static void login() {
        String email = emailProperty.get();
        String password = passwordProperty.get();
        if (email.isEmpty() || password.isEmpty()) {
            validProperty.set(false);
            return;
        }
        Task<DBUsers.UserListData> task = new Task<>() {
            @Override
            protected DBUsers.UserListData call() {
                return DBUsers.get(
                    DBUsers.Column.EMAIL,
                    email
                );
            }
        };
        
        task.setOnSucceeded(e -> {
            try {
                DBUsers.UserListData users = task.get();
                if (users.size() != 1) {
                    validProperty.set(false);
                    return;
                }
                
                DBUsers.UserData userData = users.getFirst();
                boolean isCorrectPassword = Utils.checkPassword(
                    password,
                    userData.getPassword()
                );
                
                if (isCorrectPassword) {
                    SceneManager.setScene("base");
                    validProperty.set(true);
                    
                    int id = userData.getID();
                    
                    UserSessionModel.currentUser.set(
                        getUser(id)
                    );
                } else {
                    validProperty.set(false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    public static UserObject getUser(int id) {
        Task<UserObject> task = new Task<>() {
            @Override
            protected UserObject call() throws Exception {
                DBUsers.UserData userData = DBUsers.getOne(DBUsers.Column.ID, id);
                if (userData != null) {
                    return new UserObject(
                        id,
                        userData.getEmail(),
                        userData.getPassword(),
                        userData.getJoinedDate(),
                        userData.getLastActivityDate(),
                        userData.getRoleID(),
                        userData.isDisabled(),
                        userData.isOwner()
                    );
                }
                
                return null;
            }
        };
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
        
        UserObject userObject = null;
        try {
            userObject = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return userObject;
    }
}
