package com.ims.components;

import com.ims.Config;
import com.ims.utils.TextFieldValidator;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;

public class RoleAddModal extends Modal {
    public final MFXButton addButton = new MFXButton("Add");
    public final MFXTextField nameTextField = new MFXTextField();
    public final TextFieldValidator nameTextFieldValidator;
    
    public RoleAddModal() {
        this.headerText.setText("Add Role");
        this.controlContainer.getChildren().add(addButton);
        this.contentContainer.add(nameTextField, 0, 0);
        this.contentContainer.setMaxWidth(400);
        this.contentContainer.setMaxHeight(400);
        nameTextField.setFloatingText("Role Name");
        nameTextField.setMinWidth(100);
        nameTextField.setPrefWidth(300);
        nameTextField.setMaxWidth(Double.MAX_VALUE);
        
        nameTextFieldValidator = new TextFieldValidator(
            nameTextField
        );
        
        nameTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Please enter the role name.",
            () -> !nameTextField.getText().isEmpty(),
            nameTextField.textProperty(),
            addButton.armedProperty()
        );
        
        nameTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Role name must be at most %s characters long.".formatted(
                Config.maxRoleNameLength
            ),
            () -> nameTextField.getText().length() <= Config.maxRoleNameLength,
            nameTextField.textProperty(),
            addButton.armedProperty()
        );
        
        this.setOnShown((e) -> {
            nameTextField.requestFocus();
        });
        
        this.setOnHidden((e) -> {
            nameTextFieldValidator.reset();
        });
    }
}
