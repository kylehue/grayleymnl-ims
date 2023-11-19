package com.ims.model;

import com.ims.database.DBRoles;
import com.ims.database.DBUsers;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;

public abstract class UserSessionModel {
    public static ObjectProperty<User> currentUser = new SimpleObjectProperty<>(null);
    
    public static void clear() {
        currentUser.set(null);
    }
    
    public static Integer getCurrentUserID() {
        if (currentUser.get() == null) return null;
        return currentUser.get().getID();
    }
    
    public static String getCurrentUserEmail() {
        if (currentUser.get() == null) return null;
        return currentUser.get().getEmail();
    }
    
    public static HashMap<DBRoles.Column, Object> getCurrentUserRole() {
        if (currentUser.get() == null) return null;
        HashMap<DBUsers.Column, Object> user = DBUsers.getOne(
            DBUsers.Column.ID,
            currentUser.get().getID()
        );
        if (user == null) return null;
        return DBRoles.getOne(
            DBRoles.Column.ID,
            user.get(DBUsers.Column.ROLE_ID)
        );
    }
    
    public static class User {
        private int id;
        private String email;
        public User(int id, String email) {
            this.id = id;
            this.email = email;
        }
        
        public int getID() {
            return id;
        }
        
        public String getEmail() {
            return email;
        }
    }
}
