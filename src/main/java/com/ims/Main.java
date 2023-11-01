package com.ims;

import com.ims.database.Database;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.ims.utils.SceneManager;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        Database.connect();
        
        SceneManager.setStage(stage);
        SceneManager.registerScene("login", "login-view.fxml");
        SceneManager.registerScene("register", "register-view.fxml");
        SceneManager.registerScene("forgot-password", "forgot-password-view.fxml");
        SceneManager.registerScene("base", "base-view.fxml");
        SceneManager.registerScene("product", "product-view.fxml");
        SceneManager.registerScene("account-settings", "account-settings-view.fxml");
        SceneManager.registerScene("user-manager", "user-manager-view.fxml");
        SceneManager.registerScene("user", "user-view.fxml");
        SceneManager.setScene("base");
        SceneManager.setSize(940, 640);
        
        stage.setTitle("GrayleyMNL Inventory Manager");
        stage.show();
        stage.setMinWidth(480);
        stage.setMinHeight(320);
        stage.getIcons().add(
            new Image(Main.class.getResource(
                "/images/icon.png"
            ).toURI().toString())
        );
    }
    
    public static void main(String[] args) {
        launch();
    }
}