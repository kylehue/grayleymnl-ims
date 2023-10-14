package com.ims;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    private ImageView vectorImage;

    @FXML
    public void initialize() {
        System.out.println("hi");
        Utils.fitImageViewToParent(vectorImage);
    }
}