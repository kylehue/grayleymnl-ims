package com.ims.components;

import com.ims.utils.LayoutUtils;
import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.time.LocalDate;

public class User extends GridPane {
    private final ObservableList<String> styleClass = this.getStyleClass();
    
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
        
        // Setup buttons
        MFXButton editButton = new MFXButton();
        editButton.setText("");
        LayoutUtils.addIconToButton(editButton, "/icons/pencil.svg");
        editButton.getStyleClass().add("icon-button");
        controlFlowPane.getChildren().add(editButton);
    }
    
    public void setEmail(String email) {
        Label label = new Label();
        label.setText(email);
        label.getStyleClass().add("user-email-label");
        this.add(label, 0, 0);
    }
    
    public void setRole(String role) {
        Label label = new Label();
        label.setText(role);
        label.getStyleClass().add("user-role-label");
        this.add(label, 0, 1);
    }
    
    public void setJoinedDate(String joinedDate) {
        Label label = new Label();
        label.setText("Joined: " + Utils.formatDate(LocalDate.parse(joinedDate)));
        label.getStyleClass().add("user-joined-date-label");
        this.add(label, 0, 2);
    }
    
    public void setLastActivityDate(String lastActivityDate) {
        Label label = new Label();
        label.setText("Last Activity: " + Utils.formatDate(LocalDate.parse(lastActivityDate)));
        label.getStyleClass().add("user-last-activity-date-label");
        this.add(label, 0, 3);
    }
}
