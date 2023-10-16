package com.ims.controller;

import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class LoginController {
    @FXML
    private Label welcomeText;
    
    @FXML
    private ImageView vectorImage;
    
    @FXML
    private MFXButton loginButton;
    
    @FXML
    public void initialize() {
        Utils.fitImageViewToParent(vectorImage);
        
        loginButton.setOnMouseClicked((MouseEvent event) -> {
            SceneManager.setScene("base");
        });
    }
}