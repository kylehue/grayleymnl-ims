package com.ims.components;

import com.ims.Config;
import com.ims.model.UserSessionModel;
import com.ims.utils.LayoutUtils;
import com.ims.utils.TextFieldValidator;
import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.scene.layout.GridPane;

public class ConfirmDeleteAccountModal extends Modal {
    public final MFXButton deleteButton = new MFXButton("Delete");
    public final MFXTextField emailTextField = new MFXTextField();
    public final MFXPasswordField passwordField = new MFXPasswordField();
    public final TextFieldValidator emailTextFieldValidator;
    public final TextFieldValidator passwordFieldValidator;
    
    public ConfirmDeleteAccountModal() {
        deleteButton.getStyleClass().add("button-danger");
        
        this.headerText.setText("Delete Account");
        this.controlContainer.getChildren().add(deleteButton);
        
        LayoutUtils.setupGridPane(this.contentContainer, 2, 1);
        this.contentContainer.setHgap(10);
        this.contentContainer.setVgap(10);
        this.contentContainer.add(emailTextField, 0, 0);
        this.contentContainer.add(passwordField, 0, 1);
        this.contentContainer.setMaxWidth(400);
        this.contentContainer.setMaxHeight(400);
        
        emailTextField.setFloatingText("Enter your email");
        emailTextField.setMinWidth(100);
        emailTextField.setPrefWidth(300);
        emailTextField.setMaxWidth(Double.MAX_VALUE);
        
        emailTextFieldValidator = new TextFieldValidator(
            emailTextField
        );
        
        emailTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Invalid email.",
            () -> emailTextField.getText().equals(
                UserSessionModel.getCurrentUserEmail()
            ),
            deleteButton.armedProperty()
        );
        
        passwordField.setFloatingText("Enter your password");
        passwordField.setMinWidth(100);
        passwordField.setPrefWidth(300);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        
        passwordFieldValidator = new TextFieldValidator(
            passwordField
        );
        
        passwordFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Invalid password.",
            () -> Utils.checkPassword(
                passwordField.getText(),
                UserSessionModel.getCurrentUserPassword()
            ),
            deleteButton.armedProperty()
        );
        
        this.setOnShown((e) -> {
            emailTextField.requestFocus();
        });
        
        this.setOnHidden((e) -> {
            emailTextFieldValidator.reset();
            passwordFieldValidator.reset();
        });
    }
}
