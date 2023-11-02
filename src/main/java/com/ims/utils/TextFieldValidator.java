package com.ims.utils;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class TextFieldValidator {
    private final String ERROR_STYLE_CLASS = "mfx-text-field-danger";
    private final String WARNING_STYLE_CLASS = "mfx-text-field-warning";
    private final String INFO_STYLE_CLASS = "mfx-text-field-info";
    private final Label messageLabel;
    private final MFXTextField textField;
    private String invalidMessage = "...";
    private Severity severity = null;
    private final BooleanProperty validProperty = new SimpleBooleanProperty(true);
    private final ArrayList<TextFieldValidatorConstraint> constraints = new ArrayList<>();
    
    public enum Severity {
        ERROR,
        WARNING,
        INFO
    }
    
    public TextFieldValidator(MFXTextField textField) {
        this.textField = textField;
        this.messageLabel = this.initializeMessageLabel(textField);
        
        this.validProperty.addListener(($1, $2, $3) -> {
            initializeStyleValidation();
        });
        
        SceneManager.onChangeScene((currentScene, oldScene) -> {
            if (currentScene != oldScene && textField.getText().isEmpty()) {
                this.reset();
            }
        });
    }
    
    public void addConstraint(
        Severity severity,
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
        constraint.validProperty.addListener(($1, $2, isValid) -> {
            if (!isValid) {
                this.invalidMessage = constraint.getInvalidMessage();
                this.severity = constraint.getSeverity();
                this.validProperty.set(false);
            } else {
                if (this.getInvalidConstraint() == null) {
                    this.validProperty.set(true);
                }
            }
        });
    }
    
    private void resetConstraints() {
        for (TextFieldValidatorConstraint constraint : constraints) {
            constraint.validProperty.set(true);
        }
    }
    
    public void reset() {
        this.resetConstraints();
        this.validProperty.set(true);
    }
    
    public boolean isValid() {
        return this.validProperty.get() && this.getInvalidConstraint() == null;
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
        if (this.validProperty.get()) {
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
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
        } else {
            if (this.severity == Severity.ERROR) {
                if (!textFieldStyleClass.contains(ERROR_STYLE_CLASS)) {
                    textFieldStyleClass.add(ERROR_STYLE_CLASS);
                    messageLabelStyleClass.add("text-danger");
                }
            } else if (this.severity == Severity.WARNING) {
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
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
        }
    }
    
    private Label initializeMessageLabel(MFXTextField textField) {
        Label label = new Label();
        label.setVisible(false);
        label.setManaged(false);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 0.9em;");
        
        label.visibleProperty().addListener((e) -> {
            double transitionDuration = 150;
            FadeTransition fadeInTransition = new FadeTransition(
                Duration.millis(transitionDuration), label
            );
            fadeInTransition.setFromValue(0);
            fadeInTransition.setToValue(1.0);
            fadeInTransition.setInterpolator(Interpolator.EASE_OUT);
            
            TranslateTransition translateTransition = new TranslateTransition(
                Duration.millis(transitionDuration), label
            );
            translateTransition.setFromY(-30);
            translateTransition.setToY(0);
            translateTransition.setInterpolator(Interpolator.EASE_OUT);
            
            ParallelTransition parallelTransition = new ParallelTransition(
                fadeInTransition, translateTransition
            );
            parallelTransition.play();
        });
        
        VBox wrapper = new VBox();
        wrapper.setSpacing(5);
        if (textField.getParent() instanceof GridPane parent) {
            int columnIndex = GridPane.getColumnIndex(textField) == null ? 0 : GridPane.getColumnIndex(textField);
            int rowIndex = GridPane.getRowIndex(textField) == null ? 0 : GridPane.getRowIndex(textField);
            parent.getChildren().remove(textField);
            wrapper.getChildren().addAll(textField, label);
            parent.add(wrapper, columnIndex, rowIndex);
        } else if (textField.getParent() instanceof Pane parent) {
            int index = parent.getChildren().indexOf(textField);
            parent.getChildren().remove(textField);
            wrapper.getChildren().addAll(textField, label);
            parent.getChildren().add(index, wrapper);
        } else {
            throw new Error("TextField's parent must be an instance of Pane.");
        }
        
        return label;
    }
    
    private static class TextFieldValidatorConstraint {
        public final BooleanProperty validProperty = new SimpleBooleanProperty(true);
        private final TextFieldValidator.Severity severity;
        private final String invalidMessage;
        
        public TextFieldValidatorConstraint(
            TextFieldValidator.Severity severity,
            String invalidMessage,
            Callable<Boolean> validityChecker,
            Observable... dependencies
        ) {
            this.severity = severity;
            this.invalidMessage = invalidMessage;
            for (Observable dep : dependencies) {
                dep.addListener((e) -> {
                    try {
                        this.validProperty.set(validityChecker.call());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }
        }
        
        public String getInvalidMessage() {
            return invalidMessage;
        }
        
        public TextFieldValidator.Severity getSeverity() {
            return severity;
        }
    }
}
