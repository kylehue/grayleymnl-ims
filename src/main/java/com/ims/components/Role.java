package com.ims.components;

import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class Role extends GridPane {
    private final ObservableList<String> styleClass = this.getStyleClass();
    private final MFXToggleButton allowAddCategoryToggle = new MFXToggleButton();
    private final MFXToggleButton allowDeleteCategoryToggle = new MFXToggleButton();
    private final MFXToggleButton allowEditCategoryToggle = new MFXToggleButton();
    private final MFXToggleButton allowAddProductToggle = new MFXToggleButton();
    private final MFXToggleButton allowDeleteProductToggle = new MFXToggleButton();
    private final MFXToggleButton allowEditProductToggle = new MFXToggleButton();
    private final MFXTextField nameTextField = new MFXTextField();
    public Role() {
        this.styleClass.add("card");
        this.styleClass.add("role-container");
        
        // role_name, allow_delete_category, allow_add_category, allow_edit_category
        // allow_delete_product, allow_add_product, allow_edit_product, controls
        Utils.setupGridPane(this, 8, 1);
        
        // Setup name text field
        this.add(nameTextField, 0, 0);
        this.nameTextField.setMaxWidth(Double.MAX_VALUE);
        this.nameTextField.setPrefWidth(USE_COMPUTED_SIZE);
        this.nameTextField.setFloatingText("Role Name");
        
        // Setup role toggles
        allowAddCategoryToggle.setText("Allow Add Category");
        this.add(allowAddCategoryToggle, 0, 1);
        allowDeleteCategoryToggle.setText("Allow Delete Category");
        this.add(allowDeleteCategoryToggle, 0, 2);
        allowEditCategoryToggle.setText("Allow Edit Category");
        this.add(allowEditCategoryToggle, 0, 3);
        allowAddProductToggle.setText("Allow Add Product");
        this.add(allowAddProductToggle, 0, 4);
        allowDeleteProductToggle.setText("Allow Delete Product");
        this.add(allowDeleteProductToggle, 0, 5);
        allowEditProductToggle.setText("Allow Edit Product");
        this.add(allowEditProductToggle, 0, 6);
        
        // Setup buttons
        HBox controlContainer = new HBox();
        this.add(controlContainer, 0, 7);
        controlContainer.setAlignment(Pos.CENTER_RIGHT);
        controlContainer.setPadding(new Insets(10, 0, 0, 0));
        
        // Setup delete button
        MFXButton deleteButton = new MFXButton();
        controlContainer.getChildren().add(deleteButton);
        deleteButton.getStyleClass().addAll("icon-button", "icon-button-danger");
        deleteButton.setText("");
        Utils.addIconToButton(deleteButton, "/icons/delete.svg");
        
        // Setup save button
        MFXButton saveButton = new MFXButton();
        controlContainer.getChildren().add(saveButton);
        saveButton.getStyleClass().addAll("icon-button");
        saveButton.setText("");
        Utils.addIconToButton(saveButton, "/icons/content-save.svg");
    }
    
    public void setAllowAddCategory(boolean v) {
        this.allowAddCategoryToggle.setSelected(v);
    }
    
    public void setAllowEditCategory(boolean v) {
        this.allowEditCategoryToggle.setSelected(v);
    }
    
    public void setAllowDeleteCategory(boolean v) {
        this.allowDeleteCategoryToggle.setSelected(v);
    }
    
    public void setAllowAddProduct(boolean v) {
        this.allowAddProductToggle.setSelected(v);
    }
    
    public void setAllowEditProduct(boolean v) {
        this.allowEditProductToggle.setSelected(v);
    }
    
    public void setAllowDeleteProduct(boolean v) {
        this.allowDeleteProductToggle.setSelected(v);
    }
    
    public void setName(String name) {
        this.nameTextField.setText(name);
    }
}
