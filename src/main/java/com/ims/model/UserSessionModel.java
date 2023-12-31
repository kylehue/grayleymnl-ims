package com.ims.model;

import com.ims.database.DBUsers;
import com.ims.model.objects.RoleObject;
import com.ims.model.objects.UserObject;
import com.ims.utils.AsyncCaller;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class UserSessionModel {
    public static ObjectProperty<UserObject> currentUser =
        new SimpleObjectProperty<>(null);
    public static ObjectProperty<RoleObject> currentUserRole =
        new SimpleObjectProperty<>(null);
    
    public static void logout() {
        currentUser.set(null);
        currentUserRole.set(null);
        SceneManager.setScene("login");
    }
    
    public static Integer getCurrentUserID() {
        return currentUser.get().getID();
    }
    
    public static String getCurrentUserEmail() {
        if (currentUser.get() == null) return null;
        return currentUser.get().getEmail();
    }
    
    public static String getCurrentUserPassword() {
        if (currentUser.get() == null) return null;
        return currentUser.get().getPassword();
    }
    
    public static RoleObject getCurrentUserRole() {
        if (currentUserRole.get() == null) return null;
        return currentUserRole.get();
    }
    
    public static boolean currentUserIsOwner() {
        if (currentUser.get() == null) return false;
        return currentUser.get().isOwner();
    }
    
    public static boolean currentUserIsAllowAddCategory() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        if (currentUserRole.get() == null) return false;
        return currentUserRole.get().isAllowAddCategory();
    }
    
    public static boolean currentUserIsAllowDeleteCategory() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        if (currentUserRole.get() == null) return false;
        return currentUserRole.get().isAllowDeleteCategory();
    }
    
    public static boolean currentUserIsAllowEditCategory() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        if (currentUserRole.get() == null) return false;
        return currentUserRole.get().isAllowEditCategory();
    }
    
    public static boolean currentUserIsAllowAddProduct() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        if (currentUserRole.get() == null) return false;
        return currentUserRole.get().isAllowAddProduct();
    }
    
    public static boolean currentUserIsAllowDeleteProduct() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        if (currentUserRole.get() == null) return false;
        return currentUserRole.get().isAllowDeleteProduct();
    }
    
    public static boolean currentUserIsAllowEditProduct() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        if (currentUserRole.get() == null) return false;
        return currentUserRole.get().isAllowEditProduct();
    }
    
    public static void updatePassword(
        String unhashedPassword
    ) {
        new AsyncCaller<Void>((task) -> {
            String hashedPassword = Utils.hashPassword(unhashedPassword);
            DBUsers.update(
                currentUser.get().getID(),
                hashedPassword,
                null,
                null,
                null
            );
            
            currentUser.get().setPassword(hashedPassword);
            
            return null;
        }, Utils.executor).execute();
    }
    
    public static void deleteAccount() {
        if (UserSessionModel.currentUserIsOwner()) {
            System.out.println("Must transfer ownership first before deleting account.");
            return;
        }
        
        new AsyncCaller<Void>((task) -> {
            DBUsers.remove(currentUser.get().getID());
            return null;
        }, Utils.executor).onSucceeded(e -> {
            logout();
        }).execute();
    }
}
