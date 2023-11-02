package com.ims.controller;

import com.ims.Config;
import com.ims.canvas.network.Network;
import com.ims.database.DBUsers;
import com.ims.model.RegisterModel;
import com.ims.utils.*;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
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
    
    @FXML
    public void initialize() {
        // this.initializeNetworkAnimation();
        LayoutUtils.fitImageViewToParent(vectorImage);
        
        TextFieldValidator emailTextFieldValidator = new TextFieldValidator(emailTextField);
        emailTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Invalid email address.",
            () -> Utils.validateEmail(emailTextField.getText()),
            emailTextField.textProperty()
        );
        emailTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "This email address already exists.",
            () -> {
                String email = emailTextField.getText();
                // TODO: why is this getting triggered twice?
                // TODO: add delay before checking in database
                return DBUsers.get(DBUsers.Column.EMAIL, email).isEmpty();
            },
            registerButton.armedProperty()
        );
        emailTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Email must be at most %s characters long.".formatted(
                Config.maxEmailLength
            ),
            () -> emailTextField.getText().length() <= Config.maxEmailLength,
            emailTextField.textProperty()
        );

        TextFieldValidator passwordTextFieldValidator = new TextFieldValidator(passwordTextField);
        passwordTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Password must be at least 8 characters long.",
            () -> passwordTextField.getText().length() >= 8,
            passwordTextField.textProperty()
        );
        
        TextFieldValidator confirmPasswordTextFieldValidator = new TextFieldValidator(confirmPasswordTextField);
        confirmPasswordTextFieldValidator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "Passwords doesn't match.",
            () -> Objects.equals(
                passwordTextField.getText(),
                confirmPasswordTextField.getText()
            ),
            passwordTextField.textProperty(),
            confirmPasswordTextField.textProperty()
        );
        
        Utils.bindModelToTextField(RegisterModel.emailProperty, emailTextField);
        Utils.bindModelToTextField(RegisterModel.passwordProperty, passwordTextField);
        Utils.bindModelToTextField(RegisterModel.confirmPasswordProperty, confirmPasswordTextField);
        
        loginButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            SceneManager.setScene("login");
        });
        
        registerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            if (
                emailTextFieldValidator.isValid() &&
                    passwordTextFieldValidator.isValid() &&
                    confirmPasswordTextFieldValidator.isValid()
            ) {
                RegisterModel.register();
            }
        });
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
