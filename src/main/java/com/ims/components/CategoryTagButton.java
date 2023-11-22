package com.ims.components;

import com.ims.model.objects.CategoryObject;
import com.ims.utils.SceneManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;

public class CategoryTagButton extends TagButton {
    private CategoryObject categoryObject;
    
    public CategoryTagButton(CategoryObject categoryObject) {
        this.setCategoryName(categoryObject.getName());
        
        SceneManager.onChangeScene((currentScene, oldScene) -> {
            if (!currentScene.equals("base")) return;
            this.setCategoryObject(this.categoryObject);
        });
        
        this.setCategoryObject(categoryObject);
    }
    
    public void setCategoryName(String name) {
        this.setText(name);
    }
    
    public String getCategoryName() {
        return this.getText();
    }
    
    private final ChangeListener<String> categoryChangeListener = (
        e,
        oldValue,
        newValue
    ) -> {
        Platform.runLater(() -> {
            this.setText(newValue);
        });
    };
    
    private CategoryObject oldCategoryObject = null;
    
    public void setCategoryObject(CategoryObject categoryObject) {
        this.categoryObject = categoryObject;
        this.setCategoryName(categoryObject.getName());
        if (oldCategoryObject != null) {
            oldCategoryObject.nameProperty().removeListener(categoryChangeListener);
        }
        categoryObject.nameProperty().addListener(categoryChangeListener);
        oldCategoryObject = categoryObject;
    }
    
    public CategoryObject getCategoryObject() {
        return categoryObject;
    }
}
