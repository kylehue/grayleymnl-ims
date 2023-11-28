package com.ims.components;

import com.ims.Config;
import com.ims.database.DBCategories;
import com.ims.model.BaseModel;
import com.ims.model.objects.CategoryObject;
import com.ims.utils.AsyncCaller;
import com.ims.utils.LayoutUtils;
import com.ims.utils.LazyLoader;
import com.ims.utils.Utils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
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
        
        BaseModel.categoryMap.addListener(
            (MapChangeListener<Integer, CategoryObject>) change -> {
                if (!change.wasRemoved()) return;
                int id = change.getKey();
                model.remove(id);
                
                if (this.getValue() != null && this.getValue().getID() == id) {
                    this.setValue(null);
                }
            }
        );
        
        this.textField.delegateFocusedProperty().addListener(e -> {
            if (!this.textField.delegateIsFocused()) return;
            if (!this.getSearchText().isEmpty()) return;
            this.search("");
        });
    }
    
    @Override
    public void search(String searchText) {
        new AsyncCaller<Void>(task -> {
            model.clear();
            clear();
            if (searchText.isEmpty()) {
                loadCategories(Config.categoryLoadLimit);
                return null;
            }
            
            String searchPattern = Utils.textToSearchPattern(searchText);
            DBCategories.CategoryListData result = DBCategories.search(
                searchPattern
            );
            
            for (DBCategories.CategoryData row : result) {
                loadCategory(row);
            }
            
            return null;
        }, Utils.executor).execute();
    }
    
    private void loadCategories(int limit) {
        BaseModel.loadCategoriesToMap(limit, model);
    }
    
    private void loadCategory(DBCategories.CategoryData category) {
        BaseModel.loadCategoryToMap(category, model, false);
    }
    
    private void initializeCategoryLazyLoad() {
        LazyLoader lazyLoader = new LazyLoader(
            this.getDropDownScrollPane(),
            this.getDropdownContainer(),
            model
        );
        
        lazyLoader.setLoader((requestType) -> {
            switch (requestType) {
                case INITIAL:
                    loadCategories(8);
                    break;
                case HIT_BOTTOM:
                case INSUFFICIENT:
                    if (!this.getSearchText().isEmpty()) return;
                    loadCategories(Config.categoryLoadLimit);
                    break;
            }
        });
    }
}
