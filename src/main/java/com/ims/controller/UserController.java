package com.ims.controller;

import com.ims.components.ComboBox;
import com.ims.components.Role;
import com.ims.components.RoleComboBox;
import com.ims.components.User;
import com.ims.model.UserEditModel;
import com.ims.model.UserManagerModel;
import com.ims.model.objects.RoleObject;
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
        
        UserEditModel.currentUser.addListener(
            ($1, $2, currentUser) -> {
                emailTextField.setText(currentUser.getEmail());
                roleComboBox.setValue(
                    UserManagerModel.loadAndGetRole(
                        currentUser.getRoleID()
                    )
                );
            }
        );
    }
    
    @FXML
    public void goBack() {
        SceneManager.setScene("user-manager");
    }
}
