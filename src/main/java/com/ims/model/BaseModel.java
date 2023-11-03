package com.ims.model;

import com.ims.database.DBCategories;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.ProductObject;
import com.ims.utils.Utils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseModel {
    //////////////////////////////////////////////////////////////////////
    // ----------------------- DASHBOARD PAGE ------------------------- //
    //////////////////////////////////////////////////////////////////////
    
    
    //////////////////////////////////////////////////////////////////////
    // ------------------------ PRODUCT PAGE -------------------------- //
    //////////////////////////////////////////////////////////////////////
    public static final ObservableMap<Integer, ProductObject>
        productMap = FXCollections.observableHashMap();
    
    public static ProductObject getProductById(int id) {
        return productMap.get(id);
    }
    
    
    //////////////////////////////////////////////////////////////////////
    // ------------------------ CATEGORY PAGE ------------------------- //
    //////////////////////////////////////////////////////////////////////
    
    public static final ObservableMap<Integer, CategoryObject>
        categoryMap = FXCollections.observableHashMap();
    public static final BooleanProperty
        isBusyCategory = new SimpleBooleanProperty(false);
    
    /**
     * Adds category in the database.
     *
     * @param name The name of the category to add.
     */
    public static void addCategory(String name) {
        if (name.isEmpty()) return;
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyCategory.set(true);
                HashMap<DBCategories.Column, Object> newCategory = DBCategories.add(name);
                int newID = (Integer) newCategory.get(DBCategories.Column.ID);
                String newName = (String) newCategory.get(DBCategories.Column.NAME);
                Timestamp newLastModified = (Timestamp) newCategory.get(DBCategories.Column.LAST_MODIFIED);
                
                categoryMap.put(newID, new CategoryObject(
                    newID,
                    newName,
                    newLastModified
                ));
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyCategory.set(false);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    /**
     * Updates a category in the database.
     *
     * @param id   The id of the category to update.
     * @param name The name of the category to update.
     */
    public static void updateCategory(int id, String name) {
        if (name.isEmpty()) return;
        boolean isUnmodified = name.equals(
            Objects.requireNonNull(categoryMap.get(id)).getName()
        );
        if (isUnmodified) return;
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyCategory.set(true);
                
                // Update in database
                HashMap<DBCategories.Column, Object> newCategory = DBCategories.update(id, name);
                
                // Update in list if it exists
                CategoryObject categoryObject = categoryMap.get(id);
                if (categoryObject != null) {
                    String newName = (String) newCategory.get(DBCategories.Column.NAME);
                    Timestamp newLastModified = (Timestamp) newCategory.get(DBCategories.Column.LAST_MODIFIED);
                    categoryMap.put(id, new CategoryObject(
                        id,
                        newName,
                        newLastModified
                    ));
                }
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyCategory.set(false);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    /**
     * Remove a category in the database.
     *
     * @param id The id of the category to remove.
     */
    public static void removeCategory(int id) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyCategory.set(true);
                
                DBCategories.remove(id);
                categoryMap.remove(id);
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyCategory.set(false);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }

    /**
     * Load more categories from the database.
     *
     * @param limit The limit of the rows to retrieve.
     */
    public static void loadCategories(int limit) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyCategory.set(true);
                ArrayList<HashMap<DBCategories.Column, Object>> categoryRows = DBCategories.getInRange(
                    categoryMap.size(),
                    limit
                );
                
                for (HashMap<DBCategories.Column, Object> row : categoryRows) {
                    int id = (Integer) row.get(DBCategories.Column.ID);
                    // Skip if already added (this shouldn't happen but just to be sure)
                    CategoryObject category = categoryMap.get(id);
                    if (category != null) {
                        continue;
                    }
                    
                    // Add in list
                    String name = (String) row.get(DBCategories.Column.NAME);
                    Timestamp lastModified = (Timestamp) row.get(DBCategories.Column.LAST_MODIFIED);
                    categoryMap.put(id, new CategoryObject(
                        id,
                        name,
                        lastModified
                    ));
                }
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyCategory.set(false);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
}
