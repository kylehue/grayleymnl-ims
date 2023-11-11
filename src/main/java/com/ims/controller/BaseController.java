package com.ims.controller;

import com.ims.components.*;
import com.ims.database.DBCategories;
import com.ims.database.DBProducts;
import com.ims.model.BaseModel;
import com.ims.model.ProductModel;
import com.ims.model.objects.CategoryObject;
import com.ims.utils.SceneManager;
import io.github.palexdev.materialfx.controls.*;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    
    private void initializeDashboardPage() {
    
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
    
    private void initializeProductPage() {
        LayoutUtils.applyVirtualScrolling(productsScrollPane, productsFlowPane);
        
        addProductButton.setOnMouseClicked((e) -> {
            addProductModal.showModal();
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
            
            DBProducts.add(
                name,
                category.getID(),
                "",
                0,
                0
            );
            
            ProductModel.nameProperty.set(name);
            ProductModel.categoryIDProperty.set(category.getID());
            ProductModel.imageURLProperty.set("");
            ProductModel.priceProperty.set(0);
            ProductModel.currentStocksProperty.set(0);
            ProductModel.expectedStocksProperty.set(0);
            
            SceneManager.setScene("product");
            
            addProductModal.hide();
        });
    }
    
    private TagButton addCategoryTag(String categoryName, boolean isActive) {
        TagButton categoryButton = new TagButton();
        categoryButton.setText(categoryName);
        categoryButton.setActive(isActive);
        productsCategoriesFlowPane.getChildren().add(categoryButton);
        
        return categoryButton;
    }
    
    private Product addProduct(
        String name,
        String category,
        String imageUrl,
        int currentStock,
        int neededStock,
        float price
    ) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setStocks(currentStock, neededStock);
        product.setPrice(price);
        product.setImage(imageUrl);
        productsFlowPane.getChildren().add(product);
        return product;
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
    
    private CategoryDeleteModal deleteCategoryModal = new CategoryDeleteModal();
    
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
                    categories.get(id).setCategoryName(change.getValueAdded().getName());
                } else if (needsToBeRemoved) {
                    removeCategory(id);
                }
            }
        );
        
        LayoutUtils.applyVirtualScrolling(categoriesScrollPane, categoriesFlowPane);
        this.initializeCategoryLazyLoad();
        addCategoryButton.setOnMouseClicked((e) -> {
            addCategoryModal.showModal();
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
                    category.categoryObject.getID(),
                    category.getCategoryName()
                );
            }
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
            
            category.deleteButton.setOnMouseClicked((e) -> {
                deleteCategoryModal.setCategoryName(
                    BaseModel.categoryMap.get(id).getName()
                );
                deleteCategoryModal.deleteButton.setOnMouseClicked((ev) -> {
                    BaseModel.removeCategory(id);
                    deleteCategoryModal.hide();
                });
                deleteCategoryModal.showModal();
            });
            
            category.saveButton.setOnMouseClicked((e) -> {
                if (!category.nameTextFieldValidator.isValid()) {
                    return;
                }
                BaseModel.updateCategory(id, category.getCategoryName());
            });
        });
        
        return category;
    }
    
    private void removeCategory(int id) {
        Category categoryToRemove = this.categories.get(id);
        if (categoryToRemove != null) {
            Platform.runLater(() -> {
                categoriesFlowPane.getChildren().remove(categoryToRemove);
                this.categories.remove(categoryToRemove.categoryObject.getID());
                
            });
        }
    }
    
    private ArrayList<Category> getSortedCategories() {
        ArrayList<Category> sortedCategories = new ArrayList<>(
            this.categories.values().stream().sorted(
                (a, b) -> {
                    return b.categoryObject.getLastModified().compareTo(
                        a.categoryObject.getLastModified()
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
        ctx.setHeaderText("someemail12@gmail.com");
        MFXButton accountSettingsButton = ctx.addButtonItem("My Account");
        MFXButton managerUsersButton = ctx.addButtonItem("Manage Users");
        MFXButton logoutButton = ctx.addButtonItem("Logout");
        ctx.bindToNode(settingsButton);
        LayoutUtils.addIconToButton(logoutButton, "/icons/logout.svg");
        
        logoutButton.setOnMouseClicked((e) -> {
            SceneManager.setScene("login");
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
    }
}
