package com.ims.controller;

import com.ims.components.Product;
import com.ims.components.TagButton;
import com.ims.utils.SceneManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Arrays;

import com.ims.utils.Utils;

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
    
    @FXML
    public void initialize() {
        Utils.addIconToButton(tabDashboardButton, "/icons/home.svg");
        Utils.addIconToButton(tabProductsButton, "/icons/paw.svg");
        Utils.addIconToButton(tabCategoriesButton, "/icons/shape.svg");
        
        Utils.createTabGroup(
            "tab-button-active",
            Arrays.asList(
                new Pair<>(tabDashboardButton, tabDashboardPane),
                new Pair<>(tabProductsButton, tabProductsPane),
                new Pair<>(tabCategoriesButton, tabCategoriesPane)
            )
        );
        
        Utils.createResponsiveFlowPane(
            analyticsFlowPane,
            350,
            2.05,
            true
        );
        Utils.createResponsiveFlowPane(
            productsFlowPane,
            350,
            2.05,
            false
        );
        
        this.addCategory("All", true);
        this.addCategory("Dog", false);
        this.addCategory("Cat", false);
        this.addCategory("Bird", false);
        this.addCategory("Howls", false);
        this.addCategory("Cat Food", false);
        this.addCategory("Dog Food", false);
        this.addCategory("Bird Food", false);
        
        for (int i = 0; i < 12; i++) {
            this.addProduct(
                "Some Cat",
                "Cat",
                "Meow meow meow meow meow meow meow, meow meow. Meow meow meow meow! meow meow meow, meow, meow. Meow meow meow meow.",
                "https://i0.wp.com/suddenlycat.com/wp-content/uploads/2020/09/b31.jpg?resize=680%2C839&ssl=1",
                2.99f
            );
        }
    }
    
    private TagButton addCategory(String categoryName, boolean isActive) {
        TagButton categoryButton = new TagButton();
        categoryButton.setText(categoryName);
        categoryButton.setActive(isActive);
        productsCategoriesFlowPane.getChildren().add(categoryButton);
        
        return categoryButton;
    }
    
    private Product addProduct(
        String name,
        String category,
        String description,
        String imageUrl,
        float price
    ) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setDescription(description);
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
