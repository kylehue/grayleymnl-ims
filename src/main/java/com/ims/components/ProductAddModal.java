package com.ims.components;

import com.ims.Config;
import com.ims.model.BaseModel;
import com.ims.model.objects.CategoryObject;
import com.ims.utils.LayoutUtils;
import com.ims.utils.TextFieldValidator;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.scene.input.MouseEvent;

public class ProductAddModal extends Modal {
    public final MFXButton addButton = new MFXButton("Add");
    public final MFXTextField nameTextField = new MFXTextField();
    public final ComboBox<Integer, CategoryObject> categoryComboBox = new ComboBox<>();
    public final TextFieldValidator nameTextFieldValidator;
    public final TextFieldValidator categoryComboBoxValidator;
    
    public ProductAddModal() {
        this.headerText.setText("Add Product");
        
        this.controlContainer.getChildren().add(addButton);
        
        LayoutUtils.setupGridPane(this.contentContainer, 2, 1);
        this.contentContainer.setMaxWidth(400);
        this.contentContainer.setVgap(10);
        
        nameTextField.setFloatingText("Product Name");
        nameTextField.setMinWidth(100);
        nameTextField.setPrefWidth(300);
        nameTextField.setMaxWidth(Double.MAX_VALUE);
        this.contentContainer.add(nameTextField, 0, 0);
        
        nameTextFieldValidator = new TextFieldValidator(nameTextField);
        nameTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Please enter the product name.",
            () -> !nameTextField.getText().isEmpty(),
            nameTextField.textProperty(),
            addButton.armedProperty()
        );
        nameTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Product name must be at most %s characters long.".formatted(
                Config.maxProductNameLength
            ),
            () -> nameTextField.getText().length() <= Config.maxProductNameLength,
            nameTextField.textProperty(),
            addButton.armedProperty()
        );
        
        categoryComboBoxValidator = new TextFieldValidator(categoryComboBox.textField);
        categoryComboBoxValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Please enter the category name.",
            () -> categoryComboBox.getValue() != null,
            addButton.armedProperty()
        );
        
        categoryComboBox.textField.setFloatingText("Select Category");
        categoryComboBox.setMinWidth(100);
        categoryComboBox.setPrefWidth(300);
        categoryComboBox.setMaxWidth(Double.MAX_VALUE);
        this.contentContainer.add(categoryComboBox, 0, 1);
        
        categoryComboBox.setItems(BaseModel.categoryMap);
        categoryComboBox.setStringifier(CategoryObject::getName);
        
        this.setOnShown((e) -> {
            nameTextField.requestFocus();
        });
        
        this.setOnHidden((e) -> {
            nameTextField.clear();
            categoryComboBox.clearValue();
            nameTextFieldValidator.reset();
            categoryComboBoxValidator.reset();
        });
    }
}
