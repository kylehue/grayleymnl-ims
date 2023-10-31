package com.ims.model;

import com.ims.database.DBUsers;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public abstract class RegisterModel {
    public static StringProperty emailProperty = new SimpleStringProperty("");
    public static StringProperty passwordProperty = new SimpleStringProperty("");
    public static StringProperty confirmPasswordProperty = new SimpleStringProperty("");
    
    public static void register() {
        String email = emailProperty.get();
        String password = passwordProperty.get();
        
        if (email.isEmpty() || password.isEmpty()) return;
        
        DBUsers.insertToUsers(email, Utils.hashPassword(password));
        
        LoginModel.emailProperty.set(email);
        LoginModel.passwordProperty.set(password);
        SceneManager.setScene("login");
    }
}
