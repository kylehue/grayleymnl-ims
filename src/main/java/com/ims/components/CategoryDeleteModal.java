package com.ims.components;

import com.ims.utils.TextFieldValidator;
import com.ims.utils.TextFieldValidatorSeverity;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.scene.control.Label;

public class CategoryDeleteModal extends Modal {
    public final MFXButton deleteButton = new MFXButton("Delete");
    private final Label label = new Label();
    private String categoryName = "";
    
    public CategoryDeleteModal() {
        this.headerText.setText("Delete Category");
        deleteButton.getStyleClass().add("button-danger");
        this.controlContainer.getChildren().add(deleteButton);
        this.contentContainer.add(label, 0, 0);
        label.setWrapText(true);
        this.setOnShown((e) -> {
            label.setText(
                "Are you sure you want to delete the '%s' category?".formatted(
                    this.categoryName
                )
            );
        });
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}