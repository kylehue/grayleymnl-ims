package com.ims.controller;

import com.ims.canvas.network.Network;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

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
    public void initialize() {
        // this.initializeNetworkAnimation();
        Utils.fitImageViewToParent(vectorImage);
        
        loginButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            SceneManager.setScene("login");
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
