package com.ims.components;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public abstract class PopupService {
    public static final ConfirmDialog confirmDialog = new ConfirmDialog();
    public static final MessageDialog messageDialog = new MessageDialog();
    
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
}
