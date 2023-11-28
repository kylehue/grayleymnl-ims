package com.ims.controller;

import com.ims.components.PopupService;
import com.ims.database.DBRoles;
import com.ims.model.UserSessionModel;
import com.ims.model.objects.RoleObject;
import com.ims.utils.*;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.Objects;

public class AccountSettingsController {
    @FXML
    MFXButton tabGeneralButton;
    
    @FXML
    GridPane tabGeneralPane;
    
    @FXML
    MFXButton tabSecurityButton;
    
    @FXML
    GridPane tabSecurityPane;
    
    @FXML
    MFXButton tabOthersButton;
    
    @FXML
    GridPane tabOthersPane;
    
    @FXML
    MFXButton backButton;
    
    @FXML
    MFXButton deleteAccountButton;
    
    @FXML
    MFXTextField emailTextField;
    
    @FXML
    MFXTextField roleTextField;
    
    @FXML
    MFXPasswordField oldPasswordField;
    
    @FXML
    MFXPasswordField newPasswordField;
    
    @FXML
    MFXPasswordField confirmNewPasswordField;
    
    @FXML
    MFXButton updatePasswordButton;
    
    @FXML
    public void initialize() {
        LayoutUtils.addIconToButton(backButton, "/icons/arrow-left.svg");
        backButton.getStyleClass().add("icon-button");
        backButton.setText("");
        
        TabGroup tabGroup = new TabGroup(
            "tab-button-active",
            Arrays.asList(
                new Pair<>(tabGeneralButton, tabGeneralPane),
                new Pair<>(tabSecurityButton, tabSecurityPane),
                new Pair<>(tabOthersButton, tabOthersPane)
            ),
            "account-settings"
        );
        
        UserSessionModel.currentUser.addListener(e -> {
            if (UserSessionModel.currentUser.get() == null) return;
            emailTextField.setText(UserSessionModel.getCurrentUserEmail());
            RoleObject roleData = UserSessionModel.getCurrentUserRole();
            if (roleData != null) {
                roleTextField.setText(roleData.getName());
            }
        });
        
        TextFieldValidator oldPasswordFieldValidator = new TextFieldValidator(oldPasswordField);
        oldPasswordFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Invalid password.",
            () -> {
                String pw = UserSessionModel.getCurrentUserPassword();
                return Utils.checkPassword(
                    oldPasswordField.getText(),
                    pw
                );
            },
            updatePasswordButton.armedProperty()
        );
        
        TextFieldValidator newPasswordFieldValidator = new TextFieldValidator(newPasswordField);
        newPasswordFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Password must be at least 8 characters long.",
            () -> newPasswordField.getText().length() >= 8,
            newPasswordField.textProperty(),
            updatePasswordButton.armedProperty()
        );
        
        TextFieldValidator confirmNewPasswordTextFieldValidator = new TextFieldValidator(confirmNewPasswordField);
        confirmNewPasswordTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Passwords doesn't match.",
            () -> Objects.equals(
                newPasswordField.getText(),
                confirmNewPasswordField.getText()
            ),
            newPasswordField.textProperty(),
            confirmNewPasswordField.textProperty(),
            updatePasswordButton.armedProperty()
        );
        
        updatePasswordButton.setOnMouseClicked(e -> {
            if (!oldPasswordFieldValidator.isValid() ||
                !newPasswordFieldValidator.isValid() ||
                !confirmNewPasswordTextFieldValidator.isValid()) {
                return;
            }
            
            PopupService.confirmDialog.setup(
                "Update Password",
                "Are you sure you want to update your password?",
                "Update",
                false,
                () -> {
                    if (!oldPasswordFieldValidator.isValid() ||
                        !newPasswordFieldValidator.isValid() ||
                        !confirmNewPasswordTextFieldValidator.isValid()) {
                        return;
                    }
                    
                    String password = confirmNewPasswordField.getText();
                    UserSessionModel.updatePassword(
                        Utils.hashPassword(password)
                    );
                    
                    oldPasswordField.setText("");
                    newPasswordField.setText("");
                    confirmNewPasswordField.setText("");
                    
                    oldPasswordFieldValidator.reset();
                    newPasswordFieldValidator.reset();
                    confirmNewPasswordTextFieldValidator.reset();
                    
                    PopupService.confirmDialog.hide();
                    
                    PopupService.messageDialog.setup(
                        "Update Password",
                        "Your password has been updated.",
                        "Got it!"
                    ).show();
                }
            ).show();
        });
        
        deleteAccountButton.setOnMouseClicked(e -> {
            PopupService.confirmWithAuthModal.setup(
                "Delete Account",
                "Delete",
                () -> {
                    UserSessionModel.deleteAccount();
                    
                    PopupService.confirmWithAuthModal.hide();
                    
                    PopupService.messageDialog.setup(
                        "Delete Account",
                        "Account has been deleted.",
                        "Got it!"
                    ).show();
                }
            ).show();
        });
        
        UserSessionModel.currentUser.addListener(e -> {
            if (UserSessionModel.currentUserIsOwner()) {
                deleteAccountButton.setDisable(true);
            } else {
                deleteAccountButton.setDisable(false);
            }
        });
    }
    
    @FXML
    public void goBack() {
        SceneManager.setScene("base");
    }
}
