package com.ims.components;

import com.ims.Config;
import com.ims.model.objects.CategoryObject;
import com.ims.utils.LayoutUtils;
import com.ims.utils.TextFieldValidator;
import com.ims.utils.TextFieldValidatorSeverity;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class Category extends GridPane {
    public final CategoryObject categoryObject;
    private final ObservableList<String> styleClass = this.getStyleClass();
    public final MFXTextField nameTextField = new MFXTextField();
    public final TextFieldValidator nameTextFieldValidator;
    public final MFXButton deleteButton = new MFXButton();
    public final MFXButton saveButton = new MFXButton();
    
    public Category(CategoryObject categoryObject) {
        this.categoryObject = categoryObject;
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
            TextFieldValidatorSeverity.ERROR,
            "Category name must be at most %s characters long.".formatted(
                Config.maxCategoryNameLength
            ),
            () -> nameTextField.getText().length() <= Config.maxCategoryNameLength,
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
        
        // Setup save button
        controlContainer.getChildren().add(saveButton);
        saveButton.getStyleClass().addAll("icon-button");
        saveButton.setText("");
        LayoutUtils.addIconToButton(saveButton, "/icons/content-save.svg");
        
        final double transitionDuration = 250;
        FadeTransition fadeInTransition = new FadeTransition(
            Duration.millis(transitionDuration), this
        );
        fadeInTransition.setFromValue(0);
        fadeInTransition.setToValue(1.0);
        fadeInTransition.setInterpolator(Interpolator.EASE_OUT);
        
        TranslateTransition translateTransition = new TranslateTransition(
            Duration.millis(transitionDuration), this
        );
        translateTransition.setFromY(30);
        translateTransition.setToY(0);
        translateTransition.setInterpolator(Interpolator.EASE_OUT);
        
        ParallelTransition parallelTransition = new ParallelTransition(
            fadeInTransition, translateTransition
        );
        parallelTransition.play();
    }
    
    public void setCategoryName(String name) {
        this.nameTextField.setText(name);
    }
    
    public String getCategoryName() {
        return this.nameTextField.getText();
    }
}
