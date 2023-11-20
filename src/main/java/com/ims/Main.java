package com.ims;

import com.ims.database.Database;
import com.ims.utils.Env;
import com.ims.utils.Mail;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import com.ims.utils.SceneManager;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class Main extends Application {
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
        
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 1.5);
    }
    
    public static void main(String[] args) {
        launch();
    }
}