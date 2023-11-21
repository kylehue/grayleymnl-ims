package com.ims.components;

import com.ims.Config;
import com.ims.model.BaseModel;
import com.ims.model.UserSessionModel;
import com.ims.model.objects.CategoryObject;
import com.ims.utils.LayoutUtils;
import com.ims.utils.SceneManager;
import com.ims.utils.TextFieldValidator;
import com.ims.utils.Transition;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class Category extends GridPane {
    private CategoryObject categoryObject;
    private final ObservableList<String> styleClass = this.getStyleClass();
    public final MFXTextField nameTextField = new MFXTextField();
    public final TextFieldValidator nameTextFieldValidator;
    public final MFXButton deleteButton = new MFXButton();
    
    public Category(CategoryObject categoryObject) {
        this.setCategoryObject(categoryObject);
        this.setCategoryName(categoryObject.getName());
        this.styleClass.add("card");
        this.styleClass.add("category-container");
        LayoutUtils.setupGridPane(this, 2, 1);
        
        // Setup name text field
        this.add(nameTextField, 0, 0);
        this.nameTextField.setMaxWidth(Double.MAX_VALUE);
        this.nameTextField.setPrefWidth(USE_COMPUTED_SIZE);
        this.nameTextField.setFloatingText("Category Name");
        
        nameTextFieldValidator = new TextFieldValidator(nameTextField);
        nameTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Category name must be at most %s characters long.".formatted(
                Config.maxCategoryNameLength
            ),
            () -> nameTextField.getText().length() <= Config.maxCategoryNameLength,
            nameTextField.textProperty()
        );
        nameTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Please enter the category name.",
            () -> !nameTextField.getText().isEmpty(),
            nameTextField.textProperty()
        );
        
        // Setup buttons
        HBox controlContainer = new HBox();
        this.add(controlContainer, 0, 1);
        controlContainer.setAlignment(Pos.CENTER_RIGHT);
        controlContainer.setPadding(new Insets(10, 0, 0, 0));
        
        // Setup delete button
        controlContainer.getChildren().add(deleteButton);
        deleteButton.getStyleClass().addAll("icon-button", "icon-button-danger");
        deleteButton.setText("");
        LayoutUtils.addIconToButton(deleteButton, "/icons/delete.svg");
        
        Transition.fadeUp(this, 150);
        
        int id = this.categoryObject.getID();
        
        deleteButton.setOnMouseClicked((e) -> {
            PopupService.confirmDialog.setup(
                "Delete Category",
                "Are you sure you want to delete this category?",
                "Delete",
                true,
                () -> {
                    BaseModel.removeCategory(id);
                    PopupService.confirmDialog.hide();
                }
            ).show();
        });
        
        nameTextField.delegateFocusedProperty().addListener((e) -> {
            if (nameTextField.delegateIsFocused()) return;
            if (!this.nameTextFieldValidator.isValid()) {
                return;
            }

            BaseModel.updateCategory(id, this.getCategoryName());
            
            // PopupService.messageDialog.setup(
            //     "Update Category",
            //     "Category has been successfully updated.",
            //     "Got it!"
            // ).show();
        });
        
        SceneManager.onChangeScene(($1, $2) -> {
            this.setCategoryObject(this.categoryObject);
        });
        
        updateEditPermissions(UserSessionModel.currentUserIsAllowEditCategory());
        updateDeletePermissions(UserSessionModel.currentUserIsAllowDeleteCategory());
        UserSessionModel.currentUser.addListener(e -> {
            updateEditPermissions(UserSessionModel.currentUserIsAllowEditCategory());
            updateDeletePermissions(UserSessionModel.currentUserIsAllowDeleteCategory());
        });
    }
    
    private void updateEditPermissions(boolean isAllowed) {
        nameTextField.setDisable(!isAllowed);
    }
    
    private void updateDeletePermissions(boolean isAllowed) {
        deleteButton.setVisible(isAllowed);
        deleteButton.setManaged(isAllowed);
    }
    
    public void setCategoryName(String name) {
        this.nameTextField.setText(name);
    }
    
    public String getCategoryName() {
        return this.nameTextField.getText();
    }
    
    public void setCategoryObject(CategoryObject categoryObject) {
        this.categoryObject = categoryObject;
        this.setCategoryName(categoryObject.getName());
    }
    
    public CategoryObject getCategoryObject() {
        return categoryObject;
    }
}
