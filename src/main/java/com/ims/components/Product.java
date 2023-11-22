package com.ims.components;

import com.ims.model.BaseModel;
import com.ims.model.ProductModel;
import com.ims.model.UserSessionModel;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.ProductObject;
import com.ims.utils.LayoutUtils;
import com.ims.utils.SceneManager;
import com.ims.utils.Transition;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

public class Product extends GridPane {
    private ProductObject productObject;
    private final ObservableList<String> styleClass = this.getStyleClass();
    
    private final GridPane textGridPane = LayoutUtils.createGridPane(4, 1);
    private final ImageView imgView = new ImageView();
    
    private final Label nameLabel = new Label();
    private final Label categoryLabel = new Label();
    private final Label stocksLabel = new Label();
    private final Label priceLabel = new Label();
    private final MFXButton editButton = new MFXButton();
    
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty imageURL = new SimpleStringProperty();
    private final IntegerProperty categoryID = new SimpleIntegerProperty();
    private final IntegerProperty currentStocks = new SimpleIntegerProperty();
    private final IntegerProperty expectedStocks = new SimpleIntegerProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    
    public Product() {
        this.styleClass.add("card");
        this.styleClass.add("product-container");
        
        // Setup main row and columns
        LayoutUtils.setupGridPane(this, 1, 2);
        
        // Setup grid pane for the details
        GridPane detailsGridPane = LayoutUtils.createGridPane(2, 1);
        this.add(detailsGridPane, 1, 0);
        
        // Setup grid pane for text
        detailsGridPane.add(this.textGridPane, 0, 0);
        ObservableList<RowConstraints> textRows = textGridPane.getRowConstraints();
        for (RowConstraints textRow : textRows) {
            textRow.setPrefHeight(USE_COMPUTED_SIZE);
            textRow.setVgrow(Priority.NEVER);
        }
        textGridPane.getStyleClass().add("product-text-container");
        
        // Setup grid pane for controls
        GridPane controlGridPane = LayoutUtils.createGridPane(1, 1);
        detailsGridPane.add(controlGridPane, 0, 1);
        RowConstraints controlRow = controlGridPane.getRowConstraints().get(0);
        controlRow.setFillHeight(false);
        controlRow.setValignment(VPos.BOTTOM);
        FlowPane controlFlowPane = new FlowPane();
        controlFlowPane.setAlignment(Pos.BOTTOM_RIGHT);
        controlGridPane.add(controlFlowPane, 0, 0);
        
        // Setup buttons
        editButton.setText("");
        LayoutUtils.addIconToButton(editButton, "/icons/pencil.svg");
        editButton.getStyleClass().add("icon-button");
        controlFlowPane.getChildren().add(editButton);
        
        editButton.setOnMouseClicked(e -> {
            ProductModel.currentProduct.set(productObject);
            SceneManager.setScene("product");
        });
        
        Transition.fadeUp(this, 150);
        
        // Setup image
        StackPane imgContainer = new StackPane(imgView);
        Rectangle rectClip = new Rectangle(0, 0, 0, 0);
        rectClip.setArcWidth(10);
        rectClip.setArcHeight(10);
        rectClip.widthProperty().bind(imgContainer.widthProperty());
        rectClip.heightProperty().bind(imgContainer.heightProperty());
        
        imgView.setPreserveRatio(true);
        imgView.getStyleClass().add("product-image");
        
        imgContainer.setAlignment(Pos.CENTER);
        imgContainer.setClip(rectClip);
        
        this.widthProperty().addListener(($1, $2, $3) -> {
            Platform.runLater(() -> {
                Insets insets = this.getInsets();
                double innerHeight = this.getHeight()
                    - insets.getTop()
                    - insets.getBottom();
                imgView.setFitHeight(innerHeight);
                imgContainer.setPrefHeight(innerHeight);
                imgContainer.setPrefWidth(Math.max(this.getWidth() / 3, 150));
                imgContainer.setMinWidth(Math.max(this.getWidth() / 3, 150));
                imgContainer.setMaxWidth(Math.max(this.getWidth() / 3, 150));
            });
        });
        
        this.add(imgContainer, 0, 0);
        this.setImageURL(
            getClass().getResource("/images/image-placeholder.png").toExternalForm()
        );
        
        // Setup labels
        this.textGridPane.add(nameLabel, 0, 0);
        this.textGridPane.add(categoryLabel, 0, 1);
        this.textGridPane.add(stocksLabel, 0, 2);
        this.textGridPane.add(priceLabel, 0, 3);
        priceLabel.getStyleClass().add("product-price-label");
        stocksLabel.getStyleClass().add("product-stocks-label");
        nameLabel.getStyleClass().add("product-name-label");
        categoryLabel.getStyleClass().add("product-category-label");
    }
    
