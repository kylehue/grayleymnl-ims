package com.ims.controller;

import com.ims.components.CategoryComboBox;
import com.ims.components.NumberField;
import com.ims.components.PopupService;
import com.ims.model.BaseModel;
import com.ims.model.ProductModel;
import com.ims.model.UserSessionModel;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.ProductObject;
import com.ims.utils.SceneManager;
import com.ims.utils.LayoutUtils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
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
    MFXButton deleteProductButton;
    
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
        
        currentStocksNumberField.maxValueProperty().bind(
            expectedStocksNumberField.valueProperty()
        );
        expectedStocksNumberField.minValueProperty().bind(
            currentStocksNumberField.valueProperty()
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
                Image image = new Image(imageURL, true);
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
            updateContents(currentProduct);
        });
        
        saveAllButton.setOnMouseClicked(e -> {
            CategoryObject selectedCategory = productCategoryComboBox.getValue();
            int productID = ProductModel.currentProduct.get().getID();
            BaseModel.updateProduct(
                productID,
                productNameTextField.getText(),
                Double.parseDouble(productPriceNumberField.textField.getText()),
                selectedCategory == null ? null : selectedCategory.getID(),
                productImageURLTextField.getText(),
                Integer.parseInt(currentStocksNumberField.textField.getText()),
                Integer.parseInt(expectedStocksNumberField.textField.getText())
            );
            
            PopupService.messageDialog.setup(
                "Product Updated",
                "Product has been successfully updated.",
                "Got it!"
            ).show();
        });
        
        backButton.setOnMouseClicked(e -> {
            if (!isSaved()) {
                PopupService.confirmDialog.setup(
                    "Unsaved Changes",
                    "There are some unsaved changes, are you sure you want to exit?",
                    "Exit",
                    true,
                    () -> {
                        goBack();
                    }
                ).show();
            } else {
                goBack();
            }
        });
        
        cancelButton.setOnMouseClicked(e -> {
            goBack();
        });
        
        deleteProductButton.setOnMouseClicked(e -> {
            PopupService.confirmDialog.setup(
                "Delete Product",
                "Are you sure you want to delete this product?",
                "Delete",
                true,
                () -> {
                    BaseModel.removeProduct(
                        ProductModel.currentProduct.get().getID()
                    );
                    goBack();
                    
                    PopupService.messageDialog.setup(
                        "Delete Product",
                        "Product has been deleted.",
                        "Got it!"
                    ).show();
                }
            ).show();
        });
        
        updateDeletePermissions(UserSessionModel.currentUserIsAllowDeleteProduct());
        UserSessionModel.currentUser.addListener(e -> {
            updateDeletePermissions(UserSessionModel.currentUserIsAllowDeleteProduct());
        });
        
        SceneManager.onChangeScene((currentScene, oldScene) -> {
            if (!currentScene.equals("product")) return;
            updateContents(ProductModel.currentProduct.get());
        });
    }
    
    private boolean isSaved() {
        ProductObject productObject = ProductModel.currentProduct.get();
        CategoryObject selectedCategory = productCategoryComboBox.getValue();
        return productNameTextField.getText().equals(productObject.getName()) &&
            productPriceNumberField.getValue() == productObject.getPrice() &&
            (
                (selectedCategory == null && productObject.getCategoryID() == null) || (selectedCategory != null && productObject.getCategoryID() != null && selectedCategory.getID() == productObject.getCategoryID())
            )
            &&
            productImageURLTextField.getText().equals(productObject.getImageURL()) &&
            currentStocksNumberField.getValue() == productObject.getCurrentStocks() &&
            expectedStocksNumberField.getValue() == productObject.getExpectedStocks();
    }
    
    private void updateDeletePermissions(boolean isAllowed) {
        deleteProductButton.setDisable(!isAllowed);
    }
    
    @FXML
    public void goBack() {
        SceneManager.setScene("base");
    }
    
    private void updateContents(ProductObject currentProduct) {
        Platform.runLater(() -> {
            String name = currentProduct.getName();
            if (name == null || name.isEmpty()) {
                productNameTextField.setText("");
            } else {
                productNameTextField.setText(name);
            }
            
            Double price = currentProduct.getPrice();
            productPriceNumberField.setValue(price);
            
            Integer categoryID = currentProduct.getCategoryID();
            if (categoryID == null) {
                productCategoryComboBox.setValue(null);
            } else {
                productCategoryComboBox.setValue(BaseModel.loadAndGetCategory(
                    categoryID
                ));
            }
            
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
                productImageView.setImage(new Image(imageURL, true));
            }
            
            Integer currentStocks = currentProduct.getCurrentStocks();
            currentStocksNumberField.textField.setText(
                String.valueOf(currentStocks)
            );
            
            Integer expectedStocks = currentProduct.getExpectedStocks();
            expectedStocksNumberField.textField.setText(
                String.valueOf(expectedStocks)
            );
        });
    }
}
