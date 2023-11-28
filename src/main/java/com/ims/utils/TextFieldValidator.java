package com.ims.utils;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class TextFieldValidator {
    private final String ERROR_STYLE_CLASS = "mfx-text-field-danger";
    private final String WARNING_STYLE_CLASS = "mfx-text-field-warning";
    private final String INFO_STYLE_CLASS = "mfx-text-field-info";
    private final Label messageLabel;
    private final MFXTextField textField;
    private String invalidMessage = "...";
    private Severity severity = null;
    private final BooleanProperty validProperty = new SimpleBooleanProperty(true);
    private final ArrayList<Constraint> constraints = new ArrayList<>();
    
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
            this.reset();
        });
        
        textField.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            textField.positionCaret(textField.getText().length());
        });
    }
    
    public interface ValidityChecker {
        boolean call();
    }
    
    public interface ValidityCheckerAsync {
        AsyncCaller<Boolean> call();
    }
    
    private Constraint _addConstraint(
        Severity severity,
        String invalidMessage,
        ValidityChecker validityChecker,
        ValidityCheckerAsync validityCheckerAsync,
        Observable... dependencies
    ) {
        AtomicReference<Constraint> constraint = new AtomicReference<>(null);
        if (validityChecker != null) {
            constraint.set(new Constraint(
                severity,
                invalidMessage,
                validityChecker,
                dependencies
            ));
        } else {
            constraint.set(new Constraint(
                severity,
                invalidMessage,
                validityCheckerAsync,
                dependencies
            ));
        }
        
        constraints.add(constraint.get());
        constraint.get().validProperty.addListener(($1, $2, isValid) -> {
            if (!isValid) {
                this.invalidMessage = constraint.get().getInvalidMessage();
                this.severity = constraint.get().getSeverity();
                this.validProperty.set(false);
            } else {
                if (this.getInvalidConstraint() == null) {
                    this.validProperty.set(true);
                }
            }
        });
        
        return constraint.get();
    }
    
    public Constraint addConstraint(
        Severity severity,
        String invalidMessage,
        ValidityChecker validityChecker,
        Observable... dependencies
    ) {
        return this._addConstraint(
            severity,
            invalidMessage,
            validityChecker,
            null,
            dependencies
        );
    }
    
    public Constraint addConstraint(
        Severity severity,
        String invalidMessage,
        ValidityCheckerAsync validityCheckerAsync,
        Observable... dependencies
    ) {
        return this._addConstraint(
            severity,
            invalidMessage,
            null,
            validityCheckerAsync,
            dependencies
        );
    }
    
    public AsyncCaller<Boolean> validate() {
        return new AsyncCaller<>(task -> {
            for (Constraint constraint : constraints) {
                constraint.validate();
            }
            
            return this.validProperty.get() && this.getInvalidConstraint() == null;
        }, Utils.executor);
    }
    
    public static AsyncCaller<Boolean> validateAll(TextFieldValidator... validators) {
        return new AsyncCaller<>(task -> {
            Boolean isValid = true;
            for (TextFieldValidator validator : validators) {
                for (Constraint constraint : validator.constraints) {
                    constraint.validate();
                }
                
                isValid = validator.validProperty.get()
                    && validator.getInvalidConstraint() == null;
                
                if (!isValid) {
                    break;
                }
            }
            
            return isValid;
        }, Utils.executor);
    }
    
    private void resetConstraints() {
        for (Constraint constraint : constraints) {
            constraint.validProperty.set(true);
        }
    }
    
    public void reset() {
        this.resetConstraints();
        this.validProperty.set(true);
    }
    
    public boolean isValidSync() {
        this.validate();
        return this.validProperty.get() && this.getInvalidConstraint() == null;
    }
    
    private Constraint getInvalidConstraint() {
        for (Constraint constraint : constraints) {
            if (!constraint.validProperty.get()) {
                return constraint;
            }
        }
        return null;
    }
    
    private void initializeStyleValidation() {
        Platform.runLater(() -> {
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
        });
    }
    
    private Label initializeMessageLabel(MFXTextField textField) {
        Label label = new Label();
        label.setVisible(false);
        label.setManaged(false);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 0.9em;");
        
        label.visibleProperty().addListener((e) -> {
            Transition.fadeDown(label, 150);
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
    
    public static class Constraint {
        public final BooleanProperty validProperty = new SimpleBooleanProperty(true);
        private final TextFieldValidator.Severity severity;
        private final String invalidMessage;
        private ValidityChecker validityChecker = null;
        private ValidityCheckerAsync validityCheckerAsync = null;
        
        public Constraint(
            TextFieldValidator.Severity severity,
            String invalidMessage,
            ValidityChecker validityChecker,
            Observable... dependencies
        ) {
            this.severity = severity;
            this.invalidMessage = invalidMessage;
            this.validityChecker = validityChecker;
            for (Observable dep : dependencies) {
                dep.addListener((e) -> {
                    this.validate();
                });
            }
        }
        
        public Constraint(
            TextFieldValidator.Severity severity,
            String invalidMessage,
            ValidityCheckerAsync validityCheckerAsync,
            Observable... dependencies
        ) {
            this.severity = severity;
            this.invalidMessage = invalidMessage;
            this.validityCheckerAsync = validityCheckerAsync;
            for (Observable dep : dependencies) {
                dep.addListener((e) -> {
                    this.validate();
                });
            }
        }
        
        public void validate() {
            if (validityChecker != null) {
                this.validProperty.set(validityChecker.call());
            } else {
                try {
                    Boolean isValid = validityCheckerAsync.call().execute().getTask().get();
                    this.validProperty.set(isValid == null ? false : isValid);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
