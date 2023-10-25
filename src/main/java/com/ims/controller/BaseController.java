package com.ims.controller;

import com.ims.components.Product;
import com.ims.components.TagButton;
import com.ims.utils.SceneManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;

import com.ims.utils.Utils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

public class BaseController {
    @FXML
    private GridPane rootContainer;
    
    @FXML
    private MFXButton tabDashboardButton;
    
    @FXML
    private MFXButton tabProductsButton;
    
    @FXML
    private MFXButton tabCategoriesButton;
    
    @FXML
    private MFXButton settingsButton;
    
    @FXML
    private GridPane tabDashboardPane;
    
    @FXML
    private GridPane tabProductsPane;
    
    @FXML
    private GridPane tabCategoriesPane;
    
    @FXML
    private FlowPane analyticsFlowPane;
    
    @FXML
    private FlowPane productsFlowPane;
    
    @FXML
    private FlowPane productsCategoriesFlowPane;
    
    private HashMap<String, String> categories = new HashMap<>();
    
    @FXML
    public void initialize()
        throws URISyntaxException, ParserConfigurationException, IOException, SAXException, InterruptedException {
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
        
        this.createCategory("All", true);
        this.createCategory("Dog", false);
        this.createCategory("Cat", false);
        this.createCategory("Bird", false);
        
        Product prod = new Product();
        productsFlowPane.getChildren().add(prod);
    }
    
    private MFXButton createCategory(String categoryName, boolean isActive) {
        TagButton categoryButton = new TagButton();
        categoryButton.setText(categoryName);
        categoryButton.setActive(isActive);
        productsCategoriesFlowPane.getChildren().add(categoryButton);
        
        return categoryButton;
    }
    
    @FXML
    public void goBack() {
        SceneManager.setScene("login");
    }
}
