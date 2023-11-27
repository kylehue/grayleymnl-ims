package com.ims.controller;

import com.ims.Config;
import com.ims.components.*;
import com.ims.database.DBHistory;
import com.ims.model.BaseModel;
import com.ims.model.ProductModel;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.ProductObject;
import com.ims.utils.LazyLoader;
import com.ims.utils.SceneManager;
import com.ims.model.UserSessionModel;
import com.ims.utils.TabGroup;
import io.github.palexdev.materialfx.controls.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.util.*;

import com.ims.utils.LayoutUtils;

public class BaseController {
    //////////////////////////////////////////////////////////////////////
    // ----------------------- DASHBOARD PAGE ------------------------- //
    //////////////////////////////////////////////////////////////////////
    
    // The button used to access the dashboard or analytics page
    @FXML
    private MFXButton tabDashboardButton;
    
    // The container of the dashboard or analytics page
    @FXML
    private GridPane tabDashboardPane;
    
    // The container of the analytics cards in dashboard page
    @FXML
    private FlowPane analyticsFlowPane;
    
    @FXML
    private Label totalProductsLabel;
    
    @FXML
    private Label lowStocksProductsLabel;
    
    @FXML
    private Label outOfStocksProductsLabel;
    
    // The container for the body of inventory card in dashboard
    @FXML
    private StackPane inventoryStackPane;
    
    @FXML
    private MFXScrollPane historyScrollPane;
    
    @FXML
    private VBox historyVBox;
    
    @FXML
    private MFXButton refreshDashboardButton;
    
    ObservableMap<Integer, HistoryItem> history = FXCollections.observableHashMap();
    
    private boolean dashboardPageInitialized = false;
    
    private void initializeDashboardPage() {
        if (dashboardPageInitialized) {
            return;
        }
        dashboardPageInitialized = true;
        BaseModel.historyMap.addListener(
            (MapChangeListener<Integer, DBHistory.HistoryData>) change -> {
                int id = change.getKey();
                if (change.wasAdded()) {
                    DBHistory.HistoryData historyData = change.getValueAdded();
                    addHistory(historyData);
                } else if (change.wasRemoved()) {
                    removeHistory(id);
                }
            }
        );
        
        // Add the history in the model
        for (int id : BaseModel.historyMap.keySet()) {
            DBHistory.HistoryData historyData = BaseModel.historyMap.get(id);
            if (historyData == null) return;
            Platform.runLater(() -> {
                addHistory(historyData);
            });
        }
        
        LayoutUtils.applyVirtualScrolling(historyScrollPane, historyVBox);
        this.initializeHistoryLazyLoad();
        
        ObservableList<PieChart.Data> inventoryData = FXCollections.observableArrayList(
            new PieChart.Data("In Stock", 30),
            new PieChart.Data("Low Stock", 25),
            new PieChart.Data("Out of Stock", 45)
        );
        DonutChart donutChart = new DonutChart(inventoryData);
        donutChart.setPrefWidth(-1);
        donutChart.setMinWidth(-1);
        donutChart.setMaxWidth(-1);
        donutChart.setLabelsVisible(false);
        donutChart.setLegendSide(Side.RIGHT);
        inventoryStackPane.getChildren().add(donutChart);
        
        for (PieChart.Data data : inventoryData) {
            data.getNode().setStyle("""
                -fx-border-width: 0 !important;
                -fx-background-insets: -1 !important;
                """);
        }
        
        InvalidationListener listener = e -> {
            Platform.runLater(() -> {
                int totalProducts = BaseModel.totalProductsCount.get();
                int lowStockProducts = BaseModel.lowStockProductsCount.get();
                int outOfStockProducts = BaseModel.outOfStockProductsCount.get();
                double inStock = (double) (
                    totalProducts -
                        lowStockProducts -
                        outOfStockProducts
                ) / (double) totalProducts * 100;
                double lowStock = (double) lowStockProducts /
                    (double) totalProducts * 100;
                double outOfStock = (double) outOfStockProducts /
                    (double) totalProducts * 100;
                
                inventoryData.get(0).setPieValue(inStock);
                inventoryData.get(1).setPieValue(lowStock);
                inventoryData.get(2).setPieValue(outOfStock);
                
                totalProductsLabel.setText(
                    String.valueOf(totalProducts)
                );
                lowStocksProductsLabel.setText(
                    String.valueOf(lowStockProducts)
                );
                outOfStocksProductsLabel.setText(
                    String.valueOf(outOfStockProducts)
                );
            });
        };
        
        BaseModel.totalProductsCount.addListener(listener);
        BaseModel.lowStockProductsCount.addListener(listener);
        BaseModel.outOfStockProductsCount.addListener(listener);
        BaseModel.updateProductStats();
        
        LayoutUtils.addIconToButton(refreshDashboardButton, "/icons/refresh.svg");
        refreshDashboardButton.getStyleClass().add("icon-button");
        refreshDashboardButton.setText("");
        refreshDashboardButton.setOnAction(e -> {
            BaseModel.updateProductStats();
            BaseModel.refreshHistory();
        });
    }
    
