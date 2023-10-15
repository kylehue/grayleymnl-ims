package com.ims;

import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Theme;
import io.github.palexdev.materialfx.css.themes.Themes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("login-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 940, 480);
        stage.setTitle("GrayleyMNL Inventory Manager");
        stage.setScene(scene);
        stage.show();
        stage.setMinWidth(480);
        stage.setMinHeight(320);
    }

    public static void main(String[] args) {
        launch();
    }
}