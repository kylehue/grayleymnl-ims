package com.ims.components;

import io.github.palexdev.materialfx.controls.MFXButton;
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
        this.contentContainer.setMaxWidth(400);
        this.contentContainer.setMaxHeight(400);
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
