package com.ims.controller;

import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
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
    private MFXButton loginButton;
    
    @FXML
    public void initialize()
        throws URISyntaxException, ParserConfigurationException, IOException, SAXException {
        Utils.fitImageViewToParent(vectorImage);
        Utils.addIconToButton(loginButton, "/icons/login.svg");
        
        loginButton.setOnMouseClicked((MouseEvent event) -> {
            SceneManager.setScene("base");
        });
    }
}