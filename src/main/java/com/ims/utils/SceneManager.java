package com.ims.utils;

import com.ims.Main;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class SceneManager {
    private static Stage stage;
    
    private static HashMap<String, Scene> registeredScenes = new HashMap();
    
    private static double[] size = {940, 640};
    
    private static HashSet<SceneChangeEvent> sceneChangeListeners = new HashSet<>();
    
    private static String currentSceneID = "";
    
    /**
     * Set the stage where scenes will be added.
     *
     * @param stage
     */
    public static void setStage(Stage stage) {
        SceneManager.stage = stage;
    }
    
    public static Stage getStage() {
        return stage;
    }
    
    /**
     * Register scenes.
     *
     * @param id  The id of the scene.
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
     *
     * @param id The id of the scene.
     */
    public static void setScene(String id) {
        Scene targetScene = registeredScenes.get(id);
        
        if (targetScene == null) {
            throw new Error("The scene named '" + id + "' doesn't exist.");
        }
        
        Scene currentScene = getScene(currentSceneID);

        // if (currentScene != null) {
        //     final double transitionDuration = 200;
        //     FadeTransition fadeOut = new FadeTransition(
        //         Duration.millis(transitionDuration), currentScene.getRoot()
        //     );
        //     fadeOut.setFromValue(1.0);
        //     fadeOut.setToValue(0.0);
        //     fadeOut.playFromStart();
        //     FadeTransition fadeIn = new FadeTransition(
        //         Duration.millis(transitionDuration), targetScene.getRoot()
        //     );
        //     fadeIn.setFromValue(0.0);
        //     fadeIn.setToValue(1.0);
        //     fadeIn.playFromStart();
        // }
        
        SceneManager.stage.setScene(targetScene);
        
        for (SceneChangeEvent listener : sceneChangeListeners) {
            listener.call(id, SceneManager.currentSceneID);
        }
        
        SceneManager.currentSceneID = id;
    }
    
    /**
     * Retrieve a scene using their id.
     *
     * @param id The id of the scene to retrieve.
     * @return The scene that matches the id.
     */
    public static Scene getScene(String id) {
        return SceneManager.registeredScenes.get(id);
    }
    
    /**
     * Use this to set the default size of scenes.
     *
     * @param width
     * @param height
     */
    public static void setSize(double width, double height) {
        SceneManager.size[0] = width;
        SceneManager.size[1] = height;
        SceneManager.stage.setWidth(width);
        SceneManager.stage.setHeight(height);
    }
    
    /**
     * Execute a function whenever the scene changes.
     *
     * @param cb The function to execute.
     */
    public static void onChangeScene(SceneChangeEvent cb) {
        SceneManager.sceneChangeListeners.add(cb);
    }
}