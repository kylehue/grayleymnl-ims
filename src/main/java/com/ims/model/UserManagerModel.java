package com.ims.model;

import com.ims.Config;
import com.ims.components.User;
import com.ims.database.DBCategories;
import com.ims.database.DBRoles;
import com.ims.database.DBUsers;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.ProductObject;
import com.ims.model.objects.RoleObject;
import com.ims.model.objects.UserObject;
import com.ims.utils.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class UserManagerModel {
    //////////////////////////////////////////////////////////////////////
    // ---------------------------- ROLES ----------------------------- //
    //////////////////////////////////////////////////////////////////////
    
    public static final ObservableMap<Integer, RoleObject>
        roleMap = FXCollections.observableHashMap();
    public static final BooleanProperty
        isBusyRole = new SimpleBooleanProperty(false);
    
    /**
     * Adds role in the database.
     *
     * @param name The name of the role to add.
     */
    public static void addRole(String name) {
        if (!UserSessionModel.currentUserIsOwner()) {
            System.out.println("The user has insufficient permissions.");
            return;
        }
        if (name.isEmpty()) return;
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyRole.set(true);
                loadRole(DBRoles.add(name), true);
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyRole.set(false);
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    /**
     * Updates a role in the database.
     *
     * @param id   The id of the role to update.
     * @param name The name of the role to update.
     */
    public static void updateRole(
        int id,
        String name,
        Boolean allowAddCategory,
        Boolean allowDeleteCategory,
        Boolean allowEditCategory,
        Boolean allowAddProduct,
        Boolean allowDeleteProduct,
        Boolean allowEditProduct
    ) {
        if (!UserSessionModel.currentUserIsOwner()) {
            System.out.println("The user has insufficient permissions.");
            return;
        }
        if (name != null && name.isEmpty()) return;
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyRole.set(true);
                
                DBRoles.RoleData newRole = DBRoles.update(
                    id,
                    name,
                    allowAddCategory,
                    allowDeleteCategory,
                    allowEditCategory,
                    allowAddProduct,
                    allowDeleteProduct,
                    allowEditProduct
                );
                
                // Update in list if it exists
                RoleObject roleObject = roleMap.get(id);
                if (roleObject != null) {
                    loadRole(newRole, false);
                }
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyRole.set(false);
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    /**
     * Remove a role in the database.
     *
     * @param roleID The id of the role to remove.
     */
    public static void removeRole(int roleID) {
        if (!UserSessionModel.currentUserIsOwner()) {
            System.out.println("The user has insufficient permissions.");
            return;
        }
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyRole.set(true);
                
                DBRoles.remove(roleID);
                roleMap.remove(roleID);
                
                // Nullify role of users that has the removed role
                for (int userID : userMap.keySet()) {
                    UserObject userObject = userMap.get(userID);
                    if (userObject.getRoleID() == null) continue;
                    if (userObject.getRoleID() != roleID) continue;
                    userObject.setRoleID(null);
                }
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyRole.set(false);
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    public static void searchRoles(String searchText) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                roleMap.clear();
                if (searchText.isEmpty()) {
                    loadRoles(Config.roleLoadLimit);
                    return null;
                }
                
                String searchPattern = Utils.textToSearchPattern(searchText);
                DBRoles.RoleListData result = DBRoles.search(
                    searchPattern
                );
                
                for (DBRoles.RoleData row : result) {
                    loadRole(row, false);
                }
                
                return null;
            }
        };
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    public static RoleObject loadAndGetRole(int id) {
        RoleObject roleObject = roleMap.get(id);
        if (roleObject != null) {
            return roleObject;
        }
        
        Task<RoleObject> task = new Task<>() {
            @Override
            protected RoleObject call() throws Exception {
                isBusyRole.set(true);
                
                DBRoles.RoleData row = DBRoles.getOne(
                    DBRoles.Column.ID,
                    id
                );
                
                if (row != null) {
                    return loadRole(row, false);
                }
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyRole.set(false);
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
        
        try {
            roleObject = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return roleObject;
    }
    
    public static RoleObject loadRoleToMap(
        DBRoles.RoleData roleData,
        ObservableMap<Integer, RoleObject> map,
        boolean isNew
    ) {
        int id = roleData.getID();
        String name = roleData.getName();
        Boolean allowAddCategory = roleData.isAllowAddCategory();
        Boolean allowDeleteCategory = roleData.isAllowDeleteCategory();
        Boolean allowEditCategory = roleData.isAllowEditCategory();
        Boolean allowAddProduct = roleData.isAllowAddProduct();
        Boolean allowDeleteProduct = roleData.isAllowDeleteProduct();
        Boolean allowEditProduct = roleData.isAllowEditProduct();
        Timestamp lastModified = roleData.getLastModified();
        
        RoleObject roleObject = map.get(id);
        if (roleObject == null) {
            roleObject = new RoleObject(
                id,
                name,
                allowAddCategory,
                allowDeleteCategory,
                allowEditCategory,
                allowAddProduct,
                allowDeleteProduct,
                allowEditProduct,
                lastModified,
                isNew
            );
            map.put(id, roleObject);
        } else {
            roleObject.setName(name);
            roleObject.setAllowAddProduct(allowAddProduct);
            roleObject.setAllowDeleteProduct(allowDeleteProduct);
            roleObject.setAllowEditProduct(allowEditProduct);
            roleObject.setAllowAddCategory(allowAddCategory);
            roleObject.setAllowDeleteCategory(allowDeleteCategory);
            roleObject.setAllowEditCategory(allowEditCategory);
            roleObject.setLastModified(lastModified);
        }
        
        return roleObject;
    }
    
    private static RoleObject loadRole(DBRoles.RoleData role, boolean isNew) {
        return loadRoleToMap(role, roleMap, isNew);
    }
    
    /**
     * Load more roles from the database.
     *
     * @param limit The limit of the rows to retrieve.
     */
    public static void loadRolesToMap(
        int limit,
        ObservableMap<Integer, RoleObject> map
    ) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyRole.set(true);
                DBRoles.RoleListData roleRows = DBRoles.getInRange(
                    map.size(),
                    limit
                );
                
                for (DBRoles.RoleData row : roleRows) {
                    loadRoleToMap(row, map, false);
                }
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyRole.set(false);
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    public static void loadRoles(int limit) {
        loadRolesToMap(limit, roleMap);
    }
    
    //////////////////////////////////////////////////////////////////////
    // ---------------------------- USERS ----------------------------- //
    //////////////////////////////////////////////////////////////////////
    
    public static final ObservableMap<Integer, UserObject>
        userMap = FXCollections.observableHashMap();
    public static final BooleanProperty
        isBusyUser = new SimpleBooleanProperty(false);
    
    /**
     * Update a user.
     *
     * @param id         The id of the user to disable.
     * @param password   The new password of the user.
     * @param roleID     The new role ID of the user.
     * @param isDisabled Should the user be disabled or not?
     */
    public static void updateUser(
        int id,
        String password,
        Integer roleID,
        Boolean isDisabled,
        Boolean isOwner
    ) {
        if (!UserSessionModel.currentUserIsOwner()) {
            System.out.println("The user has insufficient permissions.");
            return;
        }
        if (
            isDisabled != null &&
                isDisabled &&
                id == UserSessionModel.getCurrentUserID() &&
                UserSessionModel.currentUserIsOwner()
        ) {
            System.out.println("You can't disable a user who is an owner.");
            return;
        }
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyUser.set(true);
                
                DBUsers.UserData user = DBUsers.update(
                    id,
                    password,
                    roleID,
                    isDisabled,
                    isOwner
                );
                
                // Update in list if it exists
                UserObject userObject = userMap.get(id);
                if (userObject != null) {
                    loadUser(user, false);
                }
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyUser.set(false);
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    public static void searchUsers(String searchText) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                userMap.clear();
                if (searchText.isEmpty()) {
                    loadUsers(Config.categoryLoadLimit);
                    return null;
                }
                
                String searchPattern = Utils.textToSearchPattern(searchText);
                DBUsers.UserListData result = DBUsers.search(
                    searchPattern
                );
                
                for (DBUsers.UserData row : result) {
                    loadUser(row, false);
                }
                
                return null;
            }
        };
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    public static UserObject loadUserToMap(
        DBUsers.UserData user,
        ObservableMap<Integer, UserObject> map,
        boolean isNew
    ) {
        int id = user.getID();
        String email = user.getEmail();
        String password = user.getPassword();
        Date joinedDate = user.getJoinedDate();
        Timestamp lastActivityDate = user.getLastActivityDate();
        Integer roleID = user.getRoleID();
        Boolean isDisabled = user.isDisabled();
        Boolean isOwner = user.isOwner();
        
        UserObject userObject = map.get(id);
        if (userObject == null) {
            userObject = new UserObject(
                id,
                email,
                password,
                joinedDate,
                lastActivityDate,
                roleID,
                isDisabled,
                isOwner,
                isNew
            );
            map.put(id, userObject);
        } else {
            userObject.setPassword(password);
            userObject.setOwner(isOwner);
            userObject.setRoleID(roleID);
            userObject.setDisabled(isDisabled);
            userObject.setLastActivityDate(lastActivityDate);
        }
        
        return userObject;
    }
    
    public static UserObject loadAndGetUser(int id) {
        UserObject userObject = userMap.get(id);
        if (userObject != null) {
            return userObject;
        }
        
        Task<UserObject> task = new Task<>() {
            @Override
            protected UserObject call() {
                isBusyUser.set(true);
                
                DBUsers.UserData userData = DBUsers.getOne(DBUsers.Column.ID, id);
                if (userData != null) {
                    return loadUser(userData, false);
                }
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyUser.set(false);
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
        
        try {
            userObject = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return userObject;
    }
    
    private static UserObject loadUser(DBUsers.UserData user, boolean isNew) {
        return loadUserToMap(user, userMap, isNew);
    }
    
    /**
     * Load more users from the database.
     *
     * @param limit The limit of the rows to retrieve.
     */
    public static void loadUsers(int limit) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyUser.set(true);
                DBUsers.UserListData userRows = DBUsers.getInRange(
                    userMap.size(),
                    limit
                );
                
                for (DBUsers.UserData row : userRows) {
                    loadUser(row, false);
                }
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyUser.set(false);
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
}
