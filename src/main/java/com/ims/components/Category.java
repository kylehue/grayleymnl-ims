package com.ims.components;

import com.ims.utils.LayoutUtils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class Category extends GridPane {
    private final ObservableList<String> styleClass = this.getStyleClass();
    private final MFXTextField nameTextField = new MFXTextField();
    
    public Category() {
        this.styleClass.add("card");
        this.styleClass.add("category-container");
        LayoutUtils.setupGridPane(this, 2, 1);
        
        // Setup name text field
        this.add(nameTextField, 0, 0);
        this.nameTextField.setMaxWidth(Double.MAX_VALUE);
        this.nameTextField.setPrefWidth(USE_COMPUTED_SIZE);
        this.nameTextField.setFloatingText("Category Name");
        
        // Setup buttons
        HBox controlContainer = new HBox();
        this.add(controlContainer, 0, 1);
        controlContainer.setAlignment(Pos.CENTER_RIGHT);
        controlContainer.setPadding(new Insets(10, 0, 0, 0));
        
        // Setup delete button
        MFXButton deleteButton = new MFXButton();
        controlContainer.getChildren().add(deleteButton);
        deleteButton.getStyleClass().addAll("icon-button", "icon-button-danger");
        deleteButton.setText("");
        LayoutUtils.addIconToButton(deleteButton, "/icons/delete.svg");
        
        // Setup save button
        MFXButton saveButton = new MFXButton();
        controlContainer.getChildren().add(saveButton);
        saveButton.getStyleClass().addAll("icon-button");
        saveButton.setText("");
        LayoutUtils.addIconToButton(saveButton, "/icons/content-save.svg");
    }
    
    public void setName(String name) {
        this.nameTextField.setText(name);
    }
}
