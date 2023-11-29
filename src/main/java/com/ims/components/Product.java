package com.ims.components;

import com.ims.Config;
import com.ims.model.BaseModel;
import com.ims.model.ProductModel;
import com.ims.model.UserSessionModel;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.ProductObject;
import com.ims.utils.LayoutUtils;
import com.ims.utils.SceneManager;
import com.ims.utils.Transition;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import io.github.palexdev.mfxcore.collections.Grid;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Product extends GridPane {
    private ProductObject productObject;
    private final ObservableList<String> styleClass = this.getStyleClass();
    
    private final GridPane textGridPane = LayoutUtils.createGridPane(3, 1);
    private final ImageView imgView = new ImageView();
    private Image img = null;
    private final StackPane imgContainer = new StackPane(imgView);
    private final MFXProgressSpinner imgProgressSpinner = new MFXProgressSpinner();
    
    private final Label nameLabel = new Label();
    private final Label categoryLabel = new Label();
    private final Label stocksLabel = new Label();
    private final Label priceLabel = new Label();
    private final MFXButton editButton = new MFXButton();
    
    private final ObjectProperty<String> name = new SimpleObjectProperty<>();
    private final ObjectProperty<String> imageURL = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> categoryID = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> currentStocks = new SimpleObjectProperty<>(0);
    private final ObjectProperty<Integer> expectedStocks = new SimpleObjectProperty<>(0);
    private final ObjectProperty<Double> price = new SimpleObjectProperty<>();
    private static final HashMap<String, Image> cachedImages = new HashMap<>();
    
    public Product() {
        this.styleClass.add("card");
        this.styleClass.add("product-container");
        
        // Setup main row and columns
        this.setHgap(10);
        this.setVgap(10);
        LayoutUtils.setupGridPane(this, 1, 2);
        
        // Setup grid pane for the details
        GridPane detailsGridPane = LayoutUtils.createGridPane(2, 1);
        this.add(detailsGridPane, 1, 0);
        detailsGridPane.getRowConstraints().get(0).setVgrow(Priority.ALWAYS);
        
        // Setup grid pane for text
        detailsGridPane.add(this.textGridPane, 0, 0);
        ObservableList<RowConstraints> textRows = textGridPane.getRowConstraints();
        for (RowConstraints textRow : textRows) {
            textRow.setPrefHeight(USE_COMPUTED_SIZE);
            textRow.setVgrow(Priority.NEVER);
        }
        textGridPane.getStyleClass().add("product-text-container");
        
        // Setup grid pane for controls
        GridPane controlGridPane = LayoutUtils.createGridPane(1, 2);
        detailsGridPane.add(controlGridPane, 0, 1);
        RowConstraints controlRow = controlGridPane.getRowConstraints().get(0);
        controlRow.setFillHeight(false);
        controlRow.setValignment(VPos.BOTTOM);
        FlowPane controlFlowPane = new FlowPane();
        controlFlowPane.setAlignment(Pos.BOTTOM_RIGHT);
        controlGridPane.add(controlFlowPane, 1, 0);
        
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
        Rectangle rectClip = new Rectangle(0, 0, 0, 0);
        rectClip.setArcWidth(10);
        rectClip.setArcHeight(10);
        rectClip.widthProperty().bind(imgContainer.widthProperty());
        rectClip.heightProperty().bind(imgContainer.heightProperty());
        
        imgView.setPreserveRatio(true);
        imgView.getStyleClass().add("product-image");
        
        imgContainer.setAlignment(Pos.CENTER);
        imgContainer.setClip(rectClip);
        imgProgressSpinner.setVisible(false);
        imgContainer.getChildren().add(0, imgProgressSpinner);
        
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
        this.setToDefaultImage();
        
        // Setup labels
        nameLabel.getStyleClass().add("product-name-label");
        this.textGridPane.add(nameLabel, 0, 0);
        this.textGridPane.getRowConstraints().get(0).setVgrow(Priority.NEVER);
        
        categoryLabel.getStyleClass().add("product-category-label");
        this.textGridPane.add(categoryLabel, 0, 1);
        this.textGridPane.getRowConstraints().get(1).setVgrow(Priority.NEVER);
        
        StackPane stocksLabelContainer = new StackPane(stocksLabel);
        stocksLabel.getStyleClass().add("product-stocks-label");
        stocksLabel.setAlignment(Pos.CENTER);
        GridPane.setValignment(stocksLabel, VPos.CENTER);
        GridPane.setHalignment(stocksLabel, HPos.CENTER);
        stocksLabel.setMaxWidth(-1);
        stocksLabel.setMinWidth(-1);
        stocksLabel.setPrefWidth(-1);
        this.textGridPane.add(stocksLabelContainer, 0, 2);
        this.textGridPane.getRowConstraints().get(2).setVgrow(Priority.ALWAYS);
        this.textGridPane.getRowConstraints().get(2).setValignment(VPos.CENTER);
        this.textGridPane.getRowConstraints().get(2).setMaxHeight(Double.MAX_VALUE);
        
        priceLabel.setPrefWidth(-1);
        priceLabel.setMaxWidth(Double.MAX_VALUE);
        priceLabel.setMinWidth(USE_PREF_SIZE);
        priceLabel.setTextAlignment(TextAlignment.LEFT);
        priceLabel.getStyleClass().add("product-price-label");
        controlGridPane.add(priceLabel, 0, 0);
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
        SceneManager.onChangeScene((currentScene, oldScene) -> {
            updateEditPermissions(UserSessionModel.currentUserIsAllowEditProduct());
        });
    }
    
    private void updateEditPermissions(boolean isAllowed) {
        Platform.runLater(() -> {
            editButton.setVisible(isAllowed);
            editButton.setManaged(isAllowed);
        });
    }
    
    public ObjectProperty<String> nameProperty() {
        return name;
    }
    
    private void setName(String name) {
        Platform.runLater(() -> {
            if (name == null) {
                categoryLabel.setText("Unknown Category");
                return;
            }
            
            nameLabel.setText(name);
        });
    }
    
    private void setToDefaultImage() {
        this.setImageURL(
            getClass().getResource("/images/image-placeholder.png").toExternalForm()
        );
    }
    
    public ObjectProperty<String> imageURLProperty() {
        return imageURL;
    }
    
    private void setImageURL(String imageUrl) {
        if (imageUrl == null) return;
        if (imageUrl.isEmpty()) return;
        imgProgressSpinner.setVisible(true);
        
        img = cachedImages.get(imageUrl);
        if (img == null) {
            img = new Image(imageUrl, true);
            cachedImages.put(imageUrl, img);
            
            img.progressProperty().addListener(($1, $2, progress) -> {
                if (progress.doubleValue() >= 1) {
                    imgProgressSpinner.setVisible(false);
                } else {
                    imgProgressSpinner.setVisible(true);
                }
            });
            
            img.errorProperty().addListener(e -> {
                if (!img.isError()) return;
                imgProgressSpinner.setVisible(false);
                setToDefaultImage();
            });
        }
        
        Platform.runLater(() -> {
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
    
    public ObjectProperty<Integer> categoryIDProperty() {
        return categoryID;
    }
    
    private void setCategoryID(Integer categoryID) {
        Platform.runLater(() -> {
            if (categoryID == null) {
                categoryLabel.setText("Unknown Category");
                return;
            }
            
            BaseModel.loadAndGetCategory(categoryID).onSucceeded(categoryObject -> {
                if (categoryObject == null) return;
                categoryLabel.setText(categoryObject.getName());
                if (oldCategoryObject != null) {
                    oldCategoryObject.nameProperty().removeListener(categoryChangeListener);
                }
                categoryObject.nameProperty().addListener(categoryChangeListener);
                oldCategoryObject = categoryObject;
            }).execute();
        });
    }
    
    public ObjectProperty<Integer> expectedStocksProperty() {
        return expectedStocks;
    }
    
    public ObjectProperty<Integer> currentStocksProperty() {
        return currentStocks;
    }
    
    private void setStocks(Integer current, Integer max) {
        Platform.runLater(() -> {
            double rate = (double) current / (double) Math.max(1, max);
            if (rate <= 0) {
                stocksLabel.getStyleClass().add("product-stocks-label-danger");
                stocksLabel.getStyleClass().remove("product-stocks-label-success");
                stocksLabel.getStyleClass().remove("product-stocks-label-warning");
            } else if (rate <= Config.lowStockRate) {
                stocksLabel.getStyleClass().add("product-stocks-label-warning");
                stocksLabel.getStyleClass().remove("product-stocks-label-success");
                stocksLabel.getStyleClass().remove("product-stocks-label-danger");
            } else {
                stocksLabel.getStyleClass().add("product-stocks-label-success");
                stocksLabel.getStyleClass().remove("product-stocks-label-warning");
                stocksLabel.getStyleClass().remove("product-stocks-label-danger");
            }
            
            stocksLabel.setText(current + "/" + max);
            stocksLabel.setAlignment(Pos.TOP_LEFT);
            stocksLabel.setWrapText(true);
            this.heightProperty().addListener(($1, $2, $3) -> {
                stocksLabel.setMaxHeight(this.getHeight() / 3);
            });
        });
    }
    
    public ObjectProperty<Double> priceProperty() {
        return price;
    }
    
    private void setPrice(Double price) {
        Platform.runLater(() -> {
            priceLabel.setText("P" + String.format("%.2f", price));
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
