package com.ims.components;

import com.ims.model.UserEditModel;
import com.ims.model.UserManagerModel;
import com.ims.model.objects.RoleObject;
import com.ims.model.objects.UserObject;
import com.ims.utils.LayoutUtils;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
    private UserObject userObject;
    
    public User(UserObject userObject) {
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
        
        // Setup buttons
        MFXButton editButton = new MFXButton();
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
        
        this.setUserObject(userObject);
    }
    
    public void setEmail(String email) {
        emailLabel.setText(email);
    }
    
    private final ChangeListener<String> roleChangeListener = (e, oldValue, newValue) -> {
        Platform.runLater(() -> {
            roleLabel.setText(newValue);
        });
    };
    
    private RoleObject oldRoleObject = null;
    
    public void setRole(int roleID) {
        RoleObject roleObject = UserManagerModel.loadAndGetRole(roleID);
        if (roleObject == null) return;
        roleLabel.setText(roleObject.getName());
        if (oldRoleObject != null) {
            oldRoleObject.nameProperty().removeListener(roleChangeListener);
        }
        roleObject.nameProperty().addListener(roleChangeListener);
        oldRoleObject = roleObject;
    }
    
    public void setJoinedDate(Date joinedDate) {
        joinedDateLabel.setText("Joined: " + Utils.formatDate(joinedDate.toLocalDate()));
    }
    
    public void setLastActivityDate(Timestamp lastActivityDate) {
        lastActivityDateLabel.setText("Last Activity: " + Utils.formatDate(lastActivityDate.toLocalDateTime().toLocalDate()));
    }
    
    public UserObject getUserObject() {
        return userObject;
    }
    
    public void setUserObject(UserObject userObject) {
        this.userObject = userObject;
        
        this.setEmail(
            userObject.getEmail()
        );
        this.setRole(
            userObject.getRoleID()
        );
        this.setJoinedDate(
            userObject.getJoinedDate()
        );
        this.setLastActivityDate(
            userObject.getLastActivityDate()
        );
        
        if (userObject.isDisabled()) {
            this.styleClass.add("user-container-disabled");
        } else {
            this.styleClass.remove("user-container-disabled");
        }
    }
}
