package com.ims.components;

import com.ims.database.DBProducts;
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
    public final ProductObject productObject;
    private final ObservableList<String> styleClass = this.getStyleClass();
    
    private final GridPane textGridPane = LayoutUtils.createGridPane(4, 1);
    private final ImageView imgView = new ImageView();
    
    public Product(ProductObject productObject) {
        this.productObject = productObject;
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
            ProductModel.idProperty.set(productObject.getID());
            ProductModel.nameProperty.set(productObject.getName());
            ProductModel.categoryIDProperty.set(productObject.getCategoryID());
            ProductModel.imageURLProperty.set(productObject.getImageURL());
            ProductModel.priceProperty.set(productObject.getPrice());
            ProductModel.currentStocksProperty.set(productObject.getCurrentStocks());
            ProductModel.expectedStocksProperty.set(productObject.getExpectedStocks());
            SceneManager.setScene("product");
        });
        
        Transition.fadeUp(this, 150);
        
        // Setup image
        imgView.setPreserveRatio(true);
        imgView.getStyleClass().add("product-image");
        
        StackPane imgContainer = new StackPane(imgView);
        Rectangle rectClip = new Rectangle(0, 0, 0, 0);
        rectClip.setArcWidth(10);
        rectClip.setArcHeight(10);
        rectClip.widthProperty().bind(imgContainer.widthProperty());
        rectClip.heightProperty().bind(imgContainer.heightProperty());
        
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
            });
        });
        
        this.add(imgContainer, 0, 0);
        this.setImage(getClass().getResource("/images/image-placeholder.png").toExternalForm());
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
        Label label = new Label();
        label.setText(name);
        label.getStyleClass().add("product-name-label");
        this.textGridPane.add(label, 0, 0);
    }
    
    public void setCategory(String category) {
        Label label = new Label();
        label.setText(category);
        label.getStyleClass().add("product-category-label");
        this.textGridPane.add(label, 0, 1);
    }
    
    public void setStocks(int current, int max) {
        Label label = new Label();
        label.setText("In stock: " + current + "/" + max);
        label.setAlignment(Pos.TOP_LEFT);
        label.setWrapText(true);
        this.heightProperty().addListener(($1, $2, $3) -> {
            label.setMaxHeight(this.getHeight() / 3);
        });
        
        label.getStyleClass().add("product-description-label");
        this.textGridPane.add(label, 0, 2);
    }
    
    public void setPrice(float price) {
        Label label = new Label();
        label.setText("₱" + String.format("%.2f", price));
        label.getStyleClass().add("product-price-label");
        this.textGridPane.add(label, 0, 3);
    }
}
