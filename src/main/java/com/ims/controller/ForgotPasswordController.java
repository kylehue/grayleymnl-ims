package com.ims.controller;

import com.ims.canvas.network.Network;
import com.ims.components.ConfirmationCodeModal;
import com.ims.components.PopupService;
import com.ims.database.DBUsers;
import com.ims.model.LoginModel;
import com.ims.model.RegisterModel;
import com.ims.model.UserManagerModel;
import com.ims.utils.*;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForgotPasswordController {
    @FXML
    private ImageView vectorImage;
    
    @FXML
    private Canvas networkCanvas;
    
    @FXML
    public MFXButton loginButton;
    
    @FXML
    public MFXButton continueButton;
    
    @FXML
    public MFXTextField emailTextField;
    
    private TextFieldValidator emailTextFieldValidator;
    
    ConfirmationCodeModal confirmationCodeModal = new ConfirmationCodeModal();
    private final BooleanProperty emailExistsProperty = new SimpleBooleanProperty(false);
    
    @FXML
    public void initialize() {
        // this.initializeNetworkAnimation();
        LayoutUtils.fitImageViewToParent(vectorImage);
        emailTextField.setContextMenuDisabled(true);
        
        loginButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            SceneManager.setScene("login");
        });
        
        emailTextFieldValidator = new TextFieldValidator(emailTextField);
        emailTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "This email address doesn't exist.",
            () -> emailExistsProperty.get(),
            continueButton.armedProperty(),
            emailExistsProperty
        );
        
        emailTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Invalid email address.",
            () -> Utils.validateEmail(emailTextField.getText()),
            emailTextField.textProperty(),
            continueButton.armedProperty()
        );
        
        Bindings.bindBidirectional(
            confirmationCodeModal.resendCodeButton.disableProperty(),
            continueButton.disableProperty()
        );
        
        emailTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            tryProceed();
        });
        
        continueButton.setOnMouseClicked(e -> {
            tryProceed();
        });
    }
    
    private void tryProceed() {
        String email = emailTextField.getText();
        
        RegisterModel.emailExists(email).onSucceeded(emailExists -> {
            emailExistsProperty.set(emailExists);
            
            if (!emailTextFieldValidator.isValid()) return;
            
            String confirmationCode = Utils.generateRandomCode(6);
            
            sendCode(email, confirmationCode);
            confirmationCodeModal.show();
            confirmationCodeModal.setCorrectCode(confirmationCode);
            
            confirmationCodeModal.resendCodeButton.setOnMouseClicked(ev -> {
                sendCode(email, confirmationCode);
            });
            
            confirmationCodeModal.setOnAction(() -> {
                confirmationCodeModal.hide();
                
                new AsyncCaller<Void>(task -> {
                    DBUsers.UserData userData = DBUsers.getOne(
                        DBUsers.Column.EMAIL,
                        email
                    );
                    
                    assert userData != null;
                    
                    // Change user's password
                    String newPassword = Utils.generateRandomCode(12);
                    String hashedPassword = Utils.hashPassword(newPassword);
                    
                    UserManagerModel.loadAndGetUser(userData.getID()).onSucceeded(userObject -> {
                        userObject.setPassword(hashedPassword);
                    }).execute();
                    
                    DBUsers.update(
                        userData.getID(),
                        hashedPassword,
                        null,
                        null,
                        null
                    );
                    
                    sendNewPassword(email, newPassword);
                    return null;
                }, Utils.executor).onSucceeded((e) -> {
                    PopupService.messageDialog.setup(
                        "Account Recovery",
                        "Your account has been successfully recovered. Your temporary password has been sent to your email. Please use it to log in and update your password.",
                        "Got it!"
                    ).show();
                }).execute();
                
                return null;
            });
        }).execute();
    }
    
    private void sendCode(String email, String code) {
        if (
            confirmationCodeModal.resendCodeButton.isDisabled() ||
                continueButton.isDisabled()
        ) {
            return;
        }
        Mail.send(
            email,
            "Account Recovery",
            "Dear User,\n\n" +
                "We received a request to reset the password associated with your account. If you initiated this request, please use the code below to change your password.\n\n" +
                "Code: %s\n\n".formatted(code) +
                "If you did not request a password reset, please ignore this email. Your account remains secure, and no changes have been made.\n\n" +
                "Thank you,\n" +
                "GrayleyMNL Inventory Management System"
        );
        temporarilyDisableSend();
        confirmationCodeModal.temporarilyDisableResend();
    }
    
    private void sendNewPassword(String email, String newPassword) {
        Mail.send(
            email,
            "Account Recovery",
            "Dear User,\n\n" +
                "Your account has been recovered successfully. Your temporary password is provided below:\n\n" +
                "Temporary Password: %s\n\n".formatted(newPassword) +
                "Please log in using this password and update it immediately.\n\n" +
                "Thank you,\n" +
                "GrayleyMNL Inventory Management System"
        );
    }
    
    private void temporarilyDisableSend() {
        if (continueButton.isDisabled()) return;
        
        CountdownTimer countdownTimer = new CountdownTimer(60);
        countdownTimer.addListener(($1, $2, seconds) -> {
            Platform.runLater(() -> {
                if (seconds.intValue() > 0) {
                    continueButton.setDisable(true);
                    continueButton.setText(
                        "Try again in %s...".formatted(seconds.intValue())
                    );
                } else {
                    continueButton.setDisable(false);
                    continueButton.setText("Continue");
                }
            });
        });
        countdownTimer.start();
    }
    
    private void initializeNetworkAnimation() {
        Network networkAnimation = new Network(networkCanvas);
        SceneManager.onChangeScene((newScene, oldScene) -> {
            if (!Objects.equals(newScene, "register")) {
                networkAnimation.stop();
            } else {
                networkAnimation.start();
            }
        });
    }
}
