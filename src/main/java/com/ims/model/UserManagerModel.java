package com.ims.model;

import com.ims.database.DBRoles;
import com.ims.model.objects.RoleObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class UserManagerModel {
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
        if (name.isEmpty()) return;
        
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
                    roleMap.put(id, new RoleObject(
                        id,
                        newName,
                        newAllowAddCategory,
                        newAllowDeleteCategory,
                        newAllowEditCategory,
                        newAllowAddProduct,
                        newAllowDeleteProduct,
                        newAllowEditProduct,
                        newLastModified
                    ));
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
                    if (role != null) {
                        continue;
                    }
                    
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
                    roleMap.put(id, new RoleObject(
                        id,
                        name,
                        allowAddCategory,
                        allowDeleteCategory,
                        allowEditCategory,
                        allowAddProduct,
                        allowDeleteProduct,
                        allowEditProduct,
                        lastModified
                    ));
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
}
