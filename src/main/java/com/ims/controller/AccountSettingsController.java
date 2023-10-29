package com.ims.controller;

import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Arrays;

public class AccountSettingsController {
    @FXML
    MFXButton backButton;
    
    @FXML
    MFXButton deleteAccountButton;
    
    @FXML
    MFXButton changePasswordButton;
    
    @FXML
    MFXTextField emailTextField;
    
    @FXML
    MFXTextField roleTextField;
    
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
    public void initialize() {
        Utils.addIconToButton(backButton, "/icons/arrow-left.svg");
        backButton.getStyleClass().add("icon-button");
        backButton.setText("");
        
        Utils.createTabGroup(
            "tab-button-active",
            Arrays.asList(
                new Pair<>(tabGeneralButton, tabGeneralPane),
                new Pair<>(tabSecurityButton, tabSecurityPane),
                new Pair<>(tabOthersButton, tabOthersPane)
            )
        );
        
        emailTextField.setText("someemail12@gmail.com");
        roleTextField.setText("Manager");
    }
    
    @FXML
    public void goBack() {
        SceneManager.setScene("base");
    }
}
