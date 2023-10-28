package com.ims.utils;

public interface SceneChangeEvent {
    void call(String currentScene, String oldScene);
}
