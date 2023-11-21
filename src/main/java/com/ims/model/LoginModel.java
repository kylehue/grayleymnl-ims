package com.ims.model;

import com.ims.database.DBUsers;
import com.ims.model.objects.UserObject;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.Date;
import java.sql.Timestamp;
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
        
        ArrayList<HashMap<DBUsers.Column, Object>> users = DBUsers.get(
            DBUsers.Column.EMAIL,
            email
        );
        
        if (users.size() != 1) {
            validProperty.set(false);
            return;
        }
        
        HashMap<DBUsers.Column, Object> user = users.getFirst();
        boolean isCorrectPassword = Utils.checkPassword(
            password,
            user.get(DBUsers.Column.PASSWORD).toString()
        );
        
        if (isCorrectPassword) {
            SceneManager.setScene("base");
            validProperty.set(true);
            
            int id = (int) user.get(DBUsers.Column.ID);
            
            UserSessionModel.currentUser.set(
                getUser(id)
            );
        } else {
            validProperty.set(false);
        }
    }
    
    public static UserObject getUser(int id) {
        HashMap<DBUsers.Column, Object> row = DBUsers.getOne(DBUsers.Column.ID, id);
        if (row != null) {
            UserObject userObject = new UserObject(
                id,
                (String) row.get(DBUsers.Column.EMAIL),
                (String) row.get(
                    DBUsers.Column.PASSWORD
                ),
                (Date) row.get(
                    DBUsers.Column.JOINED_DATE
                ),
                (Timestamp) row.get(
                    DBUsers.Column.LAST_ACTIVITY_DATE
                ),
                (Integer) row.get(
                    DBUsers.Column.ROLE_ID
                ),
                (boolean) row.get(
                    DBUsers.Column.IS_DISABLED
                ),
                (boolean) row.get(
                    DBUsers.Column.IS_OWNER
                )
            );
            
            return userObject;
        }
        
        return null;
    }
}
