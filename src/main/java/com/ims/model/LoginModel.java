package com.ims.model;

import com.ims.components.PopupService;
import com.ims.database.DBUsers;
import com.ims.utils.AsyncCaller;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class LoginModel {
    public static final BooleanProperty isBusyLogin = new SimpleBooleanProperty(false);
    public static final StringProperty emailProperty = new SimpleStringProperty("");
    public static final StringProperty passwordProperty = new SimpleStringProperty("");
    public static final BooleanProperty validProperty = new SimpleBooleanProperty(true);
    
    public static void login() {
        if (isBusyLogin.get()) {
            System.out.println("Action is taken too fast.");
            return;
        }
        
        isBusyLogin.set(true);
        String email = emailProperty.get();
        String password = passwordProperty.get();
        if (email.isEmpty() || password.isEmpty()) {
            validProperty.set(false);
            isBusyLogin.set(false);
            return;
        }
        
        new AsyncCaller<Void>(task -> {
            DBUsers.UserListData users = DBUsers.get(
                DBUsers.Column.EMAIL,
                email
            );
            
            if (users.size() != 1) {
                validProperty.set(false);
                isBusyLogin.set(false);
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
                    
                    // setup the user first...
                    int userID = userData.getID();
                    UserManagerModel.loadAndGetUser(userID)
                        .onSucceeded(userObject -> {
                            UserSessionModel.currentUser.set(userObject);
                            
                            // ...then the role...
                            Integer roleID = userData.getRoleID();
                            if (roleID != null) {
                                UserManagerModel.loadAndGetRole(
                                    roleID
                                ).onSucceeded(roleObject -> {
                                    UserSessionModel.currentUserRole.set(roleObject);
                                    
                                    SceneManager.setScene("base");
                                    validProperty.set(true);
                                    isBusyLogin.set(false);
                                }).onFailed(e -> {
                                    isBusyLogin.set(false);
                                }).execute();
                            } else {
                                SceneManager.setScene("base");
                                validProperty.set(true);
                                isBusyLogin.set(false);
                            }
                            
                        }).onFailed(e -> {
                            isBusyLogin.set(false);
                        }).execute();
                    
                } else {
                    validProperty.set(false);
                    isBusyLogin.set(false);
                }
            });
            
            return null;
        }, Utils.executor).onFailed(e -> {
            isBusyLogin.set(false);
        }).execute();
    }
}
