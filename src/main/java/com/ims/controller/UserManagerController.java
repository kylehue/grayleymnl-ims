package com.ims.controller;

import com.ims.Config;
import com.ims.components.*;
import com.ims.model.BaseModel;
import com.ims.model.UserManagerModel;
import com.ims.model.objects.RoleObject;
import com.ims.model.objects.UserObject;
import com.ims.utils.LazyLoader;
import com.ims.utils.SceneManager;
import com.ims.utils.LayoutUtils;
import com.ims.utils.TabGroup;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.*;

public class UserManagerController {
    //////////////////////////////////////////////////////////////////////
    // ---------------------------- ROLES ----------------------------- //
    //////////////////////////////////////////////////////////////////////
    
    @FXML
    MFXButton tabRolesButton;
    
    @FXML
    GridPane tabRolesPane;
    
    @FXML
    MFXButton addRoleButton;
    
    @FXML
    MFXButton refreshRolesButton;
    
    @FXML
    MFXTextField searchRoleTextField;
    
    @FXML
    MFXScrollPane rolesScrollPane;
    
    @FXML
    FlowPane rolesFlowPane;
    
    final ObservableMap<Integer, Role> roles = FXCollections.observableHashMap();
    
    RoleAddModal roleAddModal = new RoleAddModal();
    
