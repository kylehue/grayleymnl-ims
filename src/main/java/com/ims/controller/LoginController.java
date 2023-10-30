package com.ims.controller;

import com.ims.canvas.network.Network;
import com.ims.utils.SceneManager;
import com.ims.utils.LayoutUtils;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;

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
    public void initialize() {
        this.initializeNetworkAnimation();
        LayoutUtils.fitImageViewToParent(vectorImage);
        
        loginButton.setOnMouseClicked((MouseEvent event) -> {
            SceneManager.setScene("base");
        });
        
        registerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            SceneManager.setScene("register");
        });
        
        forgotPasswordButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            SceneManager.setScene("forgot-password");
        });
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