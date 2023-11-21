package com.ims.controller;

import com.ims.components.*;
import com.ims.model.BaseModel;
import com.ims.model.UserManagerModel;
import com.ims.model.objects.RoleObject;
import com.ims.model.objects.UserObject;
import com.ims.utils.SceneManager;
import com.ims.utils.LayoutUtils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
    MFXTextField searchRoleTextField;
    
    @FXML
    MFXScrollPane rolesScrollPane;
    
    @FXML
    FlowPane rolesFlowPane;
    
    HashMap<Integer, Role> roles = new HashMap<>();
    
    RoleAddModal roleAddModal = new RoleAddModal();
    
    public void initializeRolePage() {
        UserManagerModel.roleMap.addListener(
            (MapChangeListener<Integer, RoleObject>) change -> {
                int id = change.getKey();
                boolean isAddedAlready = roles.get(id) != null;
                boolean needsToBeAdded = change.wasAdded() && !isAddedAlready;
                boolean needsToBeUpdated = change.wasAdded() && isAddedAlready;
                boolean needsToBeRemoved = change.wasRemoved() && isAddedAlready;
                if (needsToBeAdded) {
                    addRole(change.getValueAdded());
                } else if (needsToBeUpdated) {
                    Role role = roles.get(id);
                    RoleObject roleObject = change.getValueAdded();
                    role.setRoleObject(roleObject);
                } else if (needsToBeRemoved) {
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
        
        roleAddModal.addButton.setOnMouseClicked(e -> {
            if (!roleAddModal.nameTextFieldValidator.isValid()) return;
            
            UserManagerModel.addRole(
                roleAddModal.nameTextField.getText()
            );
            
            roleAddModal.hide();
        });
    }
    
    public void initializeRoleLazyLoad() {
        // Load roles whenever the scrollbar hits the bottom.
        rolesScrollPane.vvalueProperty().addListener(($1, $2, scrollValue) -> {
            if (scrollValue.doubleValue() == 1) {
                UserManagerModel.loadRoles(12);
            }
        });
        
        // The listener above won't work if there is no scrollbar.
        // So here, we add components until the scroll pane gets a scrollbar.
        rolesScrollPane.viewportBoundsProperty().addListener(($1, $2, newValue) -> {
            double contentHeight = rolesFlowPane.getBoundsInLocal().getHeight();
            double viewportHeight = newValue.getHeight();
            if (contentHeight < viewportHeight) {
                UserManagerModel.loadRoles(4);
            }
        });
        
        // Everything above won't work if the `viewportBoundsProperty` doesn't trigger.
        // So here, we can trigger it by loading initial roles.
        UserManagerModel.loadRoles(12);
    }
    
    private Role addRole(RoleObject roleObject) {
        Role role = new Role(roleObject);
        Platform.runLater(() -> {
            this.roles.put(roleObject.getID(), role);
            
            rolesFlowPane.getChildren().add(
                this.getSortedRoles().indexOf(role),
                role
            );
        });
        return role;
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
    
    private ArrayList<Role> getSortedRoles() {
        ArrayList<Role> sortedRoles = new ArrayList<>(
            this.roles.values().stream().sorted(
                (a, b) -> {
                    return b.getRoleObject().getLastModified().compareTo(
                        a.getRoleObject().getLastModified()
                    );
                }
            ).toList()
        );
        
        return sortedRoles;
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
    FlowPane usersFlowPane;
    
    @FXML
    MFXScrollPane usersScrollPane;
    
    HashMap<Integer, User> users = new HashMap<>();
    
    public void initializeUserPage() {
        UserManagerModel.userMap.addListener(
            (MapChangeListener<Integer, UserObject>) change -> {
                int id = change.getKey();
                boolean isAddedAlready = users.get(id) != null;
                boolean needsToBeAdded = change.wasAdded() && !isAddedAlready;
                boolean needsToBeUpdated = change.wasAdded() && isAddedAlready;
                boolean needsToBeRemoved = change.wasRemoved() && isAddedAlready;
                if (needsToBeAdded) {
                    addUser(change.getValueAdded());
                } else if (needsToBeUpdated) {
                    User user = users.get(id);
                    UserObject userObject = change.getValueAdded();
                    user.setUserObject(userObject);
                } else if (needsToBeRemoved) {
                    removeUser(id);
                }
            }
        );
        
        LayoutUtils.applyVirtualScrolling(
            usersScrollPane,
            usersFlowPane
        );
        this.initializeUserLazyLoad();
    }
    
    public void initializeUserLazyLoad() {
        // Load users whenever the scrollbar hits the bottom.
        usersScrollPane.vvalueProperty().addListener(($1, $2, scrollValue) -> {
            if (scrollValue.doubleValue() == 1) {
                UserManagerModel.loadUsers(12);
            }
        });
        
        // The listener above won't work if there is no scrollbar.
        // So here, we add components until the scroll pane gets a scrollbar.
        usersScrollPane.viewportBoundsProperty().addListener(($1, $2, newValue) -> {
            double contentHeight = usersFlowPane.getBoundsInLocal().getHeight();
            double viewportHeight = newValue.getHeight();
            if (contentHeight < viewportHeight) {
                UserManagerModel.loadUsers(4);
            }
        });
        
        // Everything above won't work if the `viewportBoundsProperty` doesn't trigger.
        // So here, we can trigger it by loading initial users.
        UserManagerModel.loadUsers(12);
    }
    
    private User addUser(UserObject userObject) {
        User user = new User(userObject);
        Platform.runLater(() -> {
            this.users.put(userObject.getID(), user);
            
            usersFlowPane.getChildren().add(
                this.getSortedUsers().indexOf(user),
                user
            );
        });
        return user;
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
    
    private ArrayList<User> getSortedUsers() {
        ArrayList<User> sortedUsers = new ArrayList<>(
            this.users.values().stream().sorted(
                (a, b) -> {
                    return b.getUserObject().getJoinedDate().compareTo(
                        a.getUserObject().getJoinedDate()
                    );
                }
            ).toList()
        );
        
        return sortedUsers;
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
        
        LayoutUtils.createTabGroup(
            "tab-button-active",
            Arrays.asList(
                new Pair<>(tabUsersButton, tabUsersPane),
                new Pair<>(tabRolesButton, tabRolesPane)
            )
        );
        
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
        
        initializeUserPage();
        initializeRolePage();
    }
    
    @FXML
    public void goBack() {
        SceneManager.setScene("base");
    }
}
