package com.ims.utils;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

import java.util.List;

public class TabGroup {
    private final String activeClass;
    private final List<Pair<MFXButton, Pane>> tabs;
    private ObjectProperty<Pair<MFXButton, Pane>> currentTab = new SimpleObjectProperty<>();
    
    public TabGroup(
        String activeClass,
        List<Pair<MFXButton, Pane>> tabs,
        String sceneName
    ) {
        this.activeClass = activeClass;
        this.tabs = tabs;
        
        for (Pair<MFXButton, Pane> tab : tabs) {
            MFXButton tabButton = tab.getKey();
            Pane tabPane = tab.getValue();
            
            // All tab panes should start invisible
            tabPane.setVisible(false);
            
            tabButton.setOnAction(event -> {
                makeTabActive(tab);
            });
        }
        
        SceneManager.onChangeScene((currentScene, oldScene) -> {
            if (!currentScene.equals(sceneName)) return;
            Pair<MFXButton, Pane> firstTab = tabs.get(0);
            makeTabActive(firstTab);
        });
    }
    
    private void makeTabActive(
        Pair<MFXButton, Pane> tab
    ) {
        MFXButton tabButton = tab.getKey();
        Pane tabPane = tab.getValue();
        
        ObservableList<String> styleClass = tabButton.getStyleClass();
        if (!styleClass.contains(activeClass)) {
            styleClass.add(activeClass);
        }
        
        currentTab.set(tab);
        tabPane.setVisible(true);
        
        // Make other tabs inactive
        for (Pair<MFXButton, Pane> otherTab : tabs) {
            if (otherTab == tab) continue;
            MFXButton otherTabButton = otherTab.getKey();
            Pane otherTabPane = otherTab.getValue();
            otherTabButton.getStyleClass().remove(activeClass);
            otherTabPane.setVisible(false);
        }
    }
    
    public List<Pair<MFXButton, Pane>> getTabs() {
        return tabs;
    }
    
    public ObjectProperty<Pair<MFXButton, Pane>> currentTabProperty() {
        return currentTab;
    }
    
    public Pair<MFXButton, Pane> getCurrentTab() {
        return currentTab.get();
    }
    
    public String getActiveClass() {
        return activeClass;
    }
}
