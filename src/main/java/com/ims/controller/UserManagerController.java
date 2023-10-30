package com.ims.controller;

import com.ims.components.Role;
import com.ims.components.User;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Arrays;

public class UserManagerController {
    @FXML
    MFXButton backButton;
    
    @FXML
    MFXButton tabUsersButton;
    
    @FXML
    GridPane tabUsersPane;
    
    @FXML
    FlowPane usersFlowPane;
    
    @FXML
    MFXButton tabRolesButton;
    
    @FXML
    GridPane tabRolesPane;
    
    @FXML
    FlowPane rolesFlowPane;
    
    @FXML
    MFXButton saveAllRolesButton;
    
    @FXML
    MFXTextField searchUserTextField;
    
    @FXML
    MFXTextField searchRoleTextField;
    
    @FXML
    public void initialize() {
        Utils.addIconToButton(backButton, "/icons/arrow-left.svg");
        backButton.getStyleClass().add("icon-button");
        backButton.setText("");
        
        Utils.createTabGroup(
            "tab-button-active",
            Arrays.asList(
                new Pair<>(tabUsersButton, tabUsersPane),
                new Pair<>(tabRolesButton, tabRolesPane)
            )
        );
        
        Utils.createResponsiveFlowPane(
            usersFlowPane,
            300,
            1,
            false
        );
        Utils.createResponsiveFlowPane(
            rolesFlowPane,
            300,
            1,
            false
        );
        
        for (int i = 0; i < 12; i++) {
            this.addUser(
                "someemail12@gmail.com",
                "Manager",
                "2012-10-12",
                "2023-10-30"
            );
        }
        
        for (int i = 0; i < 7; i++) {
            this.addRole("Role " + (i + 1));
        }
    }
    
    private User addUser(
        String email,
        String role,
        String joinedDate,
        String lastActivityDate
    ) {
        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        user.setJoinedDate(joinedDate);
        user.setLastActivityDate(lastActivityDate);
        usersFlowPane.getChildren().add(user);
        return user;
    }
    
    private Role addRole(String name) {
        Role role = new Role();
        role.setName(name);
        rolesFlowPane.getChildren().add(role);
        return role;
    }
    
    @FXML
    public void goBack() {
        SceneManager.setScene("base");
    }
}
