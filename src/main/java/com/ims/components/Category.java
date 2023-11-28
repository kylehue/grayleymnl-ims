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
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class Category extends GridPane {
    private CategoryObject categoryObject;
    private final ObservableList<String> styleClass = this.getStyleClass();
    public final MFXTextField nameTextField = new MFXTextField();
    public final TextFieldValidator nameTextFieldValidator;
    public final MFXButton deleteButton = new MFXButton();
    private final StringProperty name = new SimpleStringProperty();
    
    public Category() {
        this.styleClass.add("card");
        this.styleClass.add("category-container");
        LayoutUtils.setupGridPane(this, 2, 1);
        
        // Setup name text field
        this.setFocusTraversable(true);
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
        
        nameTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            this.requestFocus();
        });
        
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
        
        deleteButton.setOnMouseClicked((e) -> {
            PopupService.confirmDialog.setup(
                "Delete Category",
                "Are you sure you want to delete this category?",
                "Delete",
                true,
                () -> {
                    BaseModel.removeCategory(categoryObject.getID());
                    PopupService.confirmDialog.hide();
                }
            ).show();
        });
        
        nameTextField.delegateFocusedProperty().addListener((e) -> {
            if (nameTextField.delegateIsFocused()) return;
            if (!this.nameTextFieldValidator.isValidSync()) {
                return;
            }
            
            BaseModel.updateCategory(categoryObject.getID(), this.getName());
        });
    }
    
    private boolean propertyListenersInitialized = false;
    private void initializePropertyListeners() {
        if (propertyListenersInitialized) return;
        propertyListenersInitialized = true;
        this.nameProperty().addListener(e -> {
            this.setName(this.nameProperty().get());
        });
        
        updateEditPermissions(UserSessionModel.currentUserIsAllowEditCategory());
        updateDeletePermissions(UserSessionModel.currentUserIsAllowDeleteCategory());
        SceneManager.onChangeScene((currentScene, oldScene) -> {
            updateEditPermissions(UserSessionModel.currentUserIsAllowEditCategory());
            updateDeletePermissions(UserSessionModel.currentUserIsAllowDeleteCategory());
            if (!currentScene.equals("base")) return;
            this.setCategoryObject(this.categoryObject);
        });
    }
    
    private void updateEditPermissions(boolean isAllowed) {
        Platform.runLater(() -> {
            nameTextField.setDisable(!isAllowed);
        });
    }
    
    private void updateDeletePermissions(boolean isAllowed) {
        Platform.runLater(() -> {
            deleteButton.setVisible(isAllowed);
            deleteButton.setManaged(isAllowed);
        });
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    private void setName(String name) {
        Platform.runLater(() -> {
            this.nameTextField.setText(name);
        });
    }
    
    public String getName() {
        return this.nameTextField.getText();
    }
    
    public void setCategoryObject(CategoryObject categoryObject) {
        initializePropertyListeners();
        this.categoryObject = categoryObject;
        this.nameProperty().unbind();
        this.nameProperty().bind(categoryObject.nameProperty());
    }
    
    public CategoryObject getCategoryObject() {
        return categoryObject;
    }
}
