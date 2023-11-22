package com.ims.model;

import com.ims.Config;
import com.ims.database.DBCategories;
import com.ims.database.DBProducts;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.ProductObject;
import com.ims.utils.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public abstract class BaseModel {
    //////////////////////////////////////////////////////////////////////
    // ----------------------- DASHBOARD PAGE ------------------------- //
    //////////////////////////////////////////////////////////////////////
    
    public static final IntegerProperty totalProductsCount = new SimpleIntegerProperty(0);
    public static final IntegerProperty lowStockProductsCount = new SimpleIntegerProperty(0);
    public static final IntegerProperty outOfStockProductsCount = new SimpleIntegerProperty(0);
    
    public static void updateProductStats() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int totalProducts = DBProducts.getTotalProductsCount();
                int lowStockProducts = DBProducts.getLowStockProductsCount();
                int outOfStockProducts = DBProducts.getOutOfStockProductsCount();
                
                totalProductsCount.set(totalProducts);
                lowStockProductsCount.set(lowStockProducts);
                outOfStockProductsCount.set(outOfStockProducts);
                
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
    public static ProductObject addProduct(
        String name,
        int categoryID
    ) {
        if (name.isEmpty()) return null;
        if (!UserSessionModel.currentUserIsAllowAddProduct()) {
            System.out.println("The user has insufficient permissions.");
            return null;
        }
        
        Task<ProductObject> task = new Task<>() {
            @Override
            protected ProductObject call() throws Exception {
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
                
                return loadProduct(newProduct);
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyProduct.set(false);
            BaseModel.updateProductStats();
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
        
        ProductObject productObject = null;
        try {
            productObject = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return productObject;
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
        if (!UserSessionModel.currentUserIsAllowEditProduct()) {
            System.out.println("The user has insufficient permissions.");
            return;
        }
        
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
                    loadProduct(newProduct);
                }
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            isBusyProduct.set(false);
            BaseModel.updateProductStats();
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
        if (!UserSessionModel.currentUserIsAllowDeleteProduct()) {
            System.out.println("The user has insufficient permissions.");
            return;
        }
        
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
            BaseModel.updateProductStats();
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    public static void searchProducts(String searchText) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                productMap.clear();
                if (searchText.isEmpty()) {
                    loadProducts(Config.productLoadLimit);
                    return null;
                }
                
                String searchPattern = Utils.textToSearchPattern(searchText);
                ArrayList<HashMap<DBProducts.Column, Object>> result = DBProducts.search(
                    searchPattern
                );
                
                for (HashMap<DBProducts.Column, Object> row : result) {
                    loadProduct(row);
                }
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
        
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    private static ProductObject loadProduct(
        HashMap<DBProducts.Column, Object> product
    ) {
        int id = (Integer) product.get(DBProducts.Column.ID);
        
        String name = (String) product.get(
            DBProducts.Column.NAME
        );
        Double price = (Double) product.get(
            DBProducts.Column.PRICE
        );
        Integer categoryID = (Integer) product.get(
            DBProducts.Column.CATEGORY_ID
        );
        String imageURL = (String) product.get(
            DBProducts.Column.IMAGE_URL
        );
        Integer currentStocks = (Integer) product.get(
            DBProducts.Column.CURRENT_STOCKS
        );
        Integer expectedStocks = (Integer) product.get(
            DBProducts.Column.EXPECTED_STOCKS
        );
        Timestamp lastModified = (Timestamp) product.get(
            DBProducts.Column.LAST_MODIFIED
        );
        
        ProductObject productObject = productMap.get(id);
        if (!productMap.containsKey(id)) {
            productObject = new ProductObject(
                id,
                name,
                price,
                categoryID,
                imageURL,
                currentStocks,
                expectedStocks,
                lastModified
            );
            productMap.put(id, productObject);
        } else {
            productObject.setName(name);
            productObject.setPrice(price);
            productObject.setCategoryID(categoryID);
            productObject.setImageURL(imageURL);
            productObject.setCurrentStocks(currentStocks);
            productObject.setExpectedStocks(expectedStocks);
            productObject.setLastModified(lastModified);
        }
        
        return productObject;
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
                    loadProduct(row);
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
        if (!UserSessionModel.currentUserIsAllowAddCategory()) {
            System.out.println("The user has insufficient permissions.");
            return;
        }
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
        if (!UserSessionModel.currentUserIsAllowEditCategory()) {
            System.out.println("The user has insufficient permissions.");
            return;
        }
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
                    
                    categoryObject.setName(newName);
                    categoryObject.setLastModified(newLastModified);
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
        if (!UserSessionModel.currentUserIsAllowDeleteCategory()) {
            System.out.println("The user has insufficient permissions.");
            return;
        }
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
        
        Task<CategoryObject> task = new Task<>() {
            @Override
            protected CategoryObject call() {
                isBusyCategory.set(true);
                
                HashMap<DBCategories.Column, Object> row = DBCategories.getOne(DBCategories.Column.ID, id);
                if (row != null) {
                    CategoryObject categoryObject = new CategoryObject(
                        id,
                        (String) row.get(DBCategories.Column.NAME),
                        (Timestamp) row.get(DBCategories.Column.LAST_MODIFIED)
                    );
                    categoryMap.put(id, categoryObject);
                    return categoryObject;
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
        
        try {
            categoryObject = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return categoryObject;
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
                    
                    // Add in list
                    String name = (String) row.get(DBCategories.Column.NAME);
                    Timestamp lastModified = (Timestamp) row.get(DBCategories.Column.LAST_MODIFIED);
                    
                    if (category == null) {
                        category = new CategoryObject(
                            id,
                            name,
                            lastModified
                        );
                        categoryMap.put(id, category);
                    } else {
                        category.setName(name);
                        category.setLastModified(lastModified);
                    }
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
