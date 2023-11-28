package com.ims.controller;

import com.ims.Config;
import com.ims.canvas.network.Network;
import com.ims.model.LoginModel;
import com.ims.model.RegisterModel;
import com.ims.utils.*;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.Objects;

public class RegisterController {
    @FXML
    private ImageView vectorImage;
    
    @FXML
    private Canvas networkCanvas;
    
    @FXML
    public MFXButton loginButton;
    
    @FXML
    public MFXButton registerButton;
    
    @FXML
    public MFXTextField emailTextField;
    
    @FXML
    public MFXPasswordField passwordTextField;
    
    @FXML
    public MFXPasswordField confirmPasswordTextField;
    
    private TextFieldValidator emailTextFieldValidator;
    private TextFieldValidator passwordTextFieldValidator;
    private TextFieldValidator confirmPasswordTextFieldValidator;
    
    @FXML
    public void initialize() {
        // this.initializeNetworkAnimation();
        LayoutUtils.fitImageViewToParent(vectorImage);
        emailTextField.setContextMenuDisabled(true);
        passwordTextField.setContextMenuDisabled(true);
        confirmPasswordTextField.setContextMenuDisabled(true);
        
        emailTextFieldValidator =
            new TextFieldValidator(emailTextField);
        emailTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "This email address already exists.",
            () -> {
                String email = emailTextField.getText();
                return RegisterModel.emailNotExists(email);
            },
            registerButton.armedProperty()
        );
        emailTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Invalid email address.",
            () -> Utils.validateEmail(emailTextField.getText()),
            emailTextField.textProperty(),
            registerButton.armedProperty()
        );
        emailTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Email must be at most %s characters long.".formatted(
                Config.maxEmailLength
            ),
            () -> emailTextField.getText().length() <= Config.maxEmailLength,
            emailTextField.textProperty(),
            registerButton.armedProperty()
        );
        
        passwordTextFieldValidator =
            new TextFieldValidator(passwordTextField);
        passwordTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Password must be at least 8 characters long.",
            () -> passwordTextField.getText().length() >= 8,
            passwordTextField.textProperty(),
            registerButton.armedProperty()
        );
        
        confirmPasswordTextFieldValidator =
            new TextFieldValidator(confirmPasswordTextField);
        confirmPasswordTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Passwords doesn't match.",
            () -> Objects.equals(
                passwordTextField.getText(),
                confirmPasswordTextField.getText()
            ),
            passwordTextField.textProperty(),
            confirmPasswordTextField.textProperty(),
            registerButton.armedProperty()
        );
        
        Utils.bindModelToTextField(RegisterModel.emailProperty, emailTextField);
        Utils.bindModelToTextField(RegisterModel.passwordProperty, passwordTextField);
        Utils.bindModelToTextField(RegisterModel.confirmPasswordProperty, confirmPasswordTextField);
        
        loginButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            SceneManager.setScene("login");
        });
        
        registerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            tryRegister();
        });
        
        RegisterModel.isBusyRegister.addListener(e -> {
            Platform.runLater(() -> {
                registerButton.setDisable(RegisterModel.isBusyRegister.get());
            });
        });
        
        emailTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            tryRegister();
        });
        
        passwordTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            tryRegister();
        });
        
        confirmPasswordTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            tryRegister();
        });
    }
    
    private void tryRegister() {
        TextFieldValidator.validateAll(
            emailTextFieldValidator,
            passwordTextFieldValidator,
            confirmPasswordTextFieldValidator
        ).onSucceeded(isValid -> {
            if (isValid) {
                RegisterModel.register();
            }
        }).execute();
    }
    
    private void initializeNetworkAnimation() {
        Network networkAnimation = new Network(networkCanvas);
        SceneManager.onChangeScene((newScene, oldScene) -> {
            if (newScene != "register") {
                networkAnimation.stop();
            } else {
                networkAnimation.start();
            }
        });
    }
}
