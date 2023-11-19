package com.ims.components;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.scene.control.Label;

public class ConfirmUpdatePasswordModal extends Modal {
    public final MFXButton updateButton = new MFXButton("Update");
    
    public ConfirmUpdatePasswordModal() {
        this.headerText.setText("Update Password");
        this.controlContainer.getChildren().add(updateButton);
        
        Label label = new Label("Are you sure you want to change your password?");
        this.contentContainer.add(label, 0, 0);
        this.contentContainer.setMaxWidth(400);
        this.contentContainer.setMaxHeight(400);
    }
}