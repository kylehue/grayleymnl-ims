package com.ims.model;

import com.ims.database.DBRoles;
import com.ims.database.DBUsers;
import com.ims.model.objects.UserObject;
import com.ims.utils.SceneManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class UserSessionModel {
    public static ExecutorService executor = Executors.newFixedThreadPool(4);
    
    public static ObjectProperty<UserObject> currentUser =
        new SimpleObjectProperty<>(null);
    
    public static void logout() {
        currentUser.set(null);
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
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                DBUsers.UserData userData = DBUsers.getOne(
                    DBUsers.Column.ID,
                    currentUser.get().getID()
                );
                if (userData == null) return null;
                return userData.getPassword();
            }
        };
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        executor.submit(task);
        
        String password = null;
        try {
            password = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return password;
    }
    
    public static DBRoles.RoleData getCurrentUserRole() {
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
        DBRoles.RoleData roleData = getCurrentUserRole();
        if (roleData == null) return false;
        return roleData.isAllowAddCategory().equals(true);
    }
    
    public static boolean currentUserIsAllowDeleteCategory() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        DBRoles.RoleData roleData = getCurrentUserRole();
        if (roleData == null) return false;
        return roleData.isAllowDeleteCategory().equals(true);
    }
    
    public static boolean currentUserIsAllowEditCategory() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        DBRoles.RoleData roleData = getCurrentUserRole();
        if (roleData == null) return false;
        return roleData.isAllowEditCategory().equals(true);
    }
    
    public static boolean currentUserIsAllowAddProduct() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        DBRoles.RoleData roleData = getCurrentUserRole();
        if (roleData == null) return false;
        return roleData.isAllowAddProduct().equals(true);
    }
    
    public static boolean currentUserIsAllowDeleteProduct() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        DBRoles.RoleData roleData = getCurrentUserRole();
        if (roleData == null) return false;
        return roleData.isAllowDeleteProduct().equals(true);
    }
    
    public static boolean currentUserIsAllowEditProduct() {
        if (currentUser.get() == null) return false;
        if (currentUserIsOwner()) return true;
        DBRoles.RoleData roleData = getCurrentUserRole();
        if (roleData == null) return false;
        return roleData.isAllowEditProduct().equals(true);
    }
    
    public static DBRoles.RoleData getUserRole(int userID) {
        Task<DBRoles.RoleData> task = new Task<>() {
            @Override
            protected DBRoles.RoleData call() throws Exception {
                DBUsers.UserData userData = DBUsers.getOne(
                    DBUsers.Column.ID,
                    userID
                );
                if (userData == null) return null;
                
                return DBRoles.getOne(
                    DBRoles.Column.ID,
                    userData.getRoleID()
                );
            }
        };
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        executor.submit(task);
        
        DBRoles.RoleData role = null;
        try {
            role = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return role;
    }
    
    public static DBUsers.UserData updatePassword(
        String password
    ) {
        Task<DBUsers.UserData> task = new Task<>() {
            @Override
            protected DBUsers.UserData call() throws Exception {
                return DBUsers.update(
                    currentUser.get().getID(),
                    password,
                    null,
                    null,
                    null
                );
            }
        };
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        executor.submit(task);
        
        DBUsers.UserData user = null;
        try {
            user = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return user;
    }
    
    public static void deleteAccount() {
        if (UserSessionModel.currentUserIsOwner()) {
            return;
        }
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                DBUsers.remove(currentUser.get().getID());
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            logout();
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        executor.submit(task);
    }
}
