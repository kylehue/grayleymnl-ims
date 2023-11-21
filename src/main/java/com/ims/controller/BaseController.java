package com.ims.controller;

import com.ims.components.*;
import com.ims.model.BaseModel;
import com.ims.model.ProductModel;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.ProductObject;
import com.ims.utils.SceneManager;
import com.ims.model.UserSessionModel;
import io.github.palexdev.materialfx.controls.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.ExecutionException;

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
    
    private void initializeDashboardPage() {
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
    
    private ProductAddModal addProductModal = new ProductAddModal();
    
    private HashMap<Integer, Product> products = new HashMap<>();
    
    private void initializeProductPage() {
        BaseModel.productMap.addListener(
            (MapChangeListener<Integer, ProductObject>) change -> {
                int id = change.getKey();
                boolean isAddedAlready = products.get(id) != null;
                boolean needsToBeAdded = change.wasAdded() && !isAddedAlready;
                boolean needsToBeUpdated = change.wasAdded() && isAddedAlready;
                boolean needsToBeRemoved = change.wasRemoved() && isAddedAlready;
                if (needsToBeAdded) {
                    addProduct(change.getValueAdded());
                } else if (needsToBeUpdated) {
                    ProductObject productObject = change.getValueAdded();
                    Product oldProduct = products.get(id);
                    oldProduct.setProductObject(productObject);
                } else if (needsToBeRemoved) {
                    removeProduct(id);
                }
            }
        );
        
        LayoutUtils.applyVirtualScrolling(productsScrollPane, productsFlowPane);
        this.initializeProductLazyLoad();
        
        addProductButton.setOnMouseClicked((e) -> {
            addProductModal.show();
        });
        
        addProductModal.addButton.setOnMouseClicked((e) -> {
            if (
                !addProductModal.nameTextFieldValidator.isValid() ||
                    !addProductModal.categoryComboBoxValidator.isValid()
            ) {
                return;
            }
            
            String name = addProductModal.nameTextField.getText();
            CategoryObject category = addProductModal.categoryComboBox.getValue();
            
            ProductObject addedProductObject = null;
            try {
                addedProductObject = BaseModel.addProduct(name, category.getID());
            } catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            
            ProductModel.currentProduct.set(addedProductObject);
            SceneManager.setScene("product");
            
            addProductModal.hide();
        });
    }
    
    /**
     * Autoload products whenever needed.
     */
    private void initializeProductLazyLoad() {
        // Load products whenever the scrollbar hits the bottom.
        productsScrollPane.vvalueProperty().addListener(($1, $2, scrollValue) -> {
            if (scrollValue.doubleValue() == 1) {
                BaseModel.loadProducts(3);
            }
        });
        
        // The listener above won't work if there is no scrollbar.
        // So here, we add components until the scroll pane gets a scrollbar.
        productsScrollPane.viewportBoundsProperty().addListener(($1, $2, newValue) -> {
            double contentHeight = productsFlowPane.getBoundsInLocal().getHeight();
            double viewportHeight = newValue.getHeight();
            if (contentHeight < viewportHeight) {
                BaseModel.loadProducts(3);
            }
        });
        
        // Everything above won't work if the `viewportBoundsProperty` doesn't trigger.
        // So here, we can trigger it by loading initial products.
        BaseModel.loadProducts(9);
    }
    
    private TagButton addCategoryTag(String categoryName, boolean isActive) {
        TagButton categoryButton = new TagButton();
        categoryButton.setText(categoryName);
        categoryButton.setActive(isActive);
        productsCategoriesFlowPane.getChildren().add(categoryButton);
        
        return categoryButton;
    }
    
    private Product addProduct(
        ProductObject productObject
    ) {
        Product product = new Product(productObject);
        Platform.runLater(() -> {
            this.products.put(productObject.getID(), product);
            productsFlowPane.getChildren().add(
                this.getSortedProducts().indexOf(product),
                product
            );
            
            product.setName(productObject.getName());
            product.setCategory(
                BaseModel.loadAndGetCategory(
                    productObject.getCategoryID()
                ).getName()
            );
            product.setStocks(
                productObject.getCurrentStocks(),
                productObject.getExpectedStocks()
            );
            product.setPrice((float) productObject.getPrice());
            product.setImage(productObject.getImageURL());
        });
        return product;
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
    
    private ArrayList<Product> getSortedProducts() {
        ArrayList<Product> sortedProducts = new ArrayList<>(
            this.products.values().stream().sorted(
                (a, b) -> {
                    return b.getProductObject().getLastModified().compareTo(
                        a.getProductObject().getLastModified()
                    );
                }
            ).toList()
        );
        
        return sortedProducts;
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
    
    // The button used to save all categories
    @FXML
    private MFXButton saveAllCategoriesButton;
    
    // The button used to add a category
    @FXML
    private MFXButton addCategoryButton;
    
    private HashMap<Integer, Category> categories = new HashMap<>();
    
    private CategoryAddModal addCategoryModal = new CategoryAddModal();
    
    private void initializeCategoryPage() {
        BaseModel.categoryMap.addListener(
            (MapChangeListener<Integer, CategoryObject>) change -> {
                int id = change.getKey();
                boolean isAddedAlready = categories.get(id) != null;
                boolean needsToBeAdded = change.wasAdded() && !isAddedAlready;
                boolean needsToBeUpdated = change.wasAdded() && isAddedAlready;
                boolean needsToBeRemoved = change.wasRemoved() && isAddedAlready;
                if (needsToBeAdded) {
                    addCategory(change.getValueAdded());
                } else if (needsToBeUpdated) {
                    Category category = categories.get(id);
                    CategoryObject categoryObject = change.getValueAdded();
                    category.setCategoryObject(categoryObject);
                } else if (needsToBeRemoved) {
                    removeCategory(id);
                }
            }
        );
        
        LayoutUtils.applyVirtualScrolling(categoriesScrollPane, categoriesFlowPane);
        this.initializeCategoryLazyLoad();
        addCategoryButton.setOnMouseClicked((e) -> {
            addCategoryModal.show();
        });
        
        addCategoryModal.addButton.setOnMouseClicked((e) -> {
            String name = addCategoryModal.nameTextField.getText();
            if (!addCategoryModal.nameTextFieldValidator.isValid()) return;
            addCategoryModal.hide();
            BaseModel.addCategory(name);
        });
        
        saveAllCategoriesButton.setOnMouseClicked((e) -> {
            for (Category category : this.categories.values()) {
                if (!category.nameTextFieldValidator.isValid()) {
                    continue;
                }
                
                BaseModel.updateCategory(
                    category.getCategoryObject().getID(),
                    category.getCategoryName()
                );
            }
            
            PopupService.messageDialog.setup(
                "Update Categories",
                "All categories has been successfully updated.",
                "Got it!"
            ).show();
        });
    }
    
    /**
     * Autoload categories whenever needed.
     */
    private void initializeCategoryLazyLoad() {
        // Load categories whenever the scrollbar hits the bottom.
        categoriesScrollPane.vvalueProperty().addListener(($1, $2, scrollValue) -> {
            if (scrollValue.doubleValue() == 1) {
                BaseModel.loadCategories(12);
            }
        });
        
        // The listener above won't work if there is no scrollbar.
        // So here, we add components until the scroll pane gets a scrollbar.
        categoriesScrollPane.viewportBoundsProperty().addListener(($1, $2, newValue) -> {
            double contentHeight = categoriesFlowPane.getBoundsInLocal().getHeight();
            double viewportHeight = newValue.getHeight();
            if (contentHeight < viewportHeight) {
                BaseModel.loadCategories(4);
            }
        });
        
        // Everything above won't work if the `viewportBoundsProperty` doesn't trigger.
        // So here, we can trigger it by loading initial categories.
        BaseModel.loadCategories(12);
    }
    
    private Category addCategory(CategoryObject categoryObject) {
        Category category = new Category(categoryObject);
        int id = categoryObject.getID();
        Platform.runLater(() -> {
            this.categories.put(id, category);
            
            categoriesFlowPane.getChildren().add(
                this.getSortedCategories().indexOf(category),
                category
            );
            
            addCategoryTag(category.getCategoryName(), false);
        });
        
        return category;
    }
    
    private void removeCategory(int id) {
        Category categoryToRemove = this.categories.get(id);
        if (categoryToRemove != null) {
            Platform.runLater(() -> {
                categoriesFlowPane.getChildren().remove(categoryToRemove);
                this.categories.remove(
                    categoryToRemove.getCategoryObject().getID()
                );
            });
        }
    }
    
    private ArrayList<Category> getSortedCategories() {
        ArrayList<Category> sortedCategories = new ArrayList<>(
            this.categories.values().stream().sorted(
                (a, b) -> {
                    return b.getCategoryObject().getLastModified().compareTo(
                        a.getCategoryObject().getLastModified()
                    );
                }
            ).toList()
        );
        
        return sortedCategories;
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
        
        LayoutUtils.createTabGroup(
            "tab-button-active",
            Arrays.asList(
                new Pair<>(tabDashboardButton, tabDashboardPane),
                new Pair<>(tabProductsButton, tabProductsPane),
                new Pair<>(tabCategoriesButton, tabCategoriesPane)
            )
        );
        
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
        
        this.initializeDashboardPage();
        this.initializeProductPage();
        this.initializeCategoryPage();
        
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
