package com.ims.controller;

import com.ims.components.*;
import com.ims.model.UserEditModel;
import com.ims.model.UserManagerModel;
import com.ims.model.UserSessionModel;
import com.ims.model.objects.UserObject;
import com.ims.utils.SceneManager;
import com.ims.utils.LayoutUtils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.util.Arrays;

public class UserController {
    @FXML
    MFXButton backButton;
    
    @FXML
    MFXButton disableAccountButton;
    
    @FXML
    MFXButton tabGeneralButton;
    
    @FXML
    GridPane tabGeneralPane;
    
    @FXML
    MFXButton tabOthersButton;
    
    @FXML
    GridPane tabOthersPane;
    
    @FXML
    VBox generalTabContentPane;
    
    @FXML
    MFXTextField emailTextField;
    
    RoleComboBox roleComboBox = new RoleComboBox();
    
    @FXML
    public void initialize() {
        LayoutUtils.addIconToButton(backButton, "/icons/arrow-left.svg");
        backButton.getStyleClass().add("icon-button");
        backButton.setText("");
        
        LayoutUtils.createTabGroup(
            "tab-button-active",
            Arrays.asList(
                new Pair<>(tabGeneralButton, tabGeneralPane),
                new Pair<>(tabOthersButton, tabOthersPane)
            )
        );
        
        roleComboBox.setMinWidth(100);
        roleComboBox.setMaxWidth(300);
        roleComboBox.setPrefWidth(300);
        generalTabContentPane.getChildren().add(roleComboBox);
        
        roleComboBox.addSelectionListener(roleObject -> {
            UserObject currentUser = UserEditModel.currentUser.get();
            UserManagerModel.updateUser(
                currentUser.getID(),
                null,
                roleObject.getID(),
                null,
                null
            );
        });
        
        UserEditModel.currentUser.addListener(
            ($1, $2, currentUser) -> {
                emailTextField.setText(currentUser.getEmail());
                if (currentUser.getRoleID() != null) {
                    roleComboBox.setValue(
                        UserManagerModel.loadAndGetRole(
                            currentUser.getRoleID()
                        )
                    );
                }
                
                updateDisableAccountButton(
                    currentUser.isDisabled()
                );
            }
        );
        
        disableAccountButton.setOnMouseClicked(e -> {
            if (UserEditModel.currentUser.get().isDisabled()) {
                PopupService.confirmDialog.setup(
                    "Enable Account",
                    "Are you sure you want to enable this user's account?",
                    "Enable",
                    false,
                    () -> {
                        UserManagerModel.updateUser(
                            UserEditModel.currentUser.get().getID(),
                            null,
                            null,
                            false,
                            null
                        );
                        
                        PopupService.confirmDialog.hide();
                        
                        PopupService.messageDialog.setup(
                            "Enable Account",
                            "Account has been enabled.",
                            "Got it!"
                        ).show();
                        
                        updateDisableAccountButton(false);
                        UserEditModel.currentUser.get().setDisabled(false);
                    }
                ).show();
            } else {
                PopupService.confirmDialog.setup(
                    "Disable Account",
                    "Are you sure you want to disable this user's account?",
                    "Disable",
                    true,
                    () -> {
                        UserManagerModel.updateUser(
                            UserEditModel.currentUser.get().getID(),
                            null,
                            null,
                            true,
                            null
                        );
                        
                        PopupService.confirmDialog.hide();
                        
                        PopupService.messageDialog.setup(
                            "Disable Account",
                            "Account has been disabled.",
                            "Got it!"
                        ).show();
                        
                        updateDisableAccountButton(true);
                        UserEditModel.currentUser.get().setDisabled(true);
                    }
                ).show();
            }
        });
        
        UserSessionModel.currentUser.addListener(e -> {
            if (UserSessionModel.currentUserIsOwner()) {
                disableAccountButton.setDisable(true);
            } else {
                disableAccountButton.setDisable(false);
            }
        });
    }
    
    private void updateDisableAccountButton(boolean isDisabled) {
        if (isDisabled) {
            disableAccountButton.setText("Enable Account");
            disableAccountButton.getStyleClass().remove("button-danger");
        } else {
            disableAccountButton.setText("Disable Account");
            disableAccountButton.getStyleClass().add("button-danger");
        }
    }
    
    @FXML
    public void goBack() {
        SceneManager.setScene("user-manager");
    }
}
