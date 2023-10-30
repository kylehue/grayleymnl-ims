package com.ims.utils;

import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.validation.Constraint;
import io.github.palexdev.materialfx.validation.Severity;
import javafx.beans.binding.BooleanExpression;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;

public class TextFieldValidator {
    private final String ERROR_STYLE_CLASS = "mfx-text-field-danger";
    private final String WARNING_STYLE_CLASS = "mfx-text-field-warning";
    private final String INFO_STYLE_CLASS = "mfx-text-field-info";
    private final Label messageLabel;
    private final MFXTextField textField;
    private VBox wrapper;
    private boolean hasTyped = false;
    public TextFieldValidator(MFXTextField textField) {
        this.textField = textField;
        this.messageLabel = this.initializeMessageLabel(textField);
        
        textField.textProperty().addListener(($1, $2, text) -> {
            if (!hasTyped) {
                hasTyped = !text.isEmpty();
            }
            
            if (hasTyped) {
                initializeStyleValidation();
            }
        });
        
        SceneManager.onChangeScene((currentScene, oldScene) -> {
            initializeStyleValidation();
        });
    }
    
    private void initializeStyleValidation() {
        ObservableList<String> textFieldStyleClass = textField.getStyleClass();
        ObservableList<String> messageLabelStyleClass = messageLabel.getStyleClass();
        List<Constraint> constraints = textField.validate();
        if (
            textField.isValid() ||
                constraints.isEmpty() ||
                SceneManager.getCurrentScene() != textField.getScene()
        ) {
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
            Constraint violatedConstraint = constraints.get(0);
            if (violatedConstraint.getSeverity() == Severity.ERROR) {
                if (!textFieldStyleClass.contains(ERROR_STYLE_CLASS)) {
                    textFieldStyleClass.add(ERROR_STYLE_CLASS);
                    messageLabelStyleClass.add("text-danger");
                }
            } else if (violatedConstraint.getSeverity() == Severity.ERROR) {
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
            messageLabel.setText(violatedConstraint.getMessage());
            if (!wrapper.getChildren().contains(messageLabel)) {
                wrapper.getChildren().add(1, messageLabel);
            }
        }
    }
    
    public void addDependents(MFXTextField ...textFields) {
        for (MFXTextField tf : textFields) {
            tf.textProperty().addListener(($1, $2, text) -> {
                if (!hasTyped) {
                    hasTyped = !text.isEmpty();
                }
                
                if (hasTyped) {
                    initializeStyleValidation();
                }
            });
        }
    }
    
    public void addConstraint(
        Severity severity,
        String invalidMessage,
        BooleanExpression condition
    ) {
        Constraint constraint = Constraint.Builder.build()
            .setSeverity(severity)
            .setMessage(invalidMessage)
            .setCondition(condition)
            .get();
        
        textField.getValidator().constraint(constraint);
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
