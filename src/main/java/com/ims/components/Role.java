package com.ims.components;

import com.ims.Config;
import com.ims.model.BaseModel;
import com.ims.model.UserManagerModel;
import com.ims.model.objects.RoleObject;
import com.ims.utils.LayoutUtils;
import com.ims.utils.TextFieldValidator;
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
    public final RoleObject roleObject;
    public final TextFieldValidator nameTextFieldValidator;
    
    public Role(RoleObject roleObject) {
        this.roleObject = roleObject;
        this.styleClass.add("card");
        this.styleClass.add("role-container");
        
        // role_name, allow_delete_category, allow_add_category, allow_edit_category
        // allow_delete_product, allow_add_product, allow_edit_product, controls
        LayoutUtils.setupGridPane(this, 8, 1);
        
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
        LayoutUtils.addIconToButton(deleteButton, "/icons/delete.svg");
        
        // Setup save button
        MFXButton saveButton = new MFXButton();
        controlContainer.getChildren().add(saveButton);
        saveButton.getStyleClass().addAll("icon-button");
        saveButton.setText("");
        LayoutUtils.addIconToButton(saveButton, "/icons/content-save.svg");
        
        this.setName(roleObject.getName());
        this.setAllowAddCategory(roleObject.isAllowAddCategory());
        this.setAllowDeleteCategory(roleObject.isAllowDeleteCategory());
        this.setAllowEditCategory(roleObject.isAllowEditCategory());
        this.setAllowAddProduct(roleObject.isAllowAddProduct());
        this.setAllowDeleteProduct(roleObject.isAllowDeleteProduct());
        this.setAllowEditProduct(roleObject.isAllowEditProduct());
        
        nameTextFieldValidator = new TextFieldValidator(
            nameTextField
        );
        
        nameTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Please enter the role name.",
            () -> !nameTextField.getText().isEmpty(),
            nameTextField.textProperty(),
            saveButton.armedProperty()
        );
        
        nameTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Role name must be at most %s characters long.".formatted(
                Config.maxRoleNameLength
            ),
            () -> nameTextField.getText().length() <= Config.maxRoleNameLength,
            nameTextField.textProperty(),
            saveButton.armedProperty()
        );
        
        saveButton.setOnMouseClicked(e -> {
            if (!nameTextFieldValidator.isValid()) return;
            
            UserManagerModel.updateRole(
                roleObject.getID(),
                this.nameTextField.getText(),
                this.allowAddCategoryToggle.isSelected(),
                this.allowDeleteCategoryToggle.isSelected(),
                this.allowEditCategoryToggle.isSelected(),
                this.allowAddProductToggle.isSelected(),
                this.allowDeleteProductToggle.isSelected(),
                this.allowEditProductToggle.isSelected()
            );
            
            PopupService.messageDialog.setup(
                "Update Role",
                "Role has been successfully updated.",
                "Got it!"
            ).show();
        });
        
        deleteButton.setOnMouseClicked((e) -> {
            PopupService.confirmDialog.setup(
                "Delete Role",
                "Are you sure you want to delete this role?",
                "Delete",
                true,
                () -> {
                    UserManagerModel.removeRole(roleObject.getID());
                    PopupService.confirmDialog.hide();
                }
            ).show();
        });
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
    
    public String getName() {
        return this.nameTextField.getText();
    }
    
    public boolean isAllowAddCategory() {
        return this.allowAddCategoryToggle.isSelected();
    }
    
    public boolean isAllowDeleteCategory() {
        return this.allowDeleteCategoryToggle.isSelected();
    }
    
    public boolean isAllowEditCategory() {
        return this.allowEditCategoryToggle.isSelected();
    }
    
    public boolean isAllowAddProduct() {
        return this.allowAddProductToggle.isSelected();
    }
    
    public boolean isAllowDeleteProduct() {
        return this.allowDeleteProductToggle.isSelected();
    }
    
    public boolean isAllowEditProduct() {
        return this.allowEditProductToggle.isSelected();
    }
}