    private boolean propertyListenersInitialized = false;
    private void initializePropertyListeners() {
        if (propertyListenersInitialized) return;
        propertyListenersInitialized = true;
        this.nameProperty().addListener(e -> {
            this.setName(this.nameProperty().get());
        });
        
        this.imageURLProperty().addListener(e -> {
            this.setImageURL(this.imageURLProperty().get());
        });
        
        this.categoryIDProperty().addListener(e -> {
            this.setCategoryID(this.categoryIDProperty().get());
        });
        
        this.currentStocksProperty().addListener(e -> {
            this.setStocks(
                this.currentStocksProperty().get(),
                this.expectedStocksProperty().get()
            );
        });
        
        this.expectedStocksProperty().addListener(e -> {
            this.setStocks(
                this.currentStocksProperty().get(),
                this.expectedStocksProperty().get()
            );
        });
        
        this.priceProperty().addListener(e -> {
            this.setPrice(this.priceProperty().get());
        });
        
        updateEditPermissions(UserSessionModel.currentUserIsAllowEditProduct());
        UserSessionModel.currentUser.addListener(e -> {
            updateEditPermissions(UserSessionModel.currentUserIsAllowEditProduct());
        });
    }
    
    private void updateEditPermissions(boolean isAllowed) {
        editButton.setVisible(isAllowed);
        editButton.setManaged(isAllowed);
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    private void setName(String name) {
        Platform.runLater(() -> {
            nameLabel.setText(name);
        });
    }
    
    public StringProperty imageURLProperty() {
        return imageURL;
    }

    private void setImageURL(String imageUrl) {
        if (imageUrl == null) return;
        if (imageUrl.isEmpty()) return;
        
        Platform.runLater(() -> {
            Image img = new Image(imageUrl, true);
            this.imgView.setImage(img);
        });
    }
    
    private final ChangeListener<String> categoryChangeListener = (
        e,
        oldValue,
        newValue
    ) -> {
        Platform.runLater(() -> {
            categoryLabel.setText(newValue);
        });
    };

    private CategoryObject oldCategoryObject = null;
    
    public IntegerProperty categoryIDProperty() {
        return categoryID;
    }
    
    private void setCategoryID(int categoryID) {
        CategoryObject categoryObject = BaseModel.loadAndGetCategory(categoryID);
        if (categoryObject == null) return;
        Platform.runLater(() -> {
            categoryLabel.setText(categoryObject.getName());
        });
        if (oldCategoryObject != null) {
            oldCategoryObject.nameProperty().removeListener(categoryChangeListener);
        }
        categoryObject.nameProperty().addListener(categoryChangeListener);
        oldCategoryObject = categoryObject;
    }
    
    public IntegerProperty expectedStocksProperty() {
        return expectedStocks;
    }
    
    public IntegerProperty currentStocksProperty() {
        return currentStocks;
    }
    
    private void setStocks(int current, int max) {
        Platform.runLater(() -> {
            stocksLabel.setText("In stock: " + current + "/" + max);
            stocksLabel.setAlignment(Pos.TOP_LEFT);
            stocksLabel.setWrapText(true);
            this.heightProperty().addListener(($1, $2, $3) -> {
                stocksLabel.setMaxHeight(this.getHeight() / 3);
            });
        });
    }
    
    public DoubleProperty priceProperty() {
        return price;
    }
    
    private void setPrice(double price) {
        Platform.runLater(() -> {
            priceLabel.setText("₱" + String.format("%.2f", price));
        });
    }
    
    public void setProductObject(ProductObject productObject) {
        initializePropertyListeners();
        this.productObject = productObject;
        this.nameProperty().unbind();
        this.nameProperty().bind(productObject.nameProperty());
        this.currentStocksProperty().unbind();
        this.currentStocksProperty().bind(productObject.currentStocksProperty());
        this.expectedStocksProperty().unbind();
        this.expectedStocksProperty().bind(productObject.expectedStocksProperty());
        this.categoryIDProperty().unbind();
        this.categoryIDProperty().bind(productObject.categoryIDProperty());
        this.imageURLProperty().unbind();
        this.imageURLProperty().bind(productObject.imageURLProperty());
        this.priceProperty().unbind();
        this.priceProperty().bind(productObject.priceProperty());
    }
    
    public ProductObject getProductObject() {
        return productObject;
    }
}
