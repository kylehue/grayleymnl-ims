package com.ims.components;

import com.ims.Config;
import com.ims.utils.TextFieldValidator;
import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class CategoryAddModal extends Modal {
    public final MFXButton addButton = new MFXButton("Add");
    public final MFXTextField nameTextField = new MFXTextField();
    public final TextFieldValidator nameTextFieldValidator;
    private Utils.Callable<Void> listener = null;
    
    public CategoryAddModal() {
        this.headerText.setText("Add Category");
        this.controlContainer.getChildren().add(addButton);
        this.contentContainer.add(nameTextField, 0, 0);
        this.contentContainer.setMaxWidth(400);
        this.contentContainer.setMaxHeight(400);
        nameTextField.setFloatingText("Category Name");
        nameTextField.setMinWidth(100);
        nameTextField.setPrefWidth(300);
        nameTextField.setMaxWidth(Double.MAX_VALUE);
        nameTextField.setContextMenuDisabled(true);
        
        nameTextFieldValidator = new TextFieldValidator(
            nameTextField
        );
        
        nameTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Please enter the category name.",
            () -> !nameTextField.getText().isEmpty(),
            nameTextField.textProperty(),
            addButton.armedProperty()
        );
        
        nameTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Category name must be at most %s characters long.".formatted(
                Config.maxCategoryNameLength
            ),
            () -> nameTextField.getText().length() <= Config.maxCategoryNameLength,
            nameTextField.textProperty(),
            addButton.armedProperty()
        );
        
        this.setOnShown((e) -> {
            nameTextField.requestFocus();
        });
        
        this.setOnHidden((e) -> {
            nameTextFieldValidator.reset();
        });
        
        nameTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (this.listener == null) return;
            if (e.getCode() != KeyCode.ENTER) return;
            if (!nameTextFieldValidator.isValidSync()) return;
            this.listener.call();
        });
        
        addButton.setOnAction(e -> {
            if (this.listener == null) return;
            if (!nameTextFieldValidator.isValidSync()) return;
            this.listener.call();
        });
    }
    
    public void setOnAction(Utils.Callable<Void> listener) {
        this.listener = listener;
    }
}
