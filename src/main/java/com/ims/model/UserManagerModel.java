package com.ims.model;

import com.ims.Config;
import com.ims.database.DBRoles;
import com.ims.database.DBUsers;
import com.ims.model.objects.RoleObject;
import com.ims.model.objects.UserObject;
import com.ims.utils.AsyncCaller;
import com.ims.utils.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.sql.Date;
import java.sql.Timestamp;
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
        
        new AsyncCaller<Void>(task -> {
            isBusyRole.set(true);
            loadRole(DBRoles.add(name), true);
            
            return null;
        }, Utils.executor).onSucceeded(e -> {
            isBusyRole.set(false);
        }).execute();
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
        
        new AsyncCaller<Void>(task -> {
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
        }, Utils.executor).onSucceeded(e -> {
            isBusyRole.set(false);
        }).execute();
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
        
        new AsyncCaller<>(task -> {
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
        }, Utils.executor).onSucceeded(e -> {
            isBusyRole.set(false);
        }).execute();
    }
    
    public static void searchRoles(String searchText) {
        new AsyncCaller<>(task -> {
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
        }, Utils.executor).execute();
    }
    
    public static AsyncCaller<RoleObject> loadAndGetRole(int id) {
        return new AsyncCaller<>(task -> {
            RoleObject roleObject = roleMap.get(id);
            if (roleObject != null) {
                return roleObject;
            }
            
            isBusyRole.set(true);
            
            DBRoles.RoleData row = DBRoles.getOne(
                DBRoles.Column.ID,
                id
            );
            
            if (row != null) {
                return loadRole(row, false);
            }
            
            isBusyRole.set(false);
            
            return null;
        }, Utils.executor);
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
        new AsyncCaller<Void>(task -> {
            isBusyRole.set(true);
            DBRoles.RoleListData roleRows = DBRoles.getBulk(
                map.keySet(),
                limit
            );
            
            for (DBRoles.RoleData row : roleRows) {
                loadRoleToMap(row, map, false);
            }
            return null;
        }, Utils.executor).onSucceeded(e -> {
            isBusyRole.set(false);
        }).execute();
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
        
        new AsyncCaller<Void>(task -> {
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
        }, Utils.executor).onSucceeded(e -> {
            isBusyUser.set(false);
        }).execute();
    }
    
    public static void searchUsers(String searchText) {
        new AsyncCaller<Void>(task -> {
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
        }, Utils.executor).execute();
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
    
    public static AsyncCaller<UserObject> loadAndGetUser(int id) {
        return new AsyncCaller<>(task -> {
            UserObject userObject = userMap.get(id);
            if (userObject != null) {
                return userObject;
            }
            
            isBusyUser.set(true);
            
            DBUsers.UserData userData = DBUsers.getOne(DBUsers.Column.ID, id);
            if (userData != null) {
                return loadUser(userData, false);
            }
            
            isBusyUser.set(false);
            
            return null;
        }, Utils.executor);
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
        new AsyncCaller<Void>(task -> {
            isBusyUser.set(true);
            DBUsers.UserListData userRows = DBUsers.getBulk(
                userMap.keySet(),
                limit
            );
            
            for (DBUsers.UserData row : userRows) {
                loadUser(row, false);
            }
            return null;
        }, Utils.executor).onSucceeded(e -> {
            isBusyUser.set(false);
            
        }).execute();
    }
}