    private boolean rolePageInitialized = false;
    public void initializeRolePage() {
        if (rolePageInitialized) {
            return;
        }
        rolePageInitialized = true;
        UserManagerModel.roleMap.addListener(
            (MapChangeListener<Integer, RoleObject>) change -> {
                int id = change.getKey();
                if (change.wasAdded()) {
                    RoleObject roleObject = change.getValueAdded();
                    addRole(roleObject);
                } else if (change.wasRemoved()) {
                    removeRole(id);
                }
            }
        );
        
        LayoutUtils.applyVirtualScrolling(
            rolesScrollPane,
            rolesFlowPane
        );
        this.initializeRoleLazyLoad();
        
        addRoleButton.setOnMouseClicked(e -> {
            roleAddModal.show();
        });
        
        roleAddModal.setOnAction(() -> {
            UserManagerModel.addRole(
                roleAddModal.nameTextField.getText()
            );
            
            roleAddModal.hide();
            
            return null;
        });
        
        searchRoleTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            UserManagerModel.searchRoles(searchRoleTextField.getText());
        });
        
        LayoutUtils.addIconToButton(refreshRolesButton, "/icons/refresh.svg");
        refreshRolesButton.getStyleClass().add("icon-button");
        refreshRolesButton.setText("");
        refreshRolesButton.setOnAction(e -> {
            UserManagerModel.searchRoles("");
        });
    }
    
    public void initializeRoleLazyLoad() {
        LazyLoader lazyLoader = new LazyLoader(
            rolesScrollPane,
            rolesFlowPane,
            roles
        );
        
        lazyLoader.setLoader((requestType) -> {
            switch (requestType) {
                case INITIAL:
                    UserManagerModel.loadRoles(1);
                    break;
                case HIT_BOTTOM:
                    if (!searchRoleTextField.getText().isEmpty()) return;
                    UserManagerModel.loadRoles(Config.roleLoadLimit);
                    break;
                case INSUFFICIENT:
                    if (!searchRoleTextField.getText().isEmpty()) return;
                    UserManagerModel.loadRoles(Config.roleLoadLimit / 3);
                    break;
            }
        });
    }
    
    private void addRole(RoleObject roleObject) {
        Role role = new Role();
        Platform.runLater(() -> {
            if (this.roles.containsKey(roleObject.getID())) return;
            role.setRoleObject(roleObject);
            this.roles.put(roleObject.getID(), role);
            if (roleObject.isNew()) {
                rolesFlowPane.getChildren().addFirst(role);
            } else {
                rolesFlowPane.getChildren().addLast(role);
            }
        });
    }
    
    private void removeRole(int id) {
        Role roleToRemove = this.roles.get(id);
        if (roleToRemove != null) {
            Platform.runLater(() -> {
                rolesFlowPane.getChildren().remove(roleToRemove);
                this.roles.remove(roleToRemove.getRoleObject().getID());
            });
        }
    }
    
    //////////////////////////////////////////////////////////////////////
    // ---------------------------- USERS ----------------------------- //
    //////////////////////////////////////////////////////////////////////
    
    @FXML
    MFXButton tabUsersButton;
    
    @FXML
    GridPane tabUsersPane;
    
    @FXML
    MFXTextField searchUserTextField;
    
    @FXML
    MFXButton refreshUsersButton;
    
    @FXML
    FlowPane usersFlowPane;
    
    @FXML
    MFXScrollPane usersScrollPane;
    
    final ObservableMap<Integer, User> users = FXCollections.observableHashMap();
    
    private boolean userPageInitialized = false;
    public void initializeUserPage() {
        if (userPageInitialized) {
            return;
        }
        userPageInitialized = true;
        UserManagerModel.userMap.addListener(
            (MapChangeListener<Integer, UserObject>) change -> {
                int id = change.getKey();
                if (change.wasAdded()) {
                    UserObject userObject = change.getValueAdded();
                    addUser(userObject);
                } else if (change.wasRemoved()) {
                    removeUser(id);
                }
            }
        );
        
        LayoutUtils.applyVirtualScrolling(
            usersScrollPane,
            usersFlowPane
        );
        this.initializeUserLazyLoad();
        
        searchUserTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            UserManagerModel.searchUsers(searchUserTextField.getText());
        });
        
        LayoutUtils.addIconToButton(refreshUsersButton, "/icons/refresh.svg");
        refreshUsersButton.getStyleClass().add("icon-button");
        refreshUsersButton.setText("");
        refreshUsersButton.setOnAction(e -> {
            UserManagerModel.searchUsers("");
        });
    }
    
    public void initializeUserLazyLoad() {
        LazyLoader lazyLoader = new LazyLoader(
            usersScrollPane,
            usersFlowPane,
            users
        );
        
        lazyLoader.setLoader((requestType) -> {
            switch (requestType) {
                case INITIAL:
                    UserManagerModel.loadUsers(1);
                    break;
                case HIT_BOTTOM:
                    if (!searchUserTextField.getText().isEmpty()) return;
                    UserManagerModel.loadUsers(Config.userLoadLimit);
                    break;
                case INSUFFICIENT:
                    if (!searchUserTextField.getText().isEmpty()) return;
                    UserManagerModel.loadUsers(Config.userLoadLimit / 3);
                    break;
            }
        });
    }
    
    private void addUser(UserObject userObject) {
        User user = new User();
        Platform.runLater(() -> {
            if (this.users.containsKey(userObject.getID())) return;
            user.setUserObject(userObject);
            this.users.put(userObject.getID(), user);
            if (userObject.isNew()) {
                usersFlowPane.getChildren().addFirst(user);
            } else {
                usersFlowPane.getChildren().addLast(user);
            }
        });
    }
    
    private void removeUser(int id) {
        User userToRemove = this.users.get(id);
        if (userToRemove != null) {
            Platform.runLater(() -> {
                usersFlowPane.getChildren().remove(userToRemove);
                this.users.remove(userToRemove.getUserObject().getID());
            });
        }
    }
    
    //////////////////////////////////////////////////////////////////////
    // ---------------------------- MAIN ------------------------------ //
    //////////////////////////////////////////////////////////////////////
    
    @FXML
    MFXButton backButton;
    
    @FXML
    public void initialize() {
        LayoutUtils.addIconToButton(backButton, "/icons/arrow-left.svg");
        backButton.getStyleClass().add("icon-button");
        backButton.setText("");
        
        TabGroup tabGroup = new TabGroup(
            "tab-button-active",
            Arrays.asList(
                new Pair<>(tabUsersButton, tabUsersPane),
                new Pair<>(tabRolesButton, tabRolesPane)
            ),
            "user-manager"
        );
        
        tabGroup.currentTabProperty().addListener(($1, $2, currentTab) -> {
            String tabText = currentTab.getKey().getText();
            if (Objects.equals(tabText, "Users")) {
                this.initializeUserPage();
            } else if (Objects.equals(tabText, "Roles")) {
                this.initializeRolePage();
            }
        });
        
        LayoutUtils.createResponsiveFlowPane(
            usersFlowPane,
            300,
            1,
            false
        );
        LayoutUtils.createResponsiveFlowPane(
            rolesFlowPane,
            300,
            1,
            false
        );
    }
    
    @FXML
    public void goBack() {
        SceneManager.setScene("base");
    }
}
