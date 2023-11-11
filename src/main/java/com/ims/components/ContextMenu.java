package com.ims.components;

import com.ims.utils.SceneManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class ContextMenu extends Popup {
    private final VBox container = new VBox();
    
    public ContextMenu() {
        this.setWidth(-1);
        this.setHeight(-1);
        this.setAutoHide(true);
        container.setPadding(new Insets(10, 0, 10, 0));
        container.getStyleClass().add("context-menu");
        container.getStylesheets().add(
            getClass().getResource("/styles/global.css").toExternalForm()
        );
        
        this.getContent().addAll(container);
    }
    
    public void bindToNode(Node node) {
        // We do this for the sake of it computing its bounds
        Platform.runLater(() -> {
            this.show(SceneManager.getStage(), -999, -999);
            Platform.runLater(() -> {
                this.hide();
            });
        });
        
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            if (this.isShowing()) {
                this.hide();
            } else {
                double width = this.container.getBoundsInLocal().getWidth();
                double height = this.container.getBoundsInLocal().getHeight();
                double x = event.getScreenX();
                double y = event.getScreenY();
                double endX = x + width;
                double endY = y + height;
                Stage stage = SceneManager.getStage();
                double stageEndX = stage.getX() + stage.getWidth();
                double stageEndY = stage.getY() + stage.getHeight();
                if (endX > stageEndX) {
                    x = stageEndX - width;
                }
                if (endY > stageEndY) {
                    y = stageEndY - height;
                }
                this.show(node, x, y);
            }
        });
    }
    
    public MFXButton addButtonItem(String text) {
        MFXButton button = new MFXButton();
        button.getStyleClass().add("context-menu-item-button");
        button.setText(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setTextAlignment(TextAlignment.LEFT);
        button.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(button);
        return button;
    }
    
    public void setHeaderText(String text) {
        Label label = new Label();
        label.getStyleClass().add("text-medium");
        label.setStyle("-fx-text-fill: -text-color-fade; -fx-border-width: 0 0 1 0; -fx-border-color:  rgba(125, 125, 125, 0.1);");
        label.setText(text);
        label.setWrapText(true);
        label.setPrefHeight(50);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxHeight(Region.USE_PREF_SIZE);
        label.setPadding(new Insets(0, 20, 0, 20));
        container.getChildren().add(0, label);
    }
}
