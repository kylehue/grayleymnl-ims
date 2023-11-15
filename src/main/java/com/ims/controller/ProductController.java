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
    MFXButton saveAllButton;
    
    @FXML
    MFXButton cancelButton;
    
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
        
        productImageURLTextField.setStyle("-fx-padding: 5 40 5 0 !important");
        
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
                String imageURL = file.toURI().toString();
                productImageURLTextField.setText(imageURL);
            }
        });
        
        productImageURLTextField.textProperty().addListener(
            ($1, $2, imageURL) -> {
                if (imageURL == null) return;
                if (imageURL.isEmpty()) return;
                Image image = new Image(imageURL);
                productImageView.setImage(image);
            }
        );
        
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
        
        ProductModel.currentProduct.addListener(($1, $2, currentProduct) -> {
            String name = currentProduct.getName();
            if (name == null || name.isEmpty()) {
                productNameTextField.setText("");
            } else {
                productNameTextField.setText(name);
            }
            
            double price = currentProduct.getPrice();
            productPriceNumberField.textField.setText(String.format(
                "%.2f", price
            ));
            
            int categoryID = currentProduct.getCategoryID();
            productCategoryComboBox.setValue(BaseModel.loadAndGetCategory(
                categoryID
            ));
            
            String imageURL = currentProduct.getImageURL();
            if (imageURL == null || imageURL.isEmpty()) {
                productImageURLTextField.setText("");
                productImageView.setImage(
                    new Image(
                        getClass().getResource(
                            "/images/image-placeholder.png"
                        ).toExternalForm()
                    )
                );
            } else {
                productImageURLTextField.setText(imageURL);
                productImageView.setImage(new Image(imageURL));
            }
            
            int currentStocks = currentProduct.getCurrentStocks();
            currentStocksNumberField.textField.setText(
                String.valueOf(currentStocks)
            );
            
            int expectedStocks = currentProduct.getExpectedStocks();
            expectedStocksNumberField.textField.setText(
                String.valueOf(expectedStocks)
            );
        });
        
        saveAllButton.setOnMouseClicked(e -> {
            BaseModel.updateProduct(
                ProductModel.currentProduct.get().getID(),
                productNameTextField.getText(),
                Double.parseDouble(productPriceNumberField.textField.getText()),
                productCategoryComboBox.getValue().getID(),
                productImageURLTextField.getText(),
                Integer.parseInt(currentStocksNumberField.textField.getText()),
                Integer.parseInt(expectedStocksNumberField.textField.getText())
            );
        });
        
        backButton.setOnMouseClicked(e -> {
            goBack();
        });
        
        cancelButton.setOnMouseClicked(e -> {
            goBack();
        });
    }
    
    @FXML
    public void goBack() {
        SceneManager.setScene("base");
        ProductModel.clearState();
    }
}
