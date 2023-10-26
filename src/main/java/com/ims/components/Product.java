package com.ims.components;

import com.ims.utils.Utils;
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
import javafx.scene.shape.Rectangle;

public class Product extends GridPane {
    private final ObservableList<String> styleClass = this.getStyleClass();
    
    private final GridPane textGridPane = Utils.createGridPane(4, 1);
    
    public Product() {
        this.styleClass.add("card");
        this.styleClass.add("product-container");
        
        // Setup main row and columns
        RowConstraints row = new RowConstraints();
        this.getRowConstraints().add(0, row);
        ColumnConstraints imageColumn = new ColumnConstraints();
        this.getColumnConstraints().add(0, imageColumn);
        ColumnConstraints detailsColumn = new ColumnConstraints();
        this.getColumnConstraints().add(1, detailsColumn);
        
        // Setup grid pane for the details
        GridPane detailsGridPane = Utils.createGridPane(2, 1);
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
        GridPane controlGridPane = Utils.createGridPane(1, 1);
        detailsGridPane.add(controlGridPane, 0, 1);
        RowConstraints controlRow = controlGridPane.getRowConstraints().get(0);
        controlRow.setFillHeight(false);
        controlRow.setValignment(VPos.BOTTOM);
        FlowPane controlFlowPane = new FlowPane();
        controlFlowPane.setAlignment(Pos.BOTTOM_RIGHT);
        controlGridPane.add(controlFlowPane, 0, 0);
        MFXButton editButton = new MFXButton();
        editButton.setText("Edit");
        Utils.addIconToButton(editButton, "/icons/pencil.svg");
        editButton.getStyleClass().add("outline-button");
        controlFlowPane.getChildren().add(editButton);
    }
    
    /**
     * Set the image of the product.
     *
     * @param imageUrl The URL of the image.
     */
    public void setImage(String imageUrl) {
        Image img = new Image(imageUrl);
        
        ImageView imgView = new ImageView(img);
        imgView.setPreserveRatio(true);
        imgView.getStyleClass().add("product-image");
        
        StackPane imgContainer = new StackPane(imgView);
        imgContainer.setAlignment(Pos.CENTER);
        
        Rectangle rectClip = new Rectangle(0, 0, 0, 0);
        rectClip.setArcWidth(10);
        rectClip.setArcHeight(10);
        rectClip.widthProperty().bind(imgContainer.widthProperty());
        rectClip.heightProperty().bind(imgContainer.heightProperty());
        
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
    
    public void setDescription(String description) {
        Label label = new Label();
        label.setText(description);
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
