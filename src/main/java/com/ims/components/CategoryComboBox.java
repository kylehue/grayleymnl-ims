package com.ims.components;

import com.ims.Config;
import com.ims.database.DBCategories;
import com.ims.model.BaseModel;
import com.ims.model.objects.CategoryObject;
import com.ims.utils.LayoutUtils;
import com.ims.utils.Utils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryComboBox extends ComboBox<Integer, CategoryObject> {
    private final ObservableMap<Integer, CategoryObject> model = FXCollections.observableHashMap();
    
    public CategoryComboBox() {
        this.textField.setFloatingText("Category");
        this.setItems(model, CategoryObject::nameProperty);
        this.setStringifier(CategoryObject::getName);
        this.initializeCategoryLazyLoad();
        
        ChangeListener<String> categoryNameListener = (e, oldValue, newValue) -> {
            Platform.runLater(() -> {
                this.textField.setText(newValue);
            });
        };
        
        this.addSelectionListener((categoryObject, oldCategoryObject) -> {
            if (oldCategoryObject != null) {
                oldCategoryObject.nameProperty().removeListener(categoryNameListener);
            }
            categoryObject.nameProperty().addListener(categoryNameListener);
        });
    }
    
    @Override
    public void search(String searchText) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                model.clear();
                clear();
                if (searchText.isEmpty()) {
                    loadCategories(Config.categoryLoadLimit);
                    return null;
                }
                
                String searchPattern = Utils.textToSearchPattern(searchText);
                ArrayList<HashMap<DBCategories.Column, Object>> result = DBCategories.search(
                    searchPattern
                );
                
                for (HashMap<DBCategories.Column, Object> row : result) {
                    loadCategory(row);
                }
                
                return null;
            }
        };
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    private void loadCategories(int limit) {
        BaseModel.loadCategoriesToMap(limit, model);
    }
    
    private void loadCategory(HashMap<DBCategories.Column, Object> category) {
        BaseModel.loadCategoryToMap(category, model);
    }
    
    private void initializeCategoryLazyLoad() {
        Platform.runLater(() -> {
            LayoutUtils.initializeLazyLoad(
                this.getDropDownScrollPane(),
                this.getDropdownContainer(),
                model,
                (requestType) -> {
                    switch (requestType) {
                        case INITIAL:
                            loadCategories(8);
                            break;
                        case HIT_BOTTOM:
                            if (!this.getSearchText().isEmpty()) return;
                            loadCategories(Config.categoryLoadLimit);
                            break;
                        case INSUFFICIENT:
                            if (!this.getSearchText().isEmpty()) return;
                            loadCategories(Config.categoryLoadLimit / 3);
                            break;
                    }
                }
            );
        });
    }
}
