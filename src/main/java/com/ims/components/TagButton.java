package com.ims.components;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;

public class TagButton extends MFXButton {
    private boolean isActive = false;
    private final ObservableList<String> styleClass = this.getStyleClass();
    
    public TagButton() {
        this.styleClass.add("tag-button");
        
        // Toggle
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
            this.setActive(!this.isActive);
        });
    }
    
    public void setActive(boolean value) {
        this.isActive = value;
        final String activeClass = "tag-button-active";
        if (this.isActive) {
            this.styleClass.add(activeClass);
        } else {
            this.styleClass.remove(activeClass);
        }
    }
    
    public boolean isActive() {
        return isActive;
    }
}
