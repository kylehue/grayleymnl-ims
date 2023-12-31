package com.ims.components;

import com.ims.model.UserEditModel;
import com.ims.model.UserManagerModel;
import com.ims.model.objects.RoleObject;
import com.ims.model.objects.UserObject;
import com.ims.utils.LayoutUtils;
import com.ims.utils.SceneManager;
import com.ims.utils.Transition;
import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.sql.Date;
import java.sql.Timestamp;

public class User extends GridPane {
    private final ObservableList<String> styleClass = this.getStyleClass();
    private final Label lastActivityDateLabel = new Label();
    private final Label joinedDateLabel = new Label();
    private final Label roleLabel = new Label();
    private final Label emailLabel = new Label();
    private final MFXButton editButton;
    private UserObject userObject;
    private final StringProperty email = new SimpleStringProperty();
    private final ObjectProperty<Integer> roleID = new SimpleObjectProperty<>();
    private final ObjectProperty<Date> joinedDate = new SimpleObjectProperty<>();
    private final ObjectProperty<Timestamp> lastActivityDate = new SimpleObjectProperty<>();
    private final BooleanProperty userDisabled = new SimpleBooleanProperty();
    private final BooleanProperty isOwner = new SimpleBooleanProperty();
    
    public User() {
        this.styleClass.add("card");
        this.styleClass.add("user-container");
        
        // email, role, joined_date, last_activity_date, controls
        // Setup main row and columns
        LayoutUtils.setupGridPane(this, 5, 1);
        
        // Setup grid pane for controls
        GridPane controlGridPane = LayoutUtils.createGridPane(1, 1);
        this.add(controlGridPane, 0, 4);
        RowConstraints controlRow = controlGridPane.getRowConstraints().get(0);
        controlRow.setFillHeight(false);
        controlRow.setValignment(VPos.BOTTOM);
        FlowPane controlFlowPane = new FlowPane();
        controlFlowPane.setAlignment(Pos.BOTTOM_RIGHT);
        controlGridPane.add(controlFlowPane, 0, 0);
        for (int i = 0; i < this.getRowConstraints().size(); i++) {
            this.getRowConstraints().get(i).setVgrow(Priority.NEVER);
        }
        
        // Setup buttons
        editButton = new MFXButton();
        editButton.setText("");
        LayoutUtils.addIconToButton(editButton, "/icons/pencil.svg");
        editButton.getStyleClass().add("icon-button");
        controlFlowPane.getChildren().add(editButton);
        
        // Setup labels
        lastActivityDateLabel.getStyleClass().add("user-last-activity-date-label");
        this.add(lastActivityDateLabel, 0, 3);
        joinedDateLabel.getStyleClass().add("user-joined-date-label");
        this.add(joinedDateLabel, 0, 2);
        roleLabel.getStyleClass().add("user-role-label");
        this.add(roleLabel, 0, 1);
        emailLabel.getStyleClass().add("user-email-label");
        this.add(emailLabel, 0, 0);
        
        editButton.setOnMouseClicked(e -> {
            UserEditModel.currentUser.set(this.getUserObject());
            SceneManager.setScene("user");
        });
        
        Transition.fadeUp(this, 150);
    }
    
    private boolean propertyListenersInitialized = false;
    
    private void initializePropertyListeners() {
        if (propertyListenersInitialized) return;
        propertyListenersInitialized = true;
        
        this.emailProperty().addListener(e -> {
            this.setEmail(this.emailProperty().get());
        });
        this.roleIDProperty().addListener(e -> {
            this.setRole(this.roleIDProperty().get());
        });
        this.joinedDateProperty().addListener(e -> {
            this.setJoinedDate(this.joinedDateProperty().get());
        });
        this.lastActivityDateProperty().addListener(e -> {
            this.setLastActivityDate(this.lastActivityDateProperty().get());
        });
        this.userDisabledProperty().addListener(e -> {
            this.setUserDisabled(this.userDisabledProperty().get());
        });
        this.isOwnerProperty().addListener(e -> {
            this.setRole(this.roleIDProperty().get());
            editButton.setVisible(!this.isOwnerProperty().get());
            editButton.setManaged(!this.isOwnerProperty().get());
        });
    }
    
    public StringProperty emailProperty() {
        return email;
    }
    
    private void setEmail(String email) {
        Platform.runLater(() -> {
            emailLabel.setText(email);
        });
    }
    
    public ObjectProperty<Integer> roleIDProperty() {
        return roleID;
    }
    
    private final ChangeListener<String> roleChangeListener = (e, oldValue, newValue) -> {
        Platform.runLater(() -> {
            roleLabel.setText(newValue);
        });
    };
    
    private RoleObject oldRoleObject = null;
    
    private void setRole(Integer roleID) {
        Platform.runLater(() -> {
            if (this.isOwnerProperty().get()) {
                roleLabel.setText("Owner");
                return;
            }
            if (roleID == null) {
                roleLabel.setText("No Role Assigned");
                return;
            }
            UserManagerModel.loadAndGetRole(roleID).onSucceeded(roleObject -> {
                if (roleObject == null) return;
                roleLabel.setText(roleObject.getName());
                if (oldRoleObject != null) {
                    oldRoleObject.nameProperty().removeListener(roleChangeListener);
                }
                roleObject.nameProperty().addListener(roleChangeListener);
                oldRoleObject = roleObject;
            }).execute();
        });
    }
    
    public ObjectProperty<Date> joinedDateProperty() {
        return joinedDate;
    }
    
    private void setJoinedDate(Date joinedDate) {
        Platform.runLater(() -> {
            joinedDateLabel.setText(
                "Joined: " + Utils.formatDate(joinedDate.toLocalDate().atTime(0, 0))
            );
        });
    }
    
    public ObjectProperty<Timestamp> lastActivityDateProperty() {
        return lastActivityDate;
    }
    
    private void setLastActivityDate(Timestamp lastActivityDate) {
        Platform.runLater(() -> {
            lastActivityDateLabel.setText(
                "Last Activity: " + Utils.formatDate(
                    lastActivityDate.toLocalDateTime()
                )
            );
        });
    }
    
    public BooleanProperty userDisabledProperty() {
        return userDisabled;
    }
    
    public void setUserDisabled(boolean userDisabled) {
        Platform.runLater(() -> {
            if (userDisabled) {
                this.styleClass.add("user-container-disabled");
            } else {
                this.styleClass.remove("user-container-disabled");
            }
        });
    }
    
    public UserObject getUserObject() {
        return userObject;
    }
    
    public BooleanProperty isOwnerProperty() {
        return isOwner;
    }
    
    public void setUserObject(UserObject userObject) {
        initializePropertyListeners();
        this.userObject = userObject;
        this.emailProperty().unbind();
        this.emailProperty().bind(userObject.emailProperty());
        this.roleIDProperty().unbind();
        this.roleIDProperty().bind(userObject.roleIDProperty());
        this.joinedDateProperty().unbind();
        this.joinedDateProperty().bind(userObject.joinedDateProperty());
        this.lastActivityDateProperty().unbind();
        this.lastActivityDateProperty().bind(userObject.lastActivityDateProperty());
        this.userDisabledProperty().unbind();
        this.userDisabledProperty().bind(userObject.isDisabledProperty());
        this.isOwnerProperty().unbind();
        this.isOwnerProperty().bind(userObject.isOwnerProperty());
    }
}
