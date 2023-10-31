package com.ims.model;

import com.ims.database.DBUsers;
import com.ims.database.DBUsersColumn;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.HashMap;

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
        
        ArrayList<HashMap<DBUsersColumn, Object>> users = DBUsers.getUsersWithLabel(
            DBUsersColumn.EMAIL,
            email
        );
        
        if (users.isEmpty()) {
            validProperty.set(false);
            return;
        }
        
        HashMap<DBUsersColumn, Object> user = users.get(0);
        boolean isCorrectPassword = Utils.checkPassword(
            password,
            user.get(DBUsersColumn.PASSWORD).toString()
        );
        
        if (isCorrectPassword) {
            SceneManager.setScene("base");
            validProperty.set(true);
        } else {
            validProperty.set(false);
        }
    }
}
