package com.ims.controller;

import com.ims.utils.LayoutUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

public class DisconnectedController {
    @FXML
    private ImageView vectorImage;
    
    @FXML
    public void initialize() {
        LayoutUtils.fitImageViewToParent(vectorImage);
    }
}
