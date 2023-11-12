package com.ims.controller;

import com.ims.components.CategoryComboBox;
import com.ims.components.NumberField;
import com.ims.model.BaseModel;
import com.ims.model.ProductModel;
import com.ims.utils.SceneManager;
import com.ims.utils.LayoutUtils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.io.File;
import java.util.Arrays;

public class ProductController {
    @FXML
    MFXButton tabGeneralButton;
    
    @FXML
    GridPane tabGeneralPane;
    
    @FXML
    MFXButton tabStocksButton;
    
    @FXML
    GridPane tabStocksPane;
    
    @FXML
    MFXButton tabOthersButton;
    
    @FXML
    GridPane tabOthersPane;
    
    @FXML
    MFXButton backButton;
    
    @FXML
    VBox stocksPaneFieldsContainer;
    
    @FXML
    VBox generalPaneFieldsContainer;
    
    @FXML
    MFXTextField productNameTextField;
    
    NumberField productPriceNumberField = new NumberField(false);
    
    CategoryComboBox productCategoryComboBox = new CategoryComboBox();
    
    @FXML
    ImageView productImageView;
    
    @FXML
    MFXButton uploadImageButton;
    
    @FXML
    MFXTextField productImageURLTextField;
    
    NumberField currentStocksNumberField = new NumberField(true);
    
    NumberField expectedStocksNumberField = new NumberField(true);
    
    @FXML
    public void initialize() {
        LayoutUtils.addIconToButton(backButton, "/icons/arrow-left.svg");
        backButton.getStyleClass().add("icon-button");
        backButton.setText("");
        
        LayoutUtils.createTabGroup(
            "tab-button-active",
            Arrays.asList(
                new Pair<>(tabGeneralButton, tabGeneralPane),
                new Pair<>(tabStocksButton, tabStocksPane),
                new Pair<>(tabOthersButton, tabOthersPane)
            )
        );
        
        uploadImageButton.setText("");
        uploadImageButton.getStyleClass().add("icon-button");
        LayoutUtils.addIconToButton(
            uploadImageButton,
            "/icons/upload.svg"
        );
        
        uploadImageButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
            FileChooser fileChooser = new FileChooser();
            
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(
                "Image files (*.png, *.jpg, *.jpeg, *.gif)",
                "*.png",
                "*.jpg",
                "*.jpeg",
                "*.gif"
            );
            
            fileChooser.getExtensionFilters().add(extensionFilter);
            
            File file = fileChooser.showOpenDialog(SceneManager.getStage());
            
            if (file != null) {
                String imageUrl = file.toURI().toString();
                Image image = new Image(imageUrl);
                productImageView.setImage(image);
            }
        });
        
        currentStocksNumberField.textField.setFloatingText("Current # of Stocks");
        currentStocksNumberField.setAllowDecimal(false);
        currentStocksNumberField.setAllowNegative(false);
        currentStocksNumberField.textField.setMinWidth(100);
        currentStocksNumberField.textField.setMaxWidth(300);
        expectedStocksNumberField.textField.setFloatingText("Needed # of Stocks");
        expectedStocksNumberField.setAllowDecimal(false);
        expectedStocksNumberField.setAllowNegative(false);
        expectedStocksNumberField.textField.setMinWidth(100);
        expectedStocksNumberField.textField.setMaxWidth(300);
        stocksPaneFieldsContainer.getChildren().addAll(
            currentStocksNumberField,
            expectedStocksNumberField
        );
        generalPaneFieldsContainer.getChildren().add(
            1,
            productPriceNumberField
        );
        productPriceNumberField.textField.setFloatingText("Price");
        productPriceNumberField.textField.setMinWidth(100);
        productPriceNumberField.textField.setMaxWidth(300);
        productPriceNumberField.setAllowNegative(false);
        generalPaneFieldsContainer.getChildren().add(
            2,
            productCategoryComboBox
        );
        productCategoryComboBox.setMinWidth(100);
        productCategoryComboBox.setMaxWidth(300);
        
        ProductModel.nameProperty.addListener(($1, $2, name) -> {
            productNameTextField.setText(name);
        });
        
        ProductModel.priceProperty.addListener(($1, $2, price) -> {
            productPriceNumberField.textField.setText(String.format(
                "%.2f", price.doubleValue()
            ));
        });
        
        ProductModel.categoryIDProperty.addListener(($1, $2, categoryID) -> {
            productCategoryComboBox.setValue(BaseModel.loadAndGetCategory(
                categoryID.intValue()
            ));
        });
        
        ProductModel.imageURLProperty.addListener(($1, $2, imageURL) -> {
            if (imageURL.isEmpty()) return;
            productImageURLTextField.setText(imageURL);
            productImageView.setImage(new Image(imageURL));
        });
        
        ProductModel.currentStocksProperty.addListener(
            ($1, $2, currentStocks) -> {
                currentStocksNumberField.textField.setText(
                    currentStocks.toString()
                );
            }
        );
        
        ProductModel.expectedStocksProperty.addListener(
            ($1, $2, expectedStocks) -> {
                expectedStocksNumberField.textField.setText(
                    expectedStocks.toString()
                );
            }
        );
    }
    
    @FXML
    public void goBack() {
        SceneManager.setScene("base");
    }
}
