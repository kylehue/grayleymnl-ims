package com.ims.components;

import com.ims.Config;
import com.ims.utils.TextFieldValidator;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.scene.control.Label;

public class ConfirmDeleteProductModal extends Modal {
    public final MFXButton deleteButton = new MFXButton("Delete");
    
    public ConfirmDeleteProductModal() {
        this.headerText.setText("Delete Product");
        this.controlContainer.getChildren().add(deleteButton);
        deleteButton.getStyleClass().add("button-danger");
        
        Label label = new Label("Are you sure you want to delete this product?");
        this.contentContainer.add(label, 0, 0);
        this.contentContainer.setMaxWidth(400);
        this.contentContainer.setMaxHeight(400);
    }
}
