package com.ims.controller;

import com.ims.components.Role;
import com.ims.components.User;
import com.ims.utils.SceneManager;
import com.ims.utils.LayoutUtils;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
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
    }
    
    @FXML
    public void goBack() {
        SceneManager.setScene("user-manager");
    }
}