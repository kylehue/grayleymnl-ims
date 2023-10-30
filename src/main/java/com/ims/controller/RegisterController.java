package com.ims.controller;

import com.ims.canvas.network.Network;
import com.ims.utils.DatabaseManager;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import com.ims.utils.TextFieldValidator;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.validation.Severity;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.sql.Connection;
import java.sql.Statement;

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
        Utils.fitImageViewToParent(vectorImage);
        
        loginButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            SceneManager.setScene("login");
        });
        
        registerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            this.register();
        });
        
        TextFieldValidator emailTextFieldValidator = new TextFieldValidator(emailTextField);
        emailTextFieldValidator.addConstraint(
            Severity.ERROR,
            "Invalid email address.",
            Bindings.createBooleanBinding(
                () -> Utils.validateEmail(emailTextField.getText()),
                emailTextField.textProperty()
            )
        );
        emailTextFieldValidator.addConstraint(
            Severity.ERROR,
            "This email address already exists.",
            Bindings.createBooleanBinding(
                () -> {
                    String email = emailTextField.getText();
                    return true;
                },
                emailTextField.textProperty()
            )
        );
        
        TextFieldValidator passwordTextFieldValidator = new TextFieldValidator(passwordTextField);
        passwordTextFieldValidator.addConstraint(
            Severity.ERROR,
            "Password must be at least 8 characters long.",
            passwordTextField.textProperty().length().greaterThanOrEqualTo(8)
        );
        
        TextFieldValidator confirmPasswordTextFieldValidator = new TextFieldValidator(confirmPasswordTextField);
        confirmPasswordTextFieldValidator.addDependents(passwordTextField);
        confirmPasswordTextFieldValidator.addConstraint(
            Severity.ERROR,
            "Passwords doesn't match.",
            confirmPasswordTextField.textProperty().isEqualTo(passwordTextField.textProperty())
        );
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
    
    private void register() {
        if (
            !emailTextField.isValid() ||
                !passwordTextField.isValid() ||
                !confirmPasswordTextField.isValid()
        ) {
            return;
        }
        
        String email = emailTextField.getText();
        String password = Utils.hashPassword(passwordTextField.getText());
        
        try {
            Connection connection = DatabaseManager.getConnection();
            Statement statement = connection.createStatement();
            String query = "insert into users (email, password) values ('" + email + "', '" + password + "');";
            statement.executeQuery(query);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
