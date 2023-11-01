package com.ims.components;

import com.ims.utils.LayoutUtils;
import com.ims.utils.SceneManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.mfxcore.collections.Grid;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class Modal extends Popup {
    private double xOffset = 0;
    private double yOffset = 0;
    public final Label headerText = new Label("Header");
    public final HBox controlContainer = new HBox();
    public final GridPane contentContainer = LayoutUtils.createGridPane(
        1,
        1
    );
    
    public Modal() {
        this.setWidth(-1);
        this.setHeight(-1);
        this.setAutoHide(false);
        this.centerOnScreen();
        this.setHideOnEscape(true);
        GridPane container = LayoutUtils.createGridPane(3, 1);
        this.getContent().add(container);
        container.getStyleClass().addAll("card", "modal");
        container.getStylesheets().add(
            getClass().getResource("/styles/global.css").toExternalForm()
        );
        
        container.setOnMousePressed(this::onMousePressed);
        container.setOnMouseDragged(this::onMouseDragged);
        
        GridPane headerContainer = LayoutUtils.createGridPane(1, 2);
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
        
        container.add(contentContainer, 0, 1);
        
        controlContainer.setSpacing(10);
        container.add(controlContainer, 0, 2);
        controlContainer.setAlignment(Pos.CENTER_RIGHT);
        GridPane.setMargin(controlContainer, new Insets(10, 0, 0, 0));
        
        SceneManager.onChangeScene(($1, $2) -> {
            this.hide();
        });
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