    /**
     * Autoload history whenever needed.
     */
    private void initializeHistoryLazyLoad() {
        LazyLoader lazyLoader = new LazyLoader(
            historyScrollPane,
            historyVBox,
            history
        );
        
        lazyLoader.setLoader((requestType) -> {
            switch (requestType) {
                case INITIAL:
                    BaseModel.loadHistory(1);
                    break;
                case HIT_BOTTOM:
                case INSUFFICIENT:
                    BaseModel.loadHistory(Config.historyLoadLimit);
                    break;
            }
        });
    }
    
    private void addHistory(DBHistory.HistoryData historyData) {
        HistoryItem historyItem = new HistoryItem();
        
        Platform.runLater(() -> {
            if (this.history.containsKey(historyData.getID())) return;
            historyItem.setHistoryData(historyData);
            this.history.put(historyData.getID(), historyItem);
            historyVBox.getChildren().add(
                getSortedHistory().indexOf(historyItem),
                historyItem
            );
        });
    }
    
    private void removeHistory(int id) {
        Platform.runLater(() -> {
            HistoryItem historyToRemove = this.history.get(id);
            if (historyToRemove != null) {
                historyVBox.getChildren().remove(historyToRemove);
                this.history.remove(id);
            }
        });
    }
    
    private ArrayList<HistoryItem> getSortedHistory() {
        return new ArrayList<>(
            this.history.values().stream().sorted(
                Comparator.comparing(a -> a.getHistoryData().getLastModified())
            ).toList().reversed()
        );
    }
    
    //////////////////////////////////////////////////////////////////////
    // ------------------------ PRODUCT PAGE -------------------------- //
    //////////////////////////////////////////////////////////////////////
    
    // The button used to access the products page
    @FXML
    private MFXButton tabProductsButton;
    
    // The container of the products page
    @FXML
    private GridPane tabProductsPane;
    
    // The scroll pane containing the products.
    @FXML
    private MFXScrollPane productsScrollPane;
    
    // The container of the product cards in products page
    @FXML
    private FlowPane productsFlowPane;
    
    // The container of the category chip buttons in products page
    @FXML
    private FlowPane productsCategoriesFlowPane;
    
    // The search text field in products page
    @FXML
    private MFXTextField searchProductTextField;
    
    // The button used to access the products page
    @FXML
    private MFXButton addProductButton;
    
    @FXML
    private MFXButton refreshProductsButton;
    
    private ProductAddModal addProductModal = new ProductAddModal();
    
    private ObservableMap<Integer, Product> products = FXCollections.observableHashMap();
    
    private boolean productPageInitialized = false;
    
