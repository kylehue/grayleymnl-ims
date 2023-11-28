package com.ims.components;

import com.ims.Config;
import com.ims.model.UserManagerModel;
import com.ims.model.objects.RoleObject;
import com.ims.utils.LayoutUtils;
import com.ims.utils.SceneManager;
import com.ims.utils.TextFieldValidator;
import com.ims.utils.Transition;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    private final MFXButton deleteButton = new MFXButton();
    private RoleObject roleObject;
    public final TextFieldValidator nameTextFieldValidator;
    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty allowAddCategory = new SimpleBooleanProperty();
    private final BooleanProperty allowDeleteCategory = new SimpleBooleanProperty();
    private final BooleanProperty allowEditCategory = new SimpleBooleanProperty();
    private final BooleanProperty allowAddProduct = new SimpleBooleanProperty();
    private final BooleanProperty allowDeleteProduct = new SimpleBooleanProperty();
    private final BooleanProperty allowEditProduct = new SimpleBooleanProperty();
    private boolean muteSaveListener = false;
    
    public Role() {
        this.styleClass.add("card");
        this.styleClass.add("role-container");
        
        this.setFocusTraversable(true);
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
        controlContainer.getChildren().add(deleteButton);
        deleteButton.getStyleClass().addAll("icon-button", "icon-button-danger");
        deleteButton.setText("");
        LayoutUtils.addIconToButton(deleteButton, "/icons/delete.svg");
        
        nameTextFieldValidator = new TextFieldValidator(
            nameTextField
        );
        
        nameTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Please enter the role name.",
            () -> !nameTextField.getText().isEmpty(),
            nameTextField.textProperty(),
            nameTextField.delegateFocusedProperty()
        );
        
        nameTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Role name must be at most %s characters long.".formatted(
                Config.maxRoleNameLength
            ),
            () -> nameTextField.getText().length() <= Config.maxRoleNameLength,
            nameTextField.textProperty(),
            nameTextField.delegateFocusedProperty()
        );
        
        nameTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            this.requestFocus();
        });
        
        nameTextField.delegateFocusedProperty().addListener(e -> {
            if (muteSaveListener) {
                muteSaveListener = false;
                return;
            }
            if (!nameTextFieldValidator.isValidSync()) return;
            if (nameTextField.delegateIsFocused()) return;
            
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
        });
        
        EventHandler<ActionEvent> toggleListener = e -> {
            UserManagerModel.updateRole(
                roleObject.getID(),
                null,
                this.allowAddCategoryToggle.isSelected(),
                this.allowDeleteCategoryToggle.isSelected(),
                this.allowEditCategoryToggle.isSelected(),
                this.allowAddProductToggle.isSelected(),
                this.allowDeleteProductToggle.isSelected(),
                this.allowEditProductToggle.isSelected()
            );
        };
        
        allowAddCategoryToggle.setOnAction(toggleListener);
        allowDeleteCategoryToggle.setOnAction(toggleListener);
        allowEditCategoryToggle.setOnAction(toggleListener);
        allowAddProductToggle.setOnAction(toggleListener);
        allowDeleteProductToggle.setOnAction(toggleListener);
        allowEditProductToggle.setOnAction(toggleListener);
        
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
        
        Transition.fadeUp(this, 150);
    }
    
    private boolean propertyListenersInitialized = false;
    
    private void initializePropertyListeners() {
        if (propertyListenersInitialized) return;
        propertyListenersInitialized = true;
        this.nameProperty().addListener(e -> {
            this.setName(this.nameProperty().get());
        });
        this.allowAddCategoryProperty().addListener(e -> {
            this.setAllowAddCategory(
                this.allowAddCategoryProperty().get()
            );
        });
        this.allowDeleteCategoryProperty().addListener(e -> {
            this.setAllowDeleteCategory(
                this.allowDeleteCategoryProperty().get()
            );
        });
        this.allowEditCategoryProperty().addListener(e -> {
            this.setAllowEditCategory(
                this.allowEditCategoryProperty().get()
            );
        });
        this.allowAddProductProperty().addListener(e -> {
            this.setAllowAddProduct(
                this.allowAddProductProperty().get()
            );
        });
        this.allowDeleteProductProperty().addListener(e -> {
            this.setAllowDeleteProduct(
                this.allowDeleteProductProperty().get()
            );
        });
        this.allowEditProductProperty().addListener(e -> {
            this.setAllowEditProduct(
                this.allowEditProductProperty().get()
            );
        });
        
        SceneManager.onChangeScene((currentScene, oldScene) -> {
            if (!currentScene.equals("user-manager")) return;
            this.setRoleObject(this.roleObject);
        });
        
        UserManagerModel.isBusyRole.addListener(e -> {
            Platform.runLater(() -> {
                boolean isBusyRole = UserManagerModel.isBusyRole.get();
                allowAddCategoryToggle.setDisable(isBusyRole);
                allowDeleteCategoryToggle.setDisable(isBusyRole);
                allowEditCategoryToggle.setDisable(isBusyRole);
                allowAddProductToggle.setDisable(isBusyRole);
                allowDeleteProductToggle.setDisable(isBusyRole);
                allowEditProductToggle.setDisable(isBusyRole);
                deleteButton.setDisable(isBusyRole);
                nameTextField.setDisable(isBusyRole);
            });
        });
    }
    
    public void setRoleObject(RoleObject roleObject) {
        initializePropertyListeners();
        this.roleObject = roleObject;
        this.nameProperty().unbind();
        this.nameProperty().bind(roleObject.nameProperty());
        this.allowAddCategoryProperty().unbind();
        this.allowAddCategoryProperty().bind(roleObject.allowAddCategoryProperty());
        this.allowDeleteCategoryProperty().unbind();
        this.allowDeleteCategoryProperty().bind(roleObject.allowDeleteCategoryProperty());
        this.allowEditCategoryProperty().unbind();
        this.allowEditCategoryProperty().bind(roleObject.allowEditCategoryProperty());
        this.allowAddProductProperty().unbind();
        this.allowAddProductProperty().bind(roleObject.allowAddProductProperty());
        this.allowDeleteProductProperty().unbind();
        this.allowDeleteProductProperty().bind(roleObject.allowDeleteProductProperty());
        this.allowEditProductProperty().unbind();
        this.allowEditProductProperty().bind(roleObject.allowEditProductProperty());
    }
    
    public RoleObject getRoleObject() {
        return roleObject;
    }
    
    private void setAllowAddCategory(boolean v) {
        Platform.runLater(() -> {
            this.allowAddCategoryToggle.setSelected(v);
        });
    }
    
    private void setAllowEditCategory(boolean v) {
        Platform.runLater(() -> {
            this.allowEditCategoryToggle.setSelected(v);
        });
    }
    
    private void setAllowDeleteCategory(boolean v) {
        Platform.runLater(() -> {
            this.allowDeleteCategoryToggle.setSelected(v);
        });
    }
    
    private void setAllowAddProduct(boolean v) {
        Platform.runLater(() -> {
            this.allowAddProductToggle.setSelected(v);
        });
    }
    
    private void setAllowEditProduct(boolean v) {
        Platform.runLater(() -> {
            this.allowEditProductToggle.setSelected(v);
        });
    }
    
    private void setAllowDeleteProduct(boolean v) {
        Platform.runLater(() -> {
            this.allowDeleteProductToggle.setSelected(v);
        });
    }
    
    private void setName(String name) {
        Platform.runLater(() -> {
            this.nameTextField.setText(name);
        });
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getName() {
        return this.nameTextField.getText();
    }
    
    public BooleanProperty allowAddCategoryProperty() {
        return allowAddCategory;
    }
    
    public boolean isAllowAddCategory() {
        return this.allowAddCategoryToggle.isSelected();
    }
    
    public BooleanProperty allowDeleteCategoryProperty() {
        return allowDeleteCategory;
    }
    
    public boolean isAllowDeleteCategory() {
        return this.allowDeleteCategoryToggle.isSelected();
    }
    
    public BooleanProperty allowEditCategoryProperty() {
        return allowEditCategory;
    }
    
    public boolean isAllowEditCategory() {
        return this.allowEditCategoryToggle.isSelected();
    }
    
    public BooleanProperty allowAddProductProperty() {
        return allowAddProduct;
    }
    
    public boolean isAllowAddProduct() {
        return this.allowAddProductToggle.isSelected();
    }
    
    public BooleanProperty allowDeleteProductProperty() {
        return allowDeleteProduct;
    }
    
    public boolean isAllowDeleteProduct() {
        return this.allowDeleteProductToggle.isSelected();
    }
    
    public BooleanProperty allowEditProductProperty() {
        return allowEditProduct;
    }
    
    public boolean isAllowEditProduct() {
        return this.allowEditProductToggle.isSelected();
    }
}
