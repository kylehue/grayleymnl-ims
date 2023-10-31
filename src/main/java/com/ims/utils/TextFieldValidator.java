package com.ims.utils;

import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.validation.Severity;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class TextFieldValidator {
    private final String ERROR_STYLE_CLASS = "mfx-text-field-danger";
    private final String WARNING_STYLE_CLASS = "mfx-text-field-warning";
    private final String INFO_STYLE_CLASS = "mfx-text-field-info";
    private final Label messageLabel;
    private final MFXTextField textField;
    private VBox wrapper;
    private String invalidMessage = "...";
    private TextFieldValidatorSeverity severity = null;
    private final BooleanProperty isValid = new SimpleBooleanProperty(true);
    private ArrayList<TextFieldValidatorConstraint> constraints = new ArrayList<>();
    
    public TextFieldValidator(MFXTextField textField) {
        this.textField = textField;
        this.messageLabel = this.initializeMessageLabel(textField);
  
        this.isValid.addListener(($1, $2, $3) -> {
            initializeStyleValidation();
        });

        SceneManager.onChangeScene((currentScene, oldScene) -> {
            if (currentScene != oldScene && textField.getText().isEmpty()) {
                this.resetConstraints();
                this.isValid.set(true);
            }
        });
    }
    
    public void addConstraint(
        TextFieldValidatorSeverity severity,
        String invalidMessage,
        Callable<Boolean> validityChecker,
        Observable... dependencies
    ) {
        TextFieldValidatorConstraint constraint = new TextFieldValidatorConstraint(
            severity,
            invalidMessage,
            validityChecker,
            dependencies
        );
        
        constraints.add(constraint);
        constraint.validProperty.addListener(($1, $2, isValid) ->{
            if (!isValid) {
                this.invalidMessage = constraint.getInvalidMessage();
                this.severity = constraint.getSeverity();
                this.isValid.set(false);
            } else {
                if (this.getInvalidConstraint() == null) {
                    this.isValid.set(true);
                }
            }
        });
    }
    
    private void resetConstraints() {
        for (TextFieldValidatorConstraint constraint : constraints) {
            constraint.validProperty.set(true);
        }
    }
    
    private TextFieldValidatorConstraint getInvalidConstraint() {
        for (TextFieldValidatorConstraint constraint : constraints) {
            if (!constraint.validProperty.get()) {
                return constraint;
            }
        }
        return null;
    }
    
    private void initializeStyleValidation() {
        ObservableList<String> textFieldStyleClass = textField.getStyleClass();
        ObservableList<String> messageLabelStyleClass = messageLabel.getStyleClass();
        if (this.isValid.get()) {
            textFieldStyleClass.removeAll(
                INFO_STYLE_CLASS,
                WARNING_STYLE_CLASS,
                ERROR_STYLE_CLASS
            );
            messageLabelStyleClass.removeAll(
                "text-info",
                "text-warning",
                "text-error"
            );
            messageLabel.setText("");
            if (wrapper.getChildren().contains(messageLabel)) {
                wrapper.getChildren().remove(messageLabel);
            }
        } else {
            if (this.severity == TextFieldValidatorSeverity.ERROR) {
                if (!textFieldStyleClass.contains(ERROR_STYLE_CLASS)) {
                    textFieldStyleClass.add(ERROR_STYLE_CLASS);
                    messageLabelStyleClass.add("text-danger");
                }
            } else if (this.severity == TextFieldValidatorSeverity.WARNING) {
                if (!textFieldStyleClass.contains(WARNING_STYLE_CLASS)) {
                    textFieldStyleClass.add(WARNING_STYLE_CLASS);
                    messageLabelStyleClass.add("text-warning");
                }
            } else {
                if (!textFieldStyleClass.contains(INFO_STYLE_CLASS)) {
                    textFieldStyleClass.add(INFO_STYLE_CLASS);
                    messageLabelStyleClass.add("text-info");
                }
            }
            messageLabel.setText(this.invalidMessage);
            if (!wrapper.getChildren().contains(messageLabel)) {
                wrapper.getChildren().add(1, messageLabel);
            }
        }
    }
    
    private Label initializeMessageLabel(MFXTextField textField) {
        Label label = new Label();
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 0.9em;");
        VBox wrapper = new VBox();
        wrapper.setSpacing(5);
        this.wrapper = wrapper;
        if (textField.getParent() instanceof GridPane parent) {
            int columnIndex = GridPane.getColumnIndex(textField) == null ? 0 : GridPane.getColumnIndex(textField);
            int rowIndex = GridPane.getRowIndex(textField) == null ? 0 : GridPane.getRowIndex(textField);
            parent.getChildren().remove(textField);
            wrapper.getChildren().add(textField);
            parent.add(wrapper, columnIndex, rowIndex);
        } else if (textField.getParent() instanceof Pane parent) {
            int index = parent.getChildren().indexOf(textField);
            parent.getChildren().remove(textField);
            wrapper.getChildren().add(textField);
            parent.getChildren().add(index, wrapper);
        } else {
            throw new Error("TextField's parent must be an instance of Pane.");
        }
        
        return label;
    }
}
