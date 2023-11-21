package com.ims.model;

import com.ims.database.DBRoles;
import com.ims.database.DBUsers;
import com.ims.model.objects.RoleObject;
import com.ims.model.objects.UserObject;
import javafx.application.Platform;
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
                HashMap<DBRoles.Column, Object> newRole = DBRoles.add(name);
                int newID = (Integer) newRole.get(DBRoles.Column.ID);
                String newName = (String) newRole.get(
                    DBRoles.Column.NAME
                );
                boolean newAllowAddCategory = (boolean) newRole.get(
                    DBRoles.Column.ALLOW_ADD_CATEGORY
                );
                boolean newAllowDeleteCategory = (boolean) newRole.get(
                    DBRoles.Column.ALLOW_DELETE_CATEGORY
                );
                boolean newAllowEditCategory = (boolean) newRole.get(
                    DBRoles.Column.ALLOW_EDIT_CATEGORY
                );
                boolean newAllowAddProduct = (boolean) newRole.get(
                    DBRoles.Column.ALLOW_ADD_PRODUCT
                );
                boolean newAllowDeleteProduct = (boolean) newRole.get(
                    DBRoles.Column.ALLOW_DELETE_PRODUCT
                );
                boolean newAllowEditProduct = (boolean) newRole.get(
                    DBRoles.Column.ALLOW_EDIT_PRODUCT
                );
                Timestamp newLastModified = (Timestamp) newRole.get(
                    DBRoles.Column.LAST_MODIFIED
                );
                
                roleMap.put(newID, new RoleObject(
                    newID,
                    newName,
                    newAllowAddCategory,
                    newAllowDeleteCategory,
                    newAllowEditCategory,
                    newAllowAddProduct,
                    newAllowDeleteProduct,
                    newAllowEditProduct,
                    newLastModified
                ));
                
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
                
                // Update in database
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
                    String newName = (String) newRole.get(
                        DBRoles.Column.NAME
                    );
                    boolean newAllowAddCategory = (boolean) newRole.get(
                        DBRoles.Column.ALLOW_ADD_CATEGORY
                    );
                    boolean newAllowDeleteCategory = (boolean) newRole.get(
                        DBRoles.Column.ALLOW_DELETE_CATEGORY
                    );
                    boolean newAllowEditCategory = (boolean) newRole.get(
                        DBRoles.Column.ALLOW_EDIT_CATEGORY
                    );
                    boolean newAllowAddProduct = (boolean) newRole.get(
                        DBRoles.Column.ALLOW_ADD_PRODUCT
                    );
                    boolean newAllowDeleteProduct = (boolean) newRole.get(
                        DBRoles.Column.ALLOW_DELETE_PRODUCT
                    );
                    boolean newAllowEditProduct = (boolean) newRole.get(
                        DBRoles.Column.ALLOW_EDIT_PRODUCT
                    );
                    Timestamp newLastModified = (Timestamp) newRole.get(
                        DBRoles.Column.LAST_MODIFIED
                    );
                    
                    // remove old object just for the sake of triggering the listeners
                    roleMap.put(id, null);
                    
                    // bring back the old role object
                    roleObject.setName(newName);
                    roleObject.setAllowAddCategory(newAllowAddCategory);
                    roleObject.setAllowDeleteCategory(newAllowDeleteCategory);
                    roleObject.setAllowEditCategory(newAllowEditCategory);
                    roleObject.setAllowAddProduct(newAllowAddProduct);
                    roleObject.setAllowDeleteProduct(newAllowDeleteProduct);
                    roleObject.setAllowEditProduct(newAllowEditProduct);
                    roleObject.setLastModified(newLastModified);
                    roleMap.put(id, roleObject);
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
                    RoleObject roleObject = new RoleObject(
                        id,
                        (String) row.get(DBRoles.Column.NAME),
                        (boolean) row.get(
                            DBRoles.Column.ALLOW_ADD_CATEGORY
                        ),
                        (boolean) row.get(
                            DBRoles.Column.ALLOW_DELETE_CATEGORY
                        ),
                        (boolean) row.get(
                            DBRoles.Column.ALLOW_EDIT_CATEGORY
                        ),
                        (boolean) row.get(
                            DBRoles.Column.ALLOW_ADD_PRODUCT
                        ),
                        (boolean) row.get(
                            DBRoles.Column.ALLOW_DELETE_PRODUCT
                        ),
                        (boolean) row.get(
                            DBRoles.Column.ALLOW_EDIT_PRODUCT
                        ),
                        (Timestamp) row.get(DBRoles.Column.LAST_MODIFIED)
                    );
                    
                    roleMap.put(id, roleObject);
                    
                    return roleObject;
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
    
    /**
     * Load more roles from the database.
     *
     * @param limit The limit of the rows to retrieve.
     */
    public static void loadRoles(int limit) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyRole.set(true);
                ArrayList<HashMap<DBRoles.Column, Object>> roleRows = DBRoles.getInRange(
                    roleMap.size(),
                    limit
                );
                
                for (HashMap<DBRoles.Column, Object> row : roleRows) {
                    int id = (Integer) row.get(DBRoles.Column.ID);
                    // Skip if already added (this shouldn't happen but just to be sure)
                    RoleObject role = roleMap.get(id);
                    
                    // Add in list
                    String name = (String) row.get(DBRoles.Column.NAME);
                    boolean allowAddCategory = (boolean) row.get(
                        DBRoles.Column.ALLOW_ADD_CATEGORY
                    );
                    boolean allowDeleteCategory = (boolean) row.get(
                        DBRoles.Column.ALLOW_DELETE_CATEGORY
                    );
                    boolean allowEditCategory = (boolean) row.get(
                        DBRoles.Column.ALLOW_EDIT_CATEGORY
                    );
                    boolean allowAddProduct = (boolean) row.get(
                        DBRoles.Column.ALLOW_ADD_PRODUCT
                    );
                    boolean allowDeleteProduct = (boolean) row.get(
                        DBRoles.Column.ALLOW_DELETE_PRODUCT
                    );
                    boolean allowEditProduct = (boolean) row.get(
                        DBRoles.Column.ALLOW_EDIT_PRODUCT
                    );
                    Timestamp lastModified = (Timestamp) row.get(
                        DBRoles.Column.LAST_MODIFIED
                    );
                    
                    if (role == null) {
                        role = new RoleObject(
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
                    }
                    
                    roleMap.put(id, null);
                    role.setName(name);
                    role.setAllowAddProduct(allowAddProduct);
                    role.setAllowDeleteProduct(allowDeleteProduct);
                    role.setAllowEditProduct(allowEditProduct);
                    role.setAllowAddCategory(allowAddCategory);
                    role.setAllowDeleteCategory(allowDeleteCategory);
                    role.setAllowEditCategory(allowEditCategory);
                    role.setLastModified(lastModified);
                    roleMap.put(id, role);
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
                    String password = (String) user.get(
                        DBUsers.Column.PASSWORD
                    );
                    Timestamp lastActivityDate = (Timestamp) user.get(
                        DBUsers.Column.LAST_ACTIVITY_DATE
                    );
                    int roleID = (Integer) user.get(
                        DBUsers.Column.ROLE_ID
                    );
                    boolean _isDisabled = (boolean) user.get(
                        DBUsers.Column.IS_DISABLED
                    );
                    boolean _isOwner = (boolean) user.get(
                        DBUsers.Column.IS_OWNER
                    );
                    
                    // remove old object just for the sake of triggering the listeners
                    userMap.put(id, null);
                    
                    // bring back the old user object
                    userObject.setPassword(password);
                    userObject.setLastActivityDate(lastActivityDate);
                    userObject.setRoleID(roleID);
                    userObject.setDisabled(_isDisabled);
                    userObject.setOwner(_isOwner);
                    userMap.put(id, userObject);
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
                    int id = (Integer) row.get(DBUsers.Column.ID);
                    UserObject user = userMap.get(id);
                    
                    // Add in list
                    String email = (String) row.get(DBUsers.Column.EMAIL);
                    String password = (String) row.get(
                        DBUsers.Column.PASSWORD
                    );
                    Date joinedDate = (Date) row.get(
                        DBUsers.Column.JOINED_DATE
                    );
                    Timestamp lastActivityDate = (Timestamp) row.get(
                        DBUsers.Column.LAST_ACTIVITY_DATE
                    );
                    int roleID = (Integer) row.get(
                        DBUsers.Column.ROLE_ID
                    );
                    boolean isDisabled = (boolean) row.get(
                        DBUsers.Column.IS_DISABLED
                    );
                    boolean isOwner = (boolean) row.get(
                        DBUsers.Column.IS_OWNER
                    );
                    
                    if (user == null) {
                        user = new UserObject(
                            id,
                            email,
                            password,
                            joinedDate,
                            lastActivityDate,
                            roleID,
                            isDisabled,
                            isOwner
                        );
                    }
                    
                    userMap.put(id, null);
                    user.setPassword(password);
                    user.setOwner(isOwner);
                    user.setRoleID(roleID);
                    user.setDisabled(isDisabled);
                    user.setLastActivityDate(lastActivityDate);
                    userMap.put(id, user);
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
