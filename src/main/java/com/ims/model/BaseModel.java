package com.ims.model;

import com.ims.Config;
import com.ims.database.DBCategories;
import com.ims.database.DBHistory;
import com.ims.database.DBProducts;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.ProductObject;
import com.ims.utils.AsyncCaller;
import com.ims.utils.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;

import java.sql.Timestamp;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseModel {
    //////////////////////////////////////////////////////////////////////
    // ----------------------- DASHBOARD PAGE ------------------------- //
    //////////////////////////////////////////////////////////////////////
    
    public static final IntegerProperty totalProductsCount = new SimpleIntegerProperty(0);
    public static final IntegerProperty lowStockProductsCount = new SimpleIntegerProperty(0);
    public static final IntegerProperty outOfStockProductsCount = new SimpleIntegerProperty(0);
    public static final ObservableMap<Integer, DBHistory.HistoryData>
        historyMap = FXCollections.observableHashMap();
    public static final BooleanProperty
        isBusyHistory = new SimpleBooleanProperty(false);
    
    public static void updateProductStats() {
        new AsyncCaller<Void>(task -> {
            int totalProducts = DBProducts.getTotalProductsCount();
            int lowStockProducts = DBProducts.getLowStockProductsCount();
            int outOfStockProducts = DBProducts.getOutOfStockProductsCount();
            
            totalProductsCount.set(totalProducts);
            lowStockProductsCount.set(lowStockProducts);
            outOfStockProductsCount.set(outOfStockProducts);
            
            return null;
        }, Utils.executor).execute();
    }
    
    /**
     * Load more history from the database.
     *
     * @param limit The limit of the rows to retrieve.
     */
    public static void loadHistory(int limit) {
        new AsyncCaller<Void>(task -> {
            isBusyHistory.set(true);
            DBHistory.HistoryListData productRows = DBHistory.getBulk(
                historyMap.keySet(),
                limit
            );
            
            for (DBHistory.HistoryData historyData : productRows) {
                historyMap.put(historyData.getID(), historyData);
            }
            return null;
        }, Utils.executor).onSucceeded(e -> {
            isBusyHistory.set(false);
        }).execute();
    }
    
    public static void refreshHistory() {
        historyMap.clear();
        loadHistory(Config.historyLoadLimit);
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
    public static AsyncCaller<ProductObject> addProduct(
        String name,
        Integer categoryID
    ) {
        return new AsyncCaller<ProductObject>(task -> {
            if (name.isEmpty()) return null;
            if (!UserSessionModel.currentUserIsAllowAddProduct()) {
                System.out.println("The user has insufficient permissions.");
                return null;
            }
            
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
            
            DBHistory.add(
                DBHistory.Action.ADD_PRODUCT,
                name,
                UserSessionModel.getCurrentUserID()
            );
            
            return loadProduct(
                DBProducts.add(
                    name,
                    categoryID,
                    null,
                    0,
                    0
                ),
                true
            );
        }, Utils.executor).onSucceeded((e) -> {
            isBusyProduct.set(false);
            updateProductStats();
            refreshHistory();
        });
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
        
        new AsyncCaller<Void>(task -> {
            isBusyProduct.set(true);
            
            loadProduct(
                DBProducts.update(
                    id,
                    name,
                    price,
                    categoryID,
                    imageURL,
                    currentStocks,
                    expectedStocks
                ),
                false
            );
            
            DBHistory.add(
                DBHistory.Action.EDIT_PRODUCT,
                name,
                UserSessionModel.getCurrentUserID()
            );
            
            return null;
        }, Utils.executor).onSucceeded((e) -> {
            isBusyProduct.set(false);
            updateProductStats();
            refreshHistory();
        }).execute();
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
        
        new AsyncCaller<Void>(task -> {
            isBusyProduct.set(true);
            
            DBProducts.remove(id);
            
            ProductObject productObject = productMap.get(id);
            
            DBHistory.add(
                DBHistory.Action.REMOVE_PRODUCT,
                productObject == null ? "" : productObject.getName(),
                UserSessionModel.getCurrentUserID()
            );
            
            productMap.remove(id);
            
            return null;
        }, Utils.executor).onSucceeded((e) -> {
            isBusyProduct.set(false);
            updateProductStats();
            refreshHistory();
            
        }).execute();
    }
    
    public static void searchProducts(String searchText, String... categories) {
        new AsyncCaller<Void>(task -> {
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
                loadProduct(row, false);
            }
            
            return null;
        }, Utils.executor).execute();
    }
    
    private static ProductObject loadProduct(
        DBProducts.ProductData productData,
        boolean isNew
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
                lastModified,
                isNew
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
        new AsyncCaller<Void>(task -> {
            isBusyProduct.set(true);
            DBProducts.ProductListData productRows = DBProducts.getBulk(
                productMap.keySet(),
                limit
            );
            
            for (DBProducts.ProductData row : productRows) {
                loadProduct(row, false);
            }
            return null;
        }, Utils.executor).onSucceeded((e) -> {
            isBusyProduct.set(false);
        }).execute();
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
        
        new AsyncCaller<Void>(task -> {
            isBusyCategory.set(true);
            loadCategory(DBCategories.add(name), true);
            
            DBHistory.add(
                DBHistory.Action.ADD_CATEGORY,
                name,
                UserSessionModel.getCurrentUserID()
            );
            
            return null;
        }, Utils.executor).onSucceeded((e) -> {
            isBusyCategory.set(false);
            refreshHistory();
        }).execute();
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
        new AsyncCaller<Void>(task -> {
            isBusyCategory.set(true);
            
            loadCategory(DBCategories.update(id, name), false);
            
            DBHistory.add(
                DBHistory.Action.EDIT_CATEGORY,
                name,
                UserSessionModel.getCurrentUserID()
            );
            
            return null;
            
        }, Utils.executor).onSucceeded((e) -> {
            isBusyCategory.set(false);
            refreshHistory();
        }).execute();
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
        
        new AsyncCaller<Void>(task -> {
            isBusyCategory.set(true);
            
            DBCategories.remove(categoryID);
            categoryMap.remove(categoryID);
            
            CategoryObject categoryObject = categoryMap.get(categoryID);
            
            DBHistory.add(
                DBHistory.Action.REMOVE_CATEGORY,
                categoryObject == null ? "" : categoryObject.getName(),
                UserSessionModel.getCurrentUserID()
            );
            
            // Nullify category of products that has the removed category
            for (int productID : productMap.keySet()) {
                ProductObject productObject = productMap.get(productID);
                if (productObject.getCategoryID() == null) continue;
                if (productObject.getCategoryID() != categoryID) continue;
                productObject.setCategoryID(null);
            }
            
            return null;
        }, Utils.executor).onSucceeded((e) -> {
            isBusyCategory.set(false);
            refreshHistory();
        }).execute();
    }
    
    public static AsyncCaller<CategoryObject> loadAndGetCategory(int id) {
        return new AsyncCaller<CategoryObject>(task -> {
            CategoryObject categoryObject = categoryMap.get(id);
            if (categoryObject != null) {
                return categoryObject;
            }
            
            isBusyCategory.set(true);
            
            DBCategories.CategoryData row = DBCategories.getOne(DBCategories.Column.ID, id);
            if (row != null) {
                return loadCategory(row, false);
            }
            
            return null;
        }, Utils.executor).onSucceeded((e) -> {
            isBusyCategory.set(false);
        });
    }
    
    public static void searchCategories(String searchText) {
        new AsyncCaller<Void>(task -> {
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
                loadCategory(row, false);
            }
            
            return null;
        }, Utils.executor).execute();
    }
    
    private static CategoryObject loadCategory(
        DBCategories.CategoryData categoryData,
        boolean isNew
    ) {
        return loadCategoryToMap(categoryData, categoryMap, isNew);
    }
    
    public static CategoryObject loadCategoryToMap(
        DBCategories.CategoryData categoryData,
        ObservableMap<Integer, CategoryObject> map,
        boolean isNew
    ) {
        int id = categoryData.getID();
        String name = categoryData.getName();
        Timestamp lastModified = categoryData.getLastModified();
        
        CategoryObject categoryObject = map.get(id);
        if (!map.containsKey(id)) {
            categoryObject = new CategoryObject(
                id,
                name,
                lastModified,
                isNew
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
        new AsyncCaller<Void>(task -> {
            isBusyCategory.set(true);
            DBCategories.CategoryListData categoryRows = DBCategories.getBulk(
                map.keySet(),
                limit
            );
            
            for (DBCategories.CategoryData row : categoryRows) {
                loadCategoryToMap(row, map, false);
            }
            return null;
        }, Utils.executor).onSucceeded((e) -> {
            isBusyCategory.set(false);
        }).execute();
    }
}
