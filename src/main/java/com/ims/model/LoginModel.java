package com.ims.model;

import com.ims.components.PopupService;
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
    public static ExecutorService executor = Executors.newSingleThreadExecutor();
    
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
                DBUsers.UserListData users = DBUsers.get(
                    DBUsers.Column.EMAIL,
                    email
                );
                
                if (users.size() != 1) {
                    validProperty.set(false);
                    return null;
                }
                
                DBUsers.UserData userData = users.getFirst();
                boolean isCorrectPassword = Utils.checkPassword(
                    password,
                    userData.getPassword()
                );
                
                Platform.runLater(() -> {
                    if (isCorrectPassword) {
                        if (userData.isDisabled() && !userData.isOwner()) {
                            PopupService.messageDialog.setup(
                                "Disabled Account",
                                "Your account has been disabled by the owner.",
                                "Close"
                            ).show();
                            return;
                        }
                        
                        int id = userData.getID();
                        
                        UserSessionModel.currentUser.set(
                            UserManagerModel.loadAndGetUser(id)
                        );
                        
                        Integer roleID = userData.getRoleID();
                        if (roleID != null) {
                            UserSessionModel.currentUserRole.set(
                                UserManagerModel.loadAndGetRole(roleID)
                            );
                        }
                        
                        SceneManager.setScene("base");
                        validProperty.set(true);
                    } else {
                        validProperty.set(false);
                    }
                });
                
                return null;
            }
        };
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        executor.submit(task);
    }
}