    private void initializeProductPage() {
        if (productPageInitialized) {
            return;
        }
        productPageInitialized = true;
        BaseModel.productMap.addListener(
            (MapChangeListener<Integer, ProductObject>) change -> {
                int id = change.getKey();
                if (change.wasAdded()) {
                    ProductObject productObject = change.getValueAdded();
                    addProduct(productObject);
                } else if (change.wasRemoved()) {
                    removeProduct(id);
                }
            }
        );
        
        // Add the products in the model
        for (int id : BaseModel.productMap.keySet()) {
            ProductObject productObject = BaseModel.productMap.get(id);
            if (productObject == null) return;
            Platform.runLater(() -> {
                addProduct(productObject);
            });
        }
        
        LayoutUtils.applyVirtualScrolling(productsScrollPane, productsFlowPane);
        this.initializeProductLazyLoad();
        
        LayoutUtils.addIconToButton(addProductButton, "/icons/plus.svg");
        
        addProductButton.setOnMouseClicked((e) -> {
            addProductModal.show();
        });
        
        addProductModal.setOnAction(() -> {
            if (!UserSessionModel.currentUserIsAllowAddProduct()) {
                System.out.println("The user has insufficient permissions.");
                return null;
            }
            
            String name = addProductModal.nameTextField.getText();
            CategoryObject category = addProductModal.categoryComboBox.getValue();
            
            ProductObject addedProductObject = BaseModel.addProduct(
                name,
                category.getID()
            );
            
            ProductModel.currentProduct.set(addedProductObject);
            SceneManager.setScene("product");
            
            addProductModal.hide();
            
            return null;
        });
        
        UserSessionModel.currentUser.addListener(e -> {
            if (UserSessionModel.currentUserIsAllowAddProduct()) {
                addProductButton.setVisible(true);
                addProductButton.setManaged(true);
            } else {
                addProductButton.setVisible(false);
                addProductButton.setManaged(false);
            }
        });
        
        searchProductTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            BaseModel.searchProducts(searchProductTextField.getText());
        });
        
        LayoutUtils.addIconToButton(refreshProductsButton, "/icons/refresh.svg");
        refreshProductsButton.getStyleClass().add("icon-button");
        refreshProductsButton.setText("");
        refreshProductsButton.setOnAction(e -> {
            BaseModel.searchProducts("");
        });
    }
    
    /**
     * Autoload products whenever needed.
     */
    private void initializeProductLazyLoad() {
        LazyLoader lazyLoader = new LazyLoader(
            productsScrollPane,
            productsFlowPane,
            products
        );
        
        lazyLoader.setLoader((requestType) -> {
            switch (requestType) {
                case INITIAL:
                    BaseModel.loadProducts(1);
                    break;
                case HIT_BOTTOM:
                case INSUFFICIENT:
                    if (!searchProductTextField.getText().isEmpty()) return;
                    if (!getAllActiveCategoryTags().isEmpty()) return;
                    BaseModel.loadProducts(Config.productLoadLimit);
                    break;
            }
        });
    }
    
    HashMap<Integer, CategoryTagButton> categoryTagButtons = new HashMap<>();
    
    private ArrayList<String> getAllActiveCategoryTags() {
        ArrayList<String> res = new ArrayList<>();
        for (int id : categoryTagButtons.keySet()) {
            CategoryTagButton categoryTagButton = categoryTagButtons.get(id);
            if (!categoryTagButton.isActive()) continue;
            res.add(categoryTagButton.getCategoryName());
        }
        
        return res;
    }
    
    private void addCategoryTag(CategoryObject categoryObject, boolean isActive) {
        CategoryTagButton categoryButton = new CategoryTagButton(categoryObject);
        
        Platform.runLater(() -> {
            final int CATEGORY_TAG_LIMIT = 12;
            if (productsCategoriesFlowPane.getChildren().size() < CATEGORY_TAG_LIMIT) {
                categoryButton.setActive(isActive);
                productsCategoriesFlowPane.getChildren().add(categoryButton);
                categoryButton.setOnMouseClicked(e -> {
                    BaseModel.searchProducts(
                        searchProductTextField.getText(),
                        getAllActiveCategoryTags().toArray(new String[0])
                    );
                });
                
                categoryTagButtons.put(categoryObject.getID(), categoryButton);
            }
        });
    }
    
    private void addProduct(
        ProductObject productObject
    ) {
        Product product = new Product();
        Platform.runLater(() -> {
            if (this.products.containsKey(productObject.getID())) return;
            product.setProductObject(productObject);
            this.products.put(productObject.getID(), product);
            if (productObject.isNew()) {
                productsFlowPane.getChildren().addFirst(product);
            } else {
                productsFlowPane.getChildren().addLast(product);
            }
        });
    }
    
    private void removeProduct(int id) {
        Platform.runLater(() -> {
            Product productToRemove = this.products.get(id);
            if (productToRemove != null) {
                productsFlowPane.getChildren().remove(productToRemove);
                this.products.remove(id);
            }
        });
    }
    
    //////////////////////////////////////////////////////////////////////
    // ------------------------ CATEGORY PAGE ------------------------- //
    //////////////////////////////////////////////////////////////////////
    
    // The button used to access the categories page
    @FXML
    private MFXButton tabCategoriesButton;
    
    // The container of the categories page
    @FXML
    private GridPane tabCategoriesPane;
    
    // The scroll pane containing the categories.
    @FXML
    private MFXScrollPane categoriesScrollPane;
    
    // The container of the category cards in categories page
    @FXML
    private FlowPane categoriesFlowPane;
    
    // The button used to add a category
    @FXML
    private MFXButton addCategoryButton;
    
    @FXML
    private MFXButton refreshCategoriesButton;
    
    // The text field used to search categories
    @FXML
    private MFXTextField searchCategoryTextField;
    
    private ObservableMap<Integer, Category> categories = FXCollections.observableHashMap();
    
    private CategoryAddModal addCategoryModal = new CategoryAddModal();
    
    private boolean categoryPageInitialized = false;
    
    private void initializeCategoryPage() {
        if (categoryPageInitialized) {
            return;
        }
        categoryPageInitialized = true;
        BaseModel.categoryMap.addListener(
            (MapChangeListener<Integer, CategoryObject>) change -> {
                int id = change.getKey();
                if (change.wasAdded()) {
                    CategoryObject categoryObject = change.getValueAdded();
                    addCategory(categoryObject);
                } else if (change.wasRemoved()) {
                    removeCategory(id);
                }
            }
        );
        
        // Add the current items in the model
        for (int id : BaseModel.categoryMap.keySet()) {
            CategoryObject categoryObject = BaseModel.categoryMap.get(id);
            if (categoryObject == null) return;
            Platform.runLater(() -> {
                addCategory(categoryObject);
            });
        }
        
        LayoutUtils.applyVirtualScrolling(categoriesScrollPane, categoriesFlowPane);
        this.initializeCategoryLazyLoad();
        
        LayoutUtils.addIconToButton(addCategoryButton, "/icons/plus.svg");
        addCategoryButton.setOnMouseClicked((e) -> {
            addCategoryModal.show();
        });
        
        addCategoryModal.setOnAction(() -> {
            String name = addCategoryModal.nameTextField.getText();
            addCategoryModal.hide();
            BaseModel.addCategory(name);
            return null;
        });
        
        UserSessionModel.currentUser.addListener(e -> {
            if (UserSessionModel.currentUserIsAllowAddCategory()) {
                addCategoryButton.setVisible(true);
                addCategoryButton.setManaged(true);
            } else {
                addCategoryButton.setVisible(false);
                addCategoryButton.setManaged(false);
            }
        });
        
        searchCategoryTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            BaseModel.searchCategories(searchCategoryTextField.getText());
        });
        
        LayoutUtils.addIconToButton(refreshCategoriesButton, "/icons/refresh.svg");
        refreshCategoriesButton.getStyleClass().add("icon-button");
        refreshCategoriesButton.setText("");
        refreshCategoriesButton.setOnAction(e -> {
            BaseModel.searchCategories("");
        });
    }
    
    /**
     * Autoload categories whenever needed.
     */
    private void initializeCategoryLazyLoad() {
        LazyLoader lazyLoader = new LazyLoader(
            categoriesScrollPane,
            categoriesFlowPane,
            categories
        );
        
        lazyLoader.setLoader((requestType) -> {
            switch (requestType) {
                case INITIAL:
                    BaseModel.loadCategories(1);
                    break;
                case HIT_BOTTOM:
                case INSUFFICIENT:
                    if (!searchCategoryTextField.getText().isEmpty()) return;
                    BaseModel.loadCategories(Config.categoryLoadLimit);
                    break;
            }
        });
    }
    
    private void addCategory(CategoryObject categoryObject) {
        Category category = new Category();
        Platform.runLater(() -> {
            int id = categoryObject.getID();
            if (this.categories.containsKey(id)) return;
            category.setCategoryObject(categoryObject);
            this.categories.put(id, category);
            if (categoryObject.isNew()) {
                categoriesFlowPane.getChildren().addFirst(category);
            } else {
                categoriesFlowPane.getChildren().addLast(category);
            }
            addCategoryTag(categoryObject, false);
        });
    }
    
    private void removeCategory(int id) {
        Category categoryToRemove = this.categories.get(id);
        if (categoryToRemove != null) {
            Platform.runLater(() -> {
                categoriesFlowPane.getChildren().remove(categoryToRemove);
                this.categories.remove(
                    categoryToRemove.getCategoryObject().getID()
                );
                
                // Remove category tag button in products if it exists
                CategoryTagButton categoryTagButton = categoryTagButtons.get(id);
                if (categoryTagButton != null) {
                    categoryTagButtons.remove(id);
                    productsCategoriesFlowPane.getChildren().remove(categoryTagButton);
                }
            });
        }
    }
    
    //////////////////////////////////////////////////////////////////////
    // ---------------------------- MAIN ------------------------------ //
    //////////////////////////////////////////////////////////////////////
    
    // The container of everything in here
    @FXML
    private GridPane rootContainer;
    
    // The settings button on the top right area
    @FXML
    private MFXButton settingsButton;
    
    @FXML
    public void initialize() {
        LayoutUtils.addIconToButton(tabDashboardButton, "/icons/home.svg");
        LayoutUtils.addIconToButton(tabProductsButton, "/icons/paw.svg");
        LayoutUtils.addIconToButton(tabCategoriesButton, "/icons/shape.svg");
        LayoutUtils.addIconToButton(settingsButton, "/icons/cog.svg");
        settingsButton.getStyleClass().add("icon-button");
        settingsButton.setText("");
        
        TabGroup tabGroup = new TabGroup(
            "tab-button-active",
            Arrays.asList(
                new Pair<>(tabDashboardButton, tabDashboardPane),
                new Pair<>(tabProductsButton, tabProductsPane),
                new Pair<>(tabCategoriesButton, tabCategoriesPane)
            ),
            "base"
        );
        
        tabGroup.setActivateFirstTabOnSceneChange(false);
        
        tabGroup.currentTabProperty().addListener(($1, $2, currentTab) -> {
            String tabText = currentTab.getKey().getText();
            if (Objects.equals(tabText, "Dashboard")) {
                this.initializeDashboardPage();
            } else if (Objects.equals(tabText, "Products")) {
                this.initializeProductPage();
            } else if (Objects.equals(tabText, "Categories")) {
                this.initializeCategoryPage();
            }
        });
        
        LayoutUtils.createResponsiveFlowPane(
            analyticsFlowPane,
            350,
            2.05,
            true
        );
        LayoutUtils.createResponsiveFlowPane(
            productsFlowPane,
            350,
            2.05,
            false
        );
        LayoutUtils.createResponsiveFlowPane(
            categoriesFlowPane,
            300,
            1,
            false
        );
        
        ContextMenu ctx = new ContextMenu();
        // TODO: change this based on user's current session
        ctx.setHeaderText("");
        MFXButton accountSettingsButton = ctx.addButtonItem("My Account");
        MFXButton managerUsersButton = ctx.addButtonItem("Manage Users");
        MFXButton logoutButton = ctx.addButtonItem("Logout");
        ctx.bindToNode(settingsButton);
        LayoutUtils.addIconToButton(logoutButton, "/icons/logout.svg");
        
        UserSessionModel.currentUser.addListener(e -> {
            String email = UserSessionModel.getCurrentUserEmail();
            ctx.setHeaderText(email != null ? email : "");
        });
        
        logoutButton.setOnMouseClicked((e) -> {
            UserSessionModel.logout();
        });
        
        accountSettingsButton.setOnMouseClicked((e) -> {
            SceneManager.setScene("account-settings");
        });
        
        managerUsersButton.setOnMouseClicked((e) -> {
            SceneManager.setScene("user-manager");
        });
        
        UserSessionModel.currentUser.addListener(e -> {
            if (UserSessionModel.currentUserIsOwner()) {
                managerUsersButton.setVisible(true);
                managerUsersButton.setManaged(true);
            } else {
                managerUsersButton.setVisible(false);
                managerUsersButton.setManaged(false);
            }
        });
    }
}
