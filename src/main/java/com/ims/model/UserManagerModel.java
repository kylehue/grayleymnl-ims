package com.ims.model;

import com.ims.Config;
import com.ims.database.DBRoles;
import com.ims.database.DBUsers;
import com.ims.model.objects.CategoryObject;
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
                loadRole(DBRoles.add(name));
                
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
                
                HashMap<DBRoles.Column, Object> newRole = DBRoles.update(
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
                    loadRole(newRole);
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
     * @param id The id of the role to remove.
     */
    public static void removeRole(int id) {
        if (!UserSessionModel.currentUserIsOwner()) {
            System.out.println("The user has insufficient permissions.");
            return;
        }
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyRole.set(true);
                
                DBRoles.remove(id);
                
                roleMap.remove(id);
                
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
    
    public static RoleObject loadAndGetRole(int id) {
        RoleObject roleObject = roleMap.get(id);
        if (roleObject != null) {
            return roleObject;
        }
        
        Task<RoleObject> task = new Task<>() {
            @Override
            protected RoleObject call() throws Exception {
                isBusyRole.set(true);
                
                HashMap<DBRoles.Column, Object> row = DBRoles.getOne(
                    DBRoles.Column.ID,
                    id
                );
                
                if (row != null) {
                    return loadRole(row);
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
        HashMap<DBRoles.Column, Object> role,
        ObservableMap<Integer, RoleObject> map
    ) {
        int id = (Integer) role.get(DBRoles.Column.ID);
        String name = (String) role.get(DBRoles.Column.NAME);
        boolean allowAddCategory = (boolean) role.get(
            DBRoles.Column.ALLOW_ADD_CATEGORY
        );
        boolean allowDeleteCategory = (boolean) role.get(
            DBRoles.Column.ALLOW_DELETE_CATEGORY
        );
        boolean allowEditCategory = (boolean) role.get(
            DBRoles.Column.ALLOW_EDIT_CATEGORY
        );
        boolean allowAddProduct = (boolean) role.get(
            DBRoles.Column.ALLOW_ADD_PRODUCT
        );
        boolean allowDeleteProduct = (boolean) role.get(
            DBRoles.Column.ALLOW_DELETE_PRODUCT
        );
        boolean allowEditProduct = (boolean) role.get(
            DBRoles.Column.ALLOW_EDIT_PRODUCT
        );
        Timestamp lastModified = (Timestamp) role.get(
            DBRoles.Column.LAST_MODIFIED
        );
        
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
                lastModified
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
    
    private static RoleObject loadRole(HashMap<DBRoles.Column, Object> role) {
        return loadRoleToMap(role, roleMap);
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
                ArrayList<HashMap<DBRoles.Column, Object>> roleRows = DBRoles.getInRange(
                    map.size(),
                    limit
                );
                
                for (HashMap<DBRoles.Column, Object> row : roleRows) {
                    loadRoleToMap(row, map);
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
                
                HashMap<DBUsers.Column, Object> user = DBUsers.update(
                    id,
                    password,
                    roleID,
                    isDisabled,
                    isOwner
                );
                
                // Update in list if it exists
                UserObject userObject = userMap.get(id);
                if (userObject != null) {
                    loadUser(user);
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
                ArrayList<HashMap<DBUsers.Column, Object>> result = DBUsers.search(
                    searchPattern
                );
                
                for (HashMap<DBUsers.Column, Object> row : result) {
                    loadUser(row);
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
        HashMap<DBUsers.Column, Object> user,
        ObservableMap<Integer, UserObject> map
    ) {
        int id = (Integer) user.get(DBUsers.Column.ID);
        String email = (String) user.get(DBUsers.Column.EMAIL);
        String password = (String) user.get(
            DBUsers.Column.PASSWORD
        );
        Date joinedDate = (Date) user.get(
            DBUsers.Column.JOINED_DATE
        );
        Timestamp lastActivityDate = (Timestamp) user.get(
            DBUsers.Column.LAST_ACTIVITY_DATE
        );
        int roleID = (Integer) user.get(
            DBUsers.Column.ROLE_ID
        );
        boolean isDisabled = (boolean) user.get(
            DBUsers.Column.IS_DISABLED
        );
        boolean isOwner = (boolean) user.get(
            DBUsers.Column.IS_OWNER
        );
        
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
                isOwner
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
    
    private static UserObject loadUser(HashMap<DBUsers.Column, Object> user) {
        return loadUserToMap(user, userMap);
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
                ArrayList<HashMap<DBUsers.Column, Object>> userRows = DBUsers.getInRange(
                    userMap.size(),
                    limit
                );
                
                for (HashMap<DBUsers.Column, Object> row : userRows) {
                    loadUser(row);
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
