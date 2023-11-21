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
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                HashMap<DBUsers.Column, Object> user = DBUsers.getOne(
                    DBUsers.Column.ID,
                    currentUser.get().getID()
                );
                if (user == null) return null;
                return user.get(DBUsers.Column.PASSWORD).toString();
            }
        };
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
        
        String password = null;
        try {
            password = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return password;
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
        Task<HashMap<DBRoles.Column, Object>> task = new Task<>() {
            @Override
            protected HashMap<DBRoles.Column, Object> call() throws Exception {
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
        };
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
        
        HashMap<DBRoles.Column, Object> role = null;
        try {
            role = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return role;
    }
    
    public static HashMap<DBUsers.Column, Object> updatePassword(
        String password
    ) {
        Task<HashMap<DBUsers.Column, Object>> task = new Task<>() {
            @Override
            protected HashMap<DBUsers.Column, Object> call() throws Exception {
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
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
        
        HashMap<DBUsers.Column, Object> user = null;
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
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
}
