package com.ims.utils;

import com.ims.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;

public class SceneManager {
    private static Stage stage;

    private static HashMap<String, Scene> registeredScenes = new HashMap();

    private static double[] size = {940, 480};

    /**
     * Set the stage where scenes will be added.
     * @param stage
     */
    public static void setStage(Stage stage) {
        SceneManager.stage = stage;
    }

    /**
     * Register scenes.
     * @param id The id of the scene.
     * @param url The fmxl source of the scene.
     * @return The created scene.
     * @throws IOException When the fmxl source doesn't exist.
     */
    public static Scene registerScene(String id, String url) throws IOException {
        double width = SceneManager.size[0];
        double height = SceneManager.size[1];

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(url));
        Scene scene = new Scene(fxmlLoader.load(), width, height);
        SceneManager.registeredScenes.put(id, scene);

        return scene;
    }

    /**
     * Set the scene using their id.
     * @param id The id of the scene.
     */
    public static void setScene(String id) {
        Scene targetScene = registeredScenes.get(id);

        if (targetScene == null) {
            throw new Error("The scene named '" + id + "' doesn't exist.");
        }

        SceneManager.stage.setScene(targetScene);
    }

    /**
     * Retrieve a scene using their id.
     * @param id The id of the scene to retrieve.
     * @return The scene that matches the id.
     */
    public static Scene getScene(String id) {
        return registeredScenes.get(id);
    }

    /**
     * Use this to set the default size of scenes.
     * @param width
     * @param height
     */
    public static void setSize(double width, double height) {
        SceneManager.size[0] = width;
        SceneManager.size[1] = height;
    }
}
