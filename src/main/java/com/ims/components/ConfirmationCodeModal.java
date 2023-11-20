package com.ims.components;

import com.ims.Config;
import com.ims.utils.CountdownTimer;
import com.ims.utils.LayoutUtils;
import com.ims.utils.TextFieldValidator;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class ConfirmationCodeModal extends Modal {
    public final MFXButton continueButton = new MFXButton("Continue");
    public final MFXButton resendCodeButton = new MFXButton("Resend Code");
    public final MFXTextField codeTextField = new MFXTextField();
    public final TextFieldValidator codeTextFieldValidator;
    private String correctCode;
    
    public ConfirmationCodeModal() {
        this.headerText.setText("Account Recovery");
        this.controlContainer.getChildren().addAll(resendCodeButton, continueButton);
        LayoutUtils.setupGridPane(this.contentContainer, 2, 1);
        
        Label message = new Label("To verify your identity, please enter the code that we sent to your email.");
        message.setWrapText(true);
        
        this.contentContainer.add(message, 0, 0);
        this.contentContainer.add(codeTextField, 0, 1);
        this.contentContainer.setMaxWidth(400);
        this.contentContainer.setMaxHeight(400);
        codeTextField.setFloatingText("Code");
        codeTextField.setMinWidth(100);
        codeTextField.setPrefWidth(300);
        codeTextField.setMaxWidth(Double.MAX_VALUE);
        
        codeTextFieldValidator = new TextFieldValidator(
            codeTextField
        );
        
        codeTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Invalid code.",
            () -> codeTextField.getText().equals(this.correctCode),
            continueButton.armedProperty()
        );
        
        this.setOnShown((e) -> {
            temporarilyDisableResend();
            codeTextField.requestFocus();
        });
        
        this.setOnHidden((e) -> {
            codeTextFieldValidator.reset();
        });
        
        resendCodeButton.getStyleClass().add("outline-button");
        resendCodeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            temporarilyDisableResend();
        });
    }
    
    public void setCorrectCode(String correctCode) {
        this.correctCode = correctCode;
    }
    
    public void temporarilyDisableResend() {
        if (resendCodeButton.isDisabled()) return;
        
        CountdownTimer countdownTimer = new CountdownTimer(60);
        countdownTimer.addListener(($1, $2, seconds) -> {
            Platform.runLater(() -> {
                if (seconds.intValue() > 0) {
                    resendCodeButton.setDisable(true);
                    resendCodeButton.setText(
                        "Try again in %s...".formatted(seconds.intValue())
                    );
                } else {
                    resendCodeButton.setDisable(false);
                    resendCodeButton.setText("Resend Code");
                }
            });
        });
        countdownTimer.start();
    }
}
