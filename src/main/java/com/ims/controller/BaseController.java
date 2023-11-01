package com.ims.controller;

import com.ims.components.Category;
import com.ims.components.ContextMenu;
import com.ims.components.Product;
import com.ims.components.TagButton;
import com.ims.model.BaseModel;
import com.ims.model.objects.CategoryObject;
import com.ims.utils.SceneManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.HashMap;

import com.ims.utils.LayoutUtils;

public class BaseController {
    // The container of everything in here
    @FXML
    private GridPane rootContainer;
    
    // The button used to access the dashboard or analytics page
    @FXML
    private MFXButton tabDashboardButton;
    
    // The container of the dashboard or analytics page
    @FXML
    private GridPane tabDashboardPane;
    
    // The button used to access the products page
    @FXML
    private MFXButton tabProductsButton;
    
    // The container of the products page
    @FXML
    private GridPane tabProductsPane;
    
    // The button used to access the categories page
    @FXML
    private MFXButton tabCategoriesButton;
    
    // The container of the categories page
    @FXML
    private GridPane tabCategoriesPane;
    
    // The settings button on the top right area
    @FXML
    private MFXButton settingsButton;
    
    // The container of the analytics cards in dashboard page
    @FXML
    private FlowPane analyticsFlowPane;
    
    // The container of the product cards in products page
    @FXML
    private FlowPane productsFlowPane;
    
    // The container of the category chip buttons in products page
    @FXML
    private FlowPane productsCategoriesFlowPane;
    
    // The container of the category cards in categories page
    @FXML
    private FlowPane categoriesFlowPane;
    
    // The search text field in products page
    @FXML
    private MFXTextField searchProductTextField;
    
    // The button used to save all categories
    @FXML
    private MFXButton saveAllCategoriesButton;
    
    // The button used to add a category
    @FXML
    private MFXButton addCategoryButton;
    
    private HashMap<Integer, Category> categories = new HashMap<>();
    
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
        
        BaseModel.categoriesProperty.addListener((MapChangeListener) change -> this.handleCategoryChange(change));
        
        
        // this.addCategoryTag("All", true);
        // this.addCategoryTag("Dog", false);
        // this.addCategoryTag("Cat", false);
        // this.addCategoryTag("Bird", false);
        // this.addCategoryTag("Cat Food", false);
        // this.addCategoryTag("Dog Food", false);
        // this.addCategoryTag("Bird Food", false);
        // this.addCategory("hello");
        // this.addCategory("uhuh");
        // this.addCategory("test");
        // this.addCategory("hello");
        // this.addCategory("uhuh");
        // this.addCategory("test");
        //
        // for (int i = 0; i < 12; i++) {
        //     this.addProduct(
        //         "Some Cat",
        //         "Cat",
        //         "https://i0.wp.com/suddenlycat.com/wp-content/uploads/2020/09/b31.jpg?resize=680%2C839&ssl=1",
        //         i * 4,
        //         i * 7,
        //         i * 2.99f
        //     );
        // }
        
        ContextMenu ctx = new ContextMenu();
        ctx.bindToNode(settingsButton);
        // TODO: change this based on user's current session
        ctx.setHeaderText("someemail12@gmail.com");
        MFXButton accountSettingsButton = ctx.addButtonItem("My Account");
        MFXButton managerUsersButton = ctx.addButtonItem("Manage Users");
        MFXButton logoutButton = ctx.addButtonItem("Logout");
        LayoutUtils.addIconToButton(logoutButton, "/icons/logout.svg");
        
        logoutButton.setOnMouseClicked((e) -> {
            this.goBack();
        });
        
        accountSettingsButton.setOnMouseClicked((e) -> {
            SceneManager.setScene("account-settings");
        });
        
        managerUsersButton.setOnMouseClicked((e) -> {
            SceneManager.setScene("user-manager");
        });
        
        BaseModel.loadCategories(12);
        addCategoryButton.setOnMouseClicked((e) -> {
            BaseModel.addCategory("Unnamed Category");
        });
        
        saveAllCategoriesButton.setOnMouseClicked((e) -> {
            for (Category category : this.categories.values()) {
                BaseModel.updateCategory(
                    category.getCategoryID(),
                    category.getCategoryName()
                );
            }
        });
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
            this.addCategory(id, value.getName());
        } else if (needsToBeUpdated) {
            CategoryObject value = change.getValueAdded();
            this.categories.get(id).setCategoryName(value.getName());
        } else if (needsToBeRemoved) {
            this.removeCategory(id);
        }
    }
    
    private TagButton addCategoryTag(String categoryName, boolean isActive) {
        TagButton categoryButton = new TagButton();
        categoryButton.setText(categoryName);
        categoryButton.setActive(isActive);
        productsCategoriesFlowPane.getChildren().add(categoryButton);
        
        return categoryButton;
    }
    
    private Category addCategory(int id, String name) {
        Category category = new Category(id, name);
        categoriesFlowPane.getChildren().add(category);
        this.categories.put(id, category);
        
        category.deleteButton.setOnMouseClicked((e) -> {
            BaseModel.removeCategory(category.getCategoryID());
        });
        
        category.saveButton.setOnMouseClicked((e) -> {
            BaseModel.updateCategory(id, category.getCategoryName());
        });
        
        return category;
    }
    
    private void removeCategory(int id) {
        Category categoryToRemove = this.categories.get(id);
        if (categoryToRemove != null) {
            categoriesFlowPane.getChildren().remove(categoryToRemove);
            this.categories.remove(categoryToRemove.getCategoryID());
        }
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
    
    @FXML
    public void goBack() {
        SceneManager.setScene("login");
    }
}
