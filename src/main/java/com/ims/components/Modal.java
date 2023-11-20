package com.ims.components;

import com.ims.utils.LayoutUtils;
import com.ims.utils.SceneManager;
import com.ims.utils.Transition;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.*;
import javafx.util.Duration;

public class Modal extends Stage {
    private double xOffset = 0;
    private double yOffset = 0;
    public final Label headerText = new Label("Header");
    public final HBox controlContainer = new HBox();
    public final GridPane contentContainer = LayoutUtils.createGridPane(
        1,
        1
    );
    
    public Modal() {
        this.centerOnScreen();
        GridPane container = LayoutUtils.createGridPane(3, 1);
        container.getStyleClass().addAll("card", "modal");
        container.getStylesheets().add(
            getClass().getResource("/styles/global.css").toExternalForm()
        );
        container.setMinWidth(Region.USE_PREF_SIZE);
        container.setPrefWidth(Region.USE_COMPUTED_SIZE);
        container.setMaxWidth(Double.MAX_VALUE);
        container.setMaxHeight(Double.MAX_VALUE);
        container.setFocusTraversable(true);
        container.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> container.requestFocus());
        container.setOnMousePressed(this::onMousePressed);
        container.setOnMouseDragged(this::onMouseDragged);
        
        GridPane headerContainer = LayoutUtils.createGridPane(1, 2);
        GridPane.setVgrow(headerContainer, Priority.NEVER);
        container.getRowConstraints().get(0).setMinHeight(50);
        container.getRowConstraints().get(0).setPrefHeight(50);
        container.getRowConstraints().get(0).setMaxHeight(50);
        container.add(headerContainer, 0, 0);
        headerContainer.add(headerText, 0, 0);
        headerText.getStyleClass().add("modal-header-text");
        GridPane.setValignment(headerText, VPos.CENTER);

        MFXButton closeButton = new MFXButton("");
        closeButton.getStyleClass().add("icon-button");
        LayoutUtils.addIconToButton(closeButton, "/icons/close.svg");
        headerContainer.add(closeButton, 1, 0);
        GridPane.setHalignment(closeButton, HPos.RIGHT);
        GridPane.setValignment(closeButton, VPos.CENTER);
        closeButton.setOnMouseClicked((e) -> {
            this.hide();
        });

        contentContainer.setMinWidth(Region.USE_COMPUTED_SIZE);
        contentContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
        contentContainer.setMaxWidth(Double.MAX_VALUE);
        contentContainer.setHgap(10);
        contentContainer.setVgap(10);
        GridPane.setMargin(contentContainer, new Insets(15, 0, 15, 0));
        container.add(contentContainer, 0, 1);

        controlContainer.setSpacing(10);
        GridPane.setVgrow(controlContainer, Priority.NEVER);
        container.getRowConstraints().get(2).setMinHeight(50);
        container.getRowConstraints().get(2).setPrefHeight(50);
        container.getRowConstraints().get(2).setMaxHeight(50);
        container.add(controlContainer, 0, 2);
        controlContainer.setAlignment(Pos.CENTER_RIGHT);

        SceneManager.onChangeScene(($1, $2) -> {
            this.hide();
        });

        this.showingProperty().addListener((e) -> {
            Platform.runLater(() -> {
                Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
                this.setX((primScreenBounds.getWidth() - this.getWidth()) / 2);
                this.setY((primScreenBounds.getHeight() - this.getHeight()) / 2);
            });
            
            Transition.fadeIn(container, 150);
        });
        
        this.initOwner(SceneManager.getStage());
        this.initModality(Modality.NONE);
        this.initStyle(StageStyle.TRANSPARENT);
        this.sizeToScene();
        Scene scene = new Scene(container);
        scene.setFill(Color.TRANSPARENT);
        this.setScene(scene);
        
        for (Node node : container.getChildren()) {
            if (node instanceof GridPane pane) {
                pane.heightProperty().addListener(e ->{
                    this.sizeToScene();
                });
            }
        }
        
        container.setStyle("""
            -fx-background-insets: 20;
            -fx-padding: 30 45 35 45;
            -fx-border-width: 0;
            -fx-border-color: transparent;
            -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 12, 0, 0, 6);
            """);
    }
    
    private void onMousePressed(MouseEvent event) {
        xOffset = event.getScreenX() - this.getX();
        yOffset = event.getScreenY() - this.getY();
    }
    
    private void onMouseDragged(MouseEvent event) {
        this.setX(event.getScreenX() - xOffset);
        this.setY(event.getScreenY() - yOffset);
    }
}
