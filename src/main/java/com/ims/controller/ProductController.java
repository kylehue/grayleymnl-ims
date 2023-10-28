package com.ims.controller;

import com.ims.components.NumberField;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
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
    MFXTextField productNameTextField;
    
    @FXML
    MFXTextField productPriceTextField;
    
    @FXML
    MFXComboBox productCategoryComboBox;
    
    @FXML
    ImageView productImageView;
    
    @FXML
    MFXButton uploadImageButton;
    
    @FXML
    VBox stocksPaneFieldsContainer;
    
    NumberField currentStocksNumberField = new NumberField();
    
    NumberField expectedStocksNumberField = new NumberField();
    
    @FXML
    public void initialize() {
        Utils.addIconToButton(backButton, "/icons/arrow-left.svg");
        backButton.getStyleClass().add("icon-button");
        backButton.setText("");
        
        Utils.createTabGroup(
            "tab-button-active",
            Arrays.asList(
                new Pair<>(tabGeneralButton, tabGeneralPane),
                new Pair<>(tabStocksButton, tabStocksPane),
                new Pair<>(tabOthersButton, tabOthersPane)
            )
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
        expectedStocksNumberField.textField.setFloatingText("Needed # of Stocks");
        expectedStocksNumberField.setAllowDecimal(false);
        expectedStocksNumberField.setAllowNegative(false);
        stocksPaneFieldsContainer.getChildren().addAll(
            currentStocksNumberField,
            expectedStocksNumberField
        );
    }
    
    @FXML
    public void goBack() {
        SceneManager.setScene("base");
    }
}
