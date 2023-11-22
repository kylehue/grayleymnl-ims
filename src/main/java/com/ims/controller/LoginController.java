package com.ims.controller;

import com.ims.canvas.network.Network;
import com.ims.model.LoginModel;
import com.ims.utils.*;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class LoginController {
    @FXML
    private Label welcomeText;
    
    @FXML
    private ImageView vectorImage;
    
    @FXML
    private Canvas networkCanvas;
    
    @FXML
    private MFXButton loginButton;
    
    @FXML
    public MFXButton registerButton;
    
    @FXML
    public MFXButton forgotPasswordButton;
    
    @FXML
    public MFXTextField emailTextField;
    
    @FXML
    public MFXPasswordField passwordTextField;
    
    @FXML
    public void initialize() {
        this.initializeNetworkAnimation();
        LayoutUtils.fitImageViewToParent(vectorImage);
        
        loginButton.setOnMouseClicked((MouseEvent event) -> {
            LoginModel.login();
        });
        
        emailTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            LoginModel.login();
        });
        
        passwordTextField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            LoginModel.login();
        });
        
        registerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            SceneManager.setScene("register");
        });
        
        forgotPasswordButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            SceneManager.setScene("forgot-password");
        });
        
        TextFieldValidator validator = new TextFieldValidator(passwordTextField);
        validator.addConstraint(
            TextFieldValidator.Severity.ERROR,
            "The email or password is invalid.",
            () -> LoginModel.validProperty.get(),
            LoginModel.validProperty
        );
        
        SceneManager.onChangeScene(($1, $2) -> {
            LoginModel.validProperty.set(true);
        });
        
        Utils.bindModelToTextField(LoginModel.emailProperty, emailTextField);
        Utils.bindModelToTextField(LoginModel.passwordProperty, passwordTextField);
    }
    
    private void initializeNetworkAnimation() {
        Network networkAnimation = new Network(networkCanvas);
        /*
         * Temporarily disable stopping the animation.
         *
         * Why?
         *
         * For some reason, stopping the animation when the scene gets hidden
         * causes a lag when the user goes back to that scene.
         */
        // SceneManager.onChangeScene((newScene, oldScene) -> {
        //     if (newScene != "login") {
        //         networkAnimation.stop();
        //     } else {
        //         networkAnimation.start();
        //     }
        // });
    }
}