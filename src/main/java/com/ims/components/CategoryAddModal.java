package com.ims.components;

import com.ims.utils.TextFieldValidator;
import com.ims.utils.TextFieldValidatorSeverity;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;

public class CategoryAddModal extends Modal {
    public final MFXButton addButton = new MFXButton("Add");
    public final MFXTextField nameTextField = new MFXTextField();
    public final TextFieldValidator nameTextFieldValidator;
    
    public CategoryAddModal() {
        this.headerText.setText("Add Category");
        this.controlContainer.getChildren().add(addButton);
        nameTextField.setFloatingText("Category Name");
        this.contentContainer.add(nameTextField, 0, 0);
        nameTextField.setMinWidth(100);
        nameTextField.setMaxWidth(300);
        
        nameTextFieldValidator = new TextFieldValidator(
            nameTextField
        );
        
        nameTextFieldValidator.addConstraint(
            TextFieldValidatorSeverity.ERROR,
            "Please enter the category name.",
            () -> !nameTextField.getText().isEmpty(),
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
