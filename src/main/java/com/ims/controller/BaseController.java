package com.ims.controller;

import com.ims.components.*;
import com.ims.model.BaseModel;
import com.ims.model.objects.CategoryObject;
import com.ims.utils.SceneManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.HashMap;

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
    private MFXScrollPane tabCategoriesScrollPane;
    
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
        BaseModel.categoriesProperty.addListener(
            (MapChangeListener) change -> this.handleCategoryChange(change)
        );
        
        this.initializeCategoryLazyLoad();
        addCategoryButton.setOnMouseClicked((e) -> {
            addCategoryModal.show(SceneManager.getStage());
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
                    category.getCategoryID(),
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
        tabCategoriesScrollPane.vvalueProperty().addListener(($1, $2, scrollValue) -> {
            if (scrollValue.doubleValue() == 1.0) {
                BaseModel.loadCategories(12);
            }
        });
        
        // The listener above won't work if there is no scrollbar.
        // So here, we add components until the scroll pane gets a scrollbar.
        tabCategoriesScrollPane.viewportBoundsProperty().addListener(($1, $2, newValue) -> {
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
    
    private void handleCategoryChange(
        MapChangeListener.Change<Integer, CategoryObject> change
    ) {
        int id = change.getKey();
        boolean isAddedAlready = this.categories.get(id) != null;
        boolean needsToBeAdded = change.wasAdded() && !isAddedAlready;
        boolean needsToBeUpdated = change.wasAdded() && isAddedAlready;
        boolean needsToBeRemoved = change.wasRemoved() && isAddedAlready;
        
        if (needsToBeAdded) {
            CategoryObject value = change.getValueAdded();
            addCategory(id, value.getName());
        } else if (needsToBeUpdated) {
            CategoryObject value = change.getValueAdded();
            categories.get(id).setCategoryName(value.getName());
        } else if (needsToBeRemoved) {
            removeCategory(id);
        }
    }
    
    private Category addCategory(int id, String name) {
        Category category = new Category(id, name);
        
        Platform.runLater(() -> {
            categoriesFlowPane.getChildren().add(category);
            this.categories.put(id, category);
            
            category.deleteButton.setOnMouseClicked((e) -> {
                deleteCategoryModal.setCategoryName(
                    BaseModel.getCategoryById(id).getName()
                );
                deleteCategoryModal.deleteButton.setOnMouseClicked((ev) -> {
                    BaseModel.removeCategory(category.getCategoryID());
                    deleteCategoryModal.hide();
                });
                deleteCategoryModal.show(SceneManager.getStage());
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
                this.categories.remove(categoryToRemove.getCategoryID());
                
            });
        }
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
    
    // The container of the product cards in products page
    @FXML
    private FlowPane productsFlowPane;
    
    // The container of the category chip buttons in products page
    @FXML
    private FlowPane productsCategoriesFlowPane;
    
    // The search text field in products page
    @FXML
    private MFXTextField searchProductTextField;
    
    private void initializeProductPage() {
    
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
        ctx.bindToNode(settingsButton);
        // TODO: change this based on user's current session
        ctx.setHeaderText("someemail12@gmail.com");
        MFXButton accountSettingsButton = ctx.addButtonItem("My Account");
        MFXButton managerUsersButton = ctx.addButtonItem("Manage Users");
        MFXButton logoutButton = ctx.addButtonItem("Logout");
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
