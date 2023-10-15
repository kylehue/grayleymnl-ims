package com.ims.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Arrays;

import static com.ims.utils.Utils.createTabGroup;

public class BaseController {
    @FXML
    private MFXButton tabDashboardButton;

    @FXML
    private MFXButton tabProductsButton;

    @FXML
    private MFXButton tabCategoriesButton;

    @FXML
    private GridPane tabDashboardPane;

    @FXML
    private GridPane tabProductsPane;

    @FXML
    private GridPane tabCategoriesPane;

    @FXML
    public void initialize() {
        createTabGroup(
                "tab-button-active",
                Arrays.asList(
                        new Pair<>(tabDashboardButton, tabDashboardPane),
                        new Pair<>(tabProductsButton, tabProductsPane),
                        new Pair<>(tabCategoriesButton, tabCategoriesPane)
                )
        );
    }
}
