package com.ims.controller;

import com.ims.utils.SceneManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class BaseController {
    @FXML
    private MFXButton backButton;

    @FXML
    public void initialize() {
        backButton.setOnMouseClicked((MouseEvent event) -> {
            SceneManager.setScene("login");
        });
    }
}
