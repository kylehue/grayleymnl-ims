package com.ims.model;

import com.ims.database.DBCategories;
import com.ims.database.DBProducts;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.ProductObject;
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

public abstract class BaseModel {
    //////////////////////////////////////////////////////////////////////
    // ----------------------- DASHBOARD PAGE ------------------------- //
    //////////////////////////////////////////////////////////////////////
    
    
    //////////////////////////////////////////////////////////////////////
    // ------------------------ PRODUCT PAGE -------------------------- //
    //////////////////////////////////////////////////////////////////////
    public static final ObservableMap<Integer, ProductObject>
        productMap = FXCollections.observableHashMap();
    public static final BooleanProperty
        isBusyProduct = new SimpleBooleanProperty(false);
    
    public static ProductObject getProductById(int id) {
        return productMap.get(id);
    }
    
    /**
     * Adds product in the database.
     *
     * @param name The name of the product to add.
     */
    public static HashMap<DBProducts.Column, Object> addProduct(
        String name,
        int categoryID
    ) {
        if (name.isEmpty()) return null;
        
        Task<HashMap<DBProducts.Column, Object>> task = new Task<>() {
            @Override
            protected HashMap<DBProducts.Column, Object> call() throws Exception {
                isBusyProduct.set(true);
                // First, we have to make sure the category exists in the database
                HashMap<DBCategories.Column, Object> retrievedCategory =
                    DBCategories.getOne(DBCategories.Column.ID, categoryID);
                if (retrievedCategory == null) {
                    System.out.println(
                        "Category with the id of %s doesn't exist.".formatted(
                            categoryID
                        )
                    );
                    return null;
                }
                
                // Add the product
                HashMap<DBProducts.Column, Object> newProduct = DBProducts.add(
                    name,
                    categoryID,
                    null,
                    0,
                    0
                );
                int newID = (Integer) newProduct.get(DBProducts.Column.ID);
                String newName = (String) newProduct.get(
                    DBProducts.Column.NAME
                );
                double newPrice = (double) newProduct.get(
                    DBProducts.Column.PRICE
                );
                int newCategoryID = (Integer) newProduct.get(
                    DBProducts.Column.CATEGORY_ID
                );
                String newImageURL = (String) newProduct.get(
                    DBProducts.Column.IMAGE_URL
                );
                int newCurrentStocks = (Integer) newProduct.get(
                    DBProducts.Column.CURRENT_STOCKS
                );
                int newExpectedStocks = (Integer) newProduct.get(
                    DBProducts.Column.EXPECTED_STOCKS
                );
                Timestamp newLastModified = (Timestamp) newProduct.get(
                    DBProducts.Column.LAST_MODIFIED
                );
                productMap.put(newID, new ProductObject(
                    newID,
                    newName,
                    newPrice,
                    newCategoryID,
                    newImageURL,
                    newCurrentStocks,
                    newExpectedStocks,
                    newLastModified
                ));
                
                return newProduct;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyProduct.set(false);
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
        
        return task.getValue();
    }
    
    /**
     * Updates a product in the database.
     *
     * @param id   The id of the product to update.
     * @param name The new name of the product.
     */
    public static void updateProduct(
        int id,
        String name,
        Double price,
        Integer categoryID,
        String imageURL,
        Integer currentStocks,
        Integer expectedStocks
    ) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyProduct.set(true);
                
                // Update in database
                HashMap<DBProducts.Column, Object> newProduct = DBProducts.update(
                    id,
                    name,
                    price,
                    categoryID,
                    imageURL,
                    currentStocks,
                    expectedStocks
                );
                
                // Update in list if it exists
                ProductObject productObject = productMap.get(id);
                if (productObject != null) {
                    String newName = (String) newProduct.get(
                        DBProducts.Column.NAME
                    );
                    Double newPrice = (Double) newProduct.get(
                        DBProducts.Column.PRICE
                    );
                    Integer newCategoryID = (Integer) newProduct.get(
                        DBProducts.Column.CATEGORY_ID
                    );
                    String newImageURL = (String) newProduct.get(
                        DBProducts.Column.IMAGE_URL
                    );
                    Integer newCurrentStocks = (Integer) newProduct.get(
                        DBProducts.Column.CURRENT_STOCKS
                    );
                    Integer newExpectedStocks = (Integer) newProduct.get(
                        DBProducts.Column.EXPECTED_STOCKS
                    );
                    Timestamp newLastModified = (Timestamp) newProduct.get(
                        DBProducts.Column.LAST_MODIFIED
                    );
                    productMap.put(id, new ProductObject(
                        id,
                        newName,
                        newPrice,
                        newCategoryID,
                        newImageURL,
                        newCurrentStocks,
                        newExpectedStocks,
                        newLastModified
                    ));
                }
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyProduct.set(false);
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    /**
     * Remove a product in the database.
     *
     * @param id The id of the category to remove.
     */
    public static void removeProduct(int id) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyProduct.set(true);
                
                DBProducts.remove(id);
                productMap.remove(id);
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyProduct.set(false);
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    /**
     * Load more products from the database.
     *
     * @param limit The limit of the rows to retrieve.
     */
    public static void loadProducts(int limit) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                isBusyProduct.set(true);
                ArrayList<HashMap<DBProducts.Column, Object>> productRows = DBProducts.getInRange(
                    productMap.size(),
                    limit
                );
                
                for (HashMap<DBProducts.Column, Object> row : productRows) {
                    int id = (Integer) row.get(DBProducts.Column.ID);
                    
                    // Skip if already added (this shouldn't happen but just to be sure)
                    ProductObject product = productMap.get(id);
                    if (product != null) {
                        continue;
                    }

                    // Add in list
                    String name = (String) row.get(
                        DBProducts.Column.NAME
                    );
                    Double price = (Double) row.get(
                        DBProducts.Column.PRICE
                    );
                    Integer categoryID = (Integer) row.get(
                        DBProducts.Column.CATEGORY_ID
                    );
                    String imageURL = (String) row.get(
                        DBProducts.Column.IMAGE_URL
                    );
                    Integer currentStocks = (Integer) row.get(
                        DBProducts.Column.CURRENT_STOCKS
                    );
                    Integer expectedStocks = (Integer) row.get(
                        DBProducts.Column.EXPECTED_STOCKS
                    );
                    Timestamp lastModified = (Timestamp) row.get(
                        DBProducts.Column.LAST_MODIFIED
                    );
                    productMap.put(id, new ProductObject(
                        id,
                        name,
                        price,
                        categoryID,
                        imageURL,
                        currentStocks,
                        expectedStocks,
                        lastModified
                    ));
                }
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyProduct.set(false);
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
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
        
        task.setOnFailed(e -> {
            System.out.println(e);
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
        
        task.setOnFailed(e -> {
            System.out.println(e);
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
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    public static CategoryObject loadAndGetCategory(int id) {
        CategoryObject categoryObject = categoryMap.get(id);
        if (categoryObject != null) {
            return categoryObject;
        }
        
        HashMap<DBCategories.Column, Object> row = DBCategories.getOne(DBCategories.Column.ID, id);
        if (row != null) {
            categoryObject = new CategoryObject(
                id,
                (String) row.get(DBCategories.Column.NAME),
                (Timestamp) row.get(DBCategories.Column.LAST_MODIFIED)
            );
            categoryMap.put(id, categoryObject);
            return categoryObject;
        }
        
        return null;
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
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
}
