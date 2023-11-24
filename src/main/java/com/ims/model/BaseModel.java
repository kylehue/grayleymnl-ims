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
        Integer categoryID
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
                DBCategories.CategoryData retrievedCategory =
                    DBCategories.getOne(DBCategories.Column.ID, categoryID);
                if (retrievedCategory == null) {
                    System.out.println(
                        "Category with the id of %s doesn't exist.".formatted(
                            categoryID
                        )
                    );
                    return null;
                }
                
                return loadProduct(DBProducts.add(
                    name,
                    categoryID,
                    null,
                    0,
                    0
                ));
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
                
                loadProduct(DBProducts.update(
                    id,
                    name,
                    price,
                    categoryID,
                    imageURL,
                    currentStocks,
                    expectedStocks
                ));
                
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
    
    public static void searchProducts(String searchText, String... categories) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                productMap.clear();
                if (searchText.isEmpty() && categories.length == 0) {
                    loadProducts(Config.productLoadLimit);
                    return null;
                }
                
                String searchPattern = Utils.textToSearchPattern(searchText);
                DBProducts.ProductListData result = DBProducts.search(
                    searchPattern,
                    categories
                );
                
                for (DBProducts.ProductData row : result) {
                    loadProduct(row);
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
    
    private static ProductObject loadProduct(
        DBProducts.ProductData productData
    ) {
        int id = productData.getID();
        String name = productData.getName();
        Double price = productData.getPrice();
        Integer categoryID = productData.getCategoryID();
        String imageURL = productData.getImageURL();
        Integer currentStocks = productData.getCurrentStocks();
        Integer expectedStocks = productData.getExpectedStocks();
        Timestamp lastModified = productData.getLastModified();
        
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
                DBProducts.ProductListData productRows = DBProducts.getInRange(
                    productMap.size(),
                    limit
                );
                
                for (DBProducts.ProductData row : productRows) {
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
                loadCategory(DBCategories.add(name));
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
                
                loadCategory(DBCategories.update(id, name));
                
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
     * @param categoryID The id of the category to remove.
     */
    public static void removeCategory(int categoryID) {
        if (!UserSessionModel.currentUserIsAllowDeleteCategory()) {
            System.out.println("The user has insufficient permissions.");
            return;
        }
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyCategory.set(true);
                
                DBCategories.remove(categoryID);
                categoryMap.remove(categoryID);
                
                // Nullify category of products that has the removed category
                for (int productID : productMap.keySet()) {
                    ProductObject productObject = productMap.get(productID);
                    if (productObject.getCategoryID() == null) continue;
                    if (productObject.getCategoryID() != categoryID) continue;
                    productObject.setCategoryID(null);
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
    
    public static CategoryObject loadAndGetCategory(int id) {
        CategoryObject categoryObject = categoryMap.get(id);
        if (categoryObject != null) {
            return categoryObject;
        }
        
        Task<CategoryObject> task = new Task<>() {
            @Override
            protected CategoryObject call() {
                isBusyCategory.set(true);
                
                DBCategories.CategoryData row = DBCategories.getOne(DBCategories.Column.ID, id);
                if (row != null) {
                    return loadCategory(row);
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
    
    public static void searchCategories(String searchText) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                categoryMap.clear();
                if (searchText.isEmpty()) {
                    loadCategories(Config.categoryLoadLimit);
                    return null;
                }
                
                String searchPattern = Utils.textToSearchPattern(searchText);
                DBCategories.CategoryListData result = DBCategories.search(
                    searchPattern
                );
                
                for (DBCategories.CategoryData row : result) {
                    loadCategory(row);
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
    
    private static CategoryObject loadCategory(
        DBCategories.CategoryData categoryData
    ) {
        return loadCategoryToMap(categoryData, categoryMap);
    }
    
    public static CategoryObject loadCategoryToMap(
        DBCategories.CategoryData categoryData,
        ObservableMap<Integer, CategoryObject> map
    ) {
        int id = categoryData.getID();
        String name = categoryData.getName();
        Timestamp lastModified = categoryData.getLastModified();
        
        CategoryObject categoryObject = map.get(id);
        if (!map.containsKey(id)) {
            categoryObject = new CategoryObject(
                id,
                name,
                lastModified
            );
            map.put(id, categoryObject);
        } else {
            categoryObject.setName(name);
            categoryObject.setLastModified(lastModified);
        }
        
        return categoryObject;
    }
    
    /**
     * Load more categories from the database.
     *
     * @param limit The limit of the rows to retrieve.
     */
    public static void loadCategories(int limit) {
        loadCategoriesToMap(limit, categoryMap);
    }
    
    public static void loadCategoriesToMap(
        int limit,
        ObservableMap<Integer, CategoryObject> map
    ) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isBusyCategory.set(true);
                DBCategories.CategoryListData categoryRows = DBCategories.getInRange(
                    map.size(),
                    limit
                );
                
                for (DBCategories.CategoryData row : categoryRows) {
                    loadCategoryToMap(row, map);
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
