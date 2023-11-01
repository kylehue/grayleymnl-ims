package com.ims.model;

import com.ims.database.DBCategories;
import com.ims.database.DBCategoriesColumn;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.ProductObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class BaseModel {
    public static ObservableMap<Integer, CategoryObject>
        categoriesProperty = FXCollections.observableHashMap();
    public static ObservableMap<Integer, ProductObject>
        productsProperty = FXCollections.observableHashMap();
    
    public static CategoryObject getCategoryById(int id) {
        return categoriesProperty.get(id);
    }
    
    /**
     * Adds category to the database and model's state.
     *
     * @param name The name of the category to add.
     */
    public static void addCategory(String name) {
        if (name.isEmpty()) return;
        
        HashMap<DBCategoriesColumn, Object> newCategory = DBCategories.add(name);
        int newID = (Integer) newCategory.get(DBCategoriesColumn.ID);
        String newName = (String) newCategory.get(DBCategoriesColumn.NAME);
        Timestamp newLastModified = (Timestamp) newCategory.get(DBCategoriesColumn.LAST_MODIFIED);
        
        categoriesProperty.put(newID, new CategoryObject(
            newID,
            newName,
            newLastModified
        ));
    }
    
    /**
     * Updates a category to the database and model's state.
     *
     * @param name The name of the category to update.
     */
    public static void updateCategory(int id, String name) {
        if (name.isEmpty()) return;
        boolean isUnmodified = name.equals(categoriesProperty.get(id).getName());
        if (isUnmodified) return;
        
        HashMap<DBCategoriesColumn, Object> newCategory = DBCategories.update(id, name);
        int newID = (Integer) newCategory.get(DBCategoriesColumn.ID);
        String newName = (String) newCategory.get(DBCategoriesColumn.NAME);
        Timestamp newLastModified = (Timestamp) newCategory.get(DBCategoriesColumn.LAST_MODIFIED);
        
        categoriesProperty.put(newID, new CategoryObject(
            newID,
            newName,
            newLastModified
        ));
    }
    
    public static void removeCategory(int id) {
        DBCategories.remove(id);
        categoriesProperty.remove(id);
    }
    
    public static void loadCategories(int limit) {
        ArrayList<HashMap<DBCategoriesColumn, Object>> categoryRows = DBCategories.getInRange(
            categoriesProperty.size(),
            limit
        );
        
        for (HashMap<DBCategoriesColumn, Object> row : categoryRows) {
            int id = (Integer) row.get(DBCategoriesColumn.ID);
            // Skip if already added
            CategoryObject category = getCategoryById(id);
            if (category != null) {
                continue;
            }
            
            // Add
            String name = (String) row.get(DBCategoriesColumn.NAME);
            Timestamp lastModified = (Timestamp) row.get(DBCategoriesColumn.LAST_MODIFIED);
            categoriesProperty.put(id, new CategoryObject(
                id,
                name,
                lastModified
            ));
        }
    }
}
