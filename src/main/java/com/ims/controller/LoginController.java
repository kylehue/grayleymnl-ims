package com.ims.controller;

import com.ims.canvas.network.Network;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
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
    public void initialize() {
        this.initializeNetworkAnimation();
        Utils.fitImageViewToParent(vectorImage);
        
        loginButton.setOnMouseClicked((MouseEvent event) -> {
            SceneManager.setScene("base");
        });
        
        registerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            SceneManager.setScene("register");
        });
    }
    
    private void initializeNetworkAnimation() {
        Network networkAnimation = new Network(networkCanvas);
        SceneManager.onChangeScene((newScene, oldScene) -> {
            if (newScene != "login") {
                networkAnimation.stop();
            } else {
                networkAnimation.start();
            }
        });
    }
}