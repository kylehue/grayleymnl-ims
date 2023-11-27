package com.ims;

import com.ims.database.Database;
import com.ims.model.*;
import com.ims.utils.Env;
import com.ims.utils.Mail;
import com.ims.utils.SceneManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;

public class Launcher extends Application {
    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        Env.initialize();
        Database.connect();
        Mail.initialize();
        
        SceneManager.setStage(stage);
        SceneManager.registerScene("login", "login-view.fxml");
        SceneManager.registerScene("register", "register-view.fxml");
        SceneManager.registerScene("forgot-password", "forgot-password-view.fxml");
        SceneManager.registerScene("base", "base-view.fxml");
        SceneManager.registerScene("product", "product-view.fxml");
        SceneManager.registerScene("account-settings", "account-settings-view.fxml");
        SceneManager.registerScene("user-manager", "user-manager-view.fxml");
        SceneManager.registerScene("user", "user-view.fxml");
        SceneManager.setScene("login");
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
        
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 1.5);
        
        stage.setOnCloseRequest(e -> {
            BaseModel.executor.shutdown();
            LoginModel.executor.shutdown();
            RegisterModel.executor.shutdown();
            UserEditModel.executor.shutdown();
            UserManagerModel.executor.shutdown();
            UserSessionModel.executor.shutdown();
            Platform.exit();
            System.exit(0);
        });
    }
    
    public static void execute() {
        launch();
    }
}