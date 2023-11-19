package com.ims.components;

import com.ims.database.DBProducts;
import com.ims.model.BaseModel;
import com.ims.model.ProductModel;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.ProductObject;
import com.ims.utils.LayoutUtils;
import com.ims.utils.SceneManager;
import com.ims.utils.Transition;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
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
    
    public Product(ProductObject productObject) {
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
        MFXButton editButton = new MFXButton();
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
        this.setImage(
            getClass().getResource("/images/image-placeholder.png").toExternalForm()
        );
        
        // Setup labels
        this.textGridPane.add(nameLabel, 0, 0);
        this.textGridPane.add(categoryLabel, 0, 1);
        this.textGridPane.add(stocksLabel, 0, 2);
        this.textGridPane.add(priceLabel, 0, 3);
        
        this.setProductObject(productObject);
    }
    
    /**
     * Set the image of the product.
     *
     * @param imageUrl The URL of the image.
     */
    public void setImage(String imageUrl) {
        if (imageUrl == null) return;
        if (imageUrl.isEmpty()) return;
        
        Image img = new Image(imageUrl);
        this.imgView.setImage(img);
    }
    
    public void setName(String name) {
        nameLabel.setText(name);
        nameLabel.getStyleClass().add("product-name-label");
    }
    
    public void setCategory(String category) {
        categoryLabel.setText(category);
        categoryLabel.getStyleClass().add("product-category-label");
    }
    
    public void setStocks(int current, int max) {
        stocksLabel.setText("In stock: " + current + "/" + max);
        stocksLabel.setAlignment(Pos.TOP_LEFT);
        stocksLabel.setWrapText(true);
        this.heightProperty().addListener(($1, $2, $3) -> {
            stocksLabel.setMaxHeight(this.getHeight() / 3);
        });
        
        stocksLabel.getStyleClass().add("product-description-label");
    }
    
    public void setPrice(float price) {
        priceLabel.setText("₱" + String.format("%.2f", price));
        priceLabel.getStyleClass().add("product-price-label");
    }
    
    public void setProductObject(ProductObject productObject) {
        this.productObject = productObject;
        this.setName(productObject.getName());
        this.setStocks(
            productObject.getCurrentStocks(),
            productObject.getExpectedStocks()
        );
        this.setCategory(
            BaseModel.loadAndGetCategory(productObject.getCategoryID()).getName()
        );
        this.setImage(productObject.getImageURL());
        this.setPrice((float) productObject.getPrice());
    }
    
    public ProductObject getProductObject() {
        return productObject;
    }
}
