package com.ims.components;

import com.ims.model.UserSessionModel;
import com.ims.utils.LayoutUtils;
import com.ims.utils.TextFieldValidator;
import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public abstract class PopupService {
    public static final ConfirmDialog confirmDialog = new ConfirmDialog();
    public static final MessageDialog messageDialog = new MessageDialog();
    public static final ConfirmWithAuthModal confirmWithAuthModal = new ConfirmWithAuthModal();
    
    /**
     * Confirmation dialog
     */
    public static class ConfirmDialog extends Modal {
        public final MFXButton confirmButton = new MFXButton();
        
        private final Label messageLabel = new Label();
        private ConfirmListener confirmListener = null;
        
        public ConfirmDialog() {
            this.controlContainer.getChildren().add(confirmButton);
            
            this.contentContainer.add(messageLabel, 0, 0);
            this.contentContainer.setMaxWidth(400);
            this.contentContainer.setMaxHeight(400);
            
            this.setup(
                "Confirm",
                "Are you sure?",
                "Confirm",
                false,
                () -> {}
            );
            
            this.showingProperty().addListener(e -> {
                if (this.isShowing()) {
                    confirmButton.requestFocus();
                    return;
                }
                confirmListener = null;
            });
            
            confirmButton.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                if (this.confirmListener == null) return;
                if (e.getCode() != KeyCode.ENTER) return;
                this.confirmListener.call();
            });
        }
        
        public interface ConfirmListener {
            void call();
        }
        
        public ConfirmDialog setup(
            String headerText,
            String message,
            String confirmText,
            boolean isDanger,
            ConfirmListener confirmListener
        ) {
            this.headerText.setText(headerText);
            this.messageLabel.setText(message);
            this.confirmButton.setText(confirmText);
            this.confirmListener = confirmListener;
            
            if (isDanger) {
                confirmButton.getStyleClass().add("button-danger");
            } else {
                confirmButton.getStyleClass().remove("button-danger");
            }
            
            this.confirmButton.setOnMouseClicked(e -> {
                confirmListener.call();
            });
            
            return this;
        }
    }
    
    /**
     * Message dialog
     */
    public static class MessageDialog extends Modal {
        public final MFXButton okButton = new MFXButton();
        
        private final Label messageLabel = new Label();
        
        public MessageDialog() {
            this.controlContainer.getChildren().add(okButton);
            
            messageLabel.setWrapText(true);
            this.contentContainer.add(messageLabel, 0, 0);
            this.contentContainer.setMaxWidth(400);
            this.contentContainer.setMaxHeight(400);
            
            this.setup(
                "Message",
                "This is a sample message.",
                "Got it!"
            );
            
            okButton.setOnMouseClicked(e -> {
                this.hide();
            });
            
            
            this.showingProperty().addListener(e -> {
                if (this.isShowing()) {
                    okButton.requestFocus();
                }
            });
            
            okButton.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() != KeyCode.ENTER) return;
                this.hide();
            });
        }
        
        public MessageDialog setup(
            String headerText,
            String message,
            String confirmText
        ) {
            this.headerText.setText(headerText);
            this.messageLabel.setText(message);
            this.okButton.setText(confirmText);
            
            return this;
        }
    }
    
    /**
     * Confirmation modal with authentication.
     */
    public static class ConfirmWithAuthModal extends Modal {
        public final MFXButton confirmButton = new MFXButton();
        public final MFXTextField emailTextField = new MFXTextField();
        public final MFXPasswordField passwordField = new MFXPasswordField();
        public final TextFieldValidator emailTextFieldValidator;
        public final TextFieldValidator passwordFieldValidator;
        private Utils.Callable<Void> listener = null;
        
        public ConfirmWithAuthModal() {
            confirmButton.getStyleClass().add("button-danger");
            this.controlContainer.getChildren().add(confirmButton);
            
            LayoutUtils.setupGridPane(this.contentContainer, 2, 1);
            this.contentContainer.add(emailTextField, 0, 0);
            this.contentContainer.add(passwordField, 0, 1);
            this.contentContainer.setMaxWidth(400);
            this.contentContainer.setMaxHeight(400);
            
            emailTextField.setFloatingText("Enter your email");
            emailTextField.setMinWidth(100);
            emailTextField.setPrefWidth(300);
            emailTextField.setMaxWidth(Double.MAX_VALUE);
            
            emailTextFieldValidator = new TextFieldValidator(
                emailTextField
            );
            
            emailTextFieldValidator.addConstraint(
                TextFieldValidator.Severity.ERROR,
                "Invalid email.",
                () -> emailTextField.getText().equals(
                    UserSessionModel.getCurrentUserEmail()
                ),
                confirmButton.armedProperty()
            );
            
            passwordField.setFloatingText("Enter your password");
            passwordField.setMinWidth(100);
            passwordField.setPrefWidth(300);
            passwordField.setMaxWidth(Double.MAX_VALUE);
            
            passwordFieldValidator = new TextFieldValidator(
                passwordField
            );
            
            passwordFieldValidator.addConstraint(
                TextFieldValidator.Severity.ERROR,
                "Invalid password.",
                () -> Utils.checkPassword(
                    passwordField.getText(),
                    UserSessionModel.getCurrentUserPassword()
                ),
                confirmButton.armedProperty()
            );
            
            this.setOnShown((e) -> {
                emailTextField.requestFocus();
            });
            
            this.setOnHidden((e) -> {
                emailTextField.setText("");
                passwordField.setText("");
                emailTextFieldValidator.reset();
                passwordFieldValidator.reset();
                listener = null;
            });
            
            emailTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() != KeyCode.ENTER) return;
                doAction();
            });
            
            passwordField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() != KeyCode.ENTER) return;
                doAction();
            });
            
            confirmButton.setOnAction(e -> {
                doAction();
            });
        }
        
        private boolean isValid() {
            boolean emailIsValid = emailTextFieldValidator.isValid();
            boolean passwordIsValid = passwordFieldValidator.isValid();
            return emailIsValid && passwordIsValid;
        }
        
        private void doAction() {
            if (this.listener == null) return;
            if (!isValid()) return;
            this.listener.call();
        }
        
        public void setOnAction(Utils.Callable<Void> listener) {
            this.listener = listener;
        }
        
        public ConfirmWithAuthModal setup(
            String headerText,
            String confirmText,
            ConfirmDialog.ConfirmListener confirmListener
        ) {
            this.headerText.setText(headerText);
            this.confirmButton.setText(confirmText);
            this.setOnAction(() -> {
                confirmListener.call();
                return null;
            });
            
            return this;
        }
    }
    
}
