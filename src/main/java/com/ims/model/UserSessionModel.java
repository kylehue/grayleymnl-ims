package com.ims.model;

import com.ims.database.DBRoles;
import com.ims.database.DBUsers;
import com.ims.model.objects.UserObject;
import com.ims.utils.SceneManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;

public abstract class UserSessionModel {
    public static ObjectProperty<UserObject> currentUser =
        new SimpleObjectProperty<>(null);
    
    public static void logout() {
        currentUser.set(null);
        SceneManager.setScene("login");
    }
    
    public static Integer getCurrentUserID() {
        if (currentUser.get() == null) return null;
        return currentUser.get().getID();
    }
    
    public static String getCurrentUserEmail() {
        if (currentUser.get() == null) return null;
        return currentUser.get().getEmail();
    }
    
    public static String getCurrentUserPassword() {
        if (currentUser.get() == null) return null;
        HashMap<DBUsers.Column, Object> user = DBUsers.getOne(
            DBUsers.Column.ID,
            currentUser.get().getID()
        );
        if (user == null) return null;
        return user.get(DBUsers.Column.PASSWORD).toString();
    }
    
    public static HashMap<DBRoles.Column, Object> getCurrentUserRole() {
        if (currentUser.get() == null) return null;
        return getUserRole(currentUser.get().getID());
    }
    
    public static boolean currentUserIsOwner() {
        if (currentUser.get() == null) return false;
        return currentUser.get().isOwner();
    }
    
    public static boolean currentUserIsAllowAddCategory() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        HashMap<DBRoles.Column, Object> role = getCurrentUserRole();
        if (role == null) return false;
        return role.get(DBRoles.Column.ALLOW_ADD_CATEGORY).equals(true);
    }
    
    public static boolean currentUserIsAllowDeleteCategory() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        HashMap<DBRoles.Column, Object> role = getCurrentUserRole();
        if (role == null) return false;
        return role.get(DBRoles.Column.ALLOW_DELETE_CATEGORY).equals(true);
    }
    
    public static boolean currentUserIsAllowEditCategory() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        HashMap<DBRoles.Column, Object> role = getCurrentUserRole();
        if (role == null) return false;
        return role.get(DBRoles.Column.ALLOW_EDIT_CATEGORY).equals(true);
    }
    
    public static boolean currentUserIsAllowAddProduct() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        HashMap<DBRoles.Column, Object> role = getCurrentUserRole();
        if (role == null) return false;
        return role.get(DBRoles.Column.ALLOW_ADD_PRODUCT).equals(true);
    }
    
    public static boolean currentUserIsAllowDeleteProduct() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        HashMap<DBRoles.Column, Object> role = getCurrentUserRole();
        if (role == null) return false;
        return role.get(DBRoles.Column.ALLOW_DELETE_PRODUCT).equals(true);
    }
    
    public static boolean currentUserIsAllowEditProduct() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        HashMap<DBRoles.Column, Object> role = getCurrentUserRole();
        if (role == null) return false;
        return role.get(DBRoles.Column.ALLOW_EDIT_PRODUCT).equals(true);
    }
    
    public static HashMap<DBRoles.Column, Object> getUserRole(int userID) {
        HashMap<DBUsers.Column, Object> user = DBUsers.getOne(
            DBUsers.Column.ID,
            userID
        );
        if (user == null) return null;
        return DBRoles.getOne(
            DBRoles.Column.ID,
            user.get(DBUsers.Column.ROLE_ID)
        );
    }
    
    public static HashMap<DBUsers.Column, Object> updatePassword(
        String password
    ) {
        return DBUsers.update(
            currentUser.get().getID(),
            password,
            null,
            null,
            null
        );
    }
    
    public static void deleteAccount() {
        if (UserSessionModel.currentUserIsOwner()) {
            return;
        }
        
        DBUsers.remove(currentUser.get().getID());
        logout();
    }
}
