package com.ims.components;

import com.ims.model.BaseModel;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.RoleObject;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;

public class CategoryComboBox extends ComboBox<Integer, CategoryObject> {
    public CategoryComboBox() {
        this.textField.setFloatingText("Category");
        this.setItems(BaseModel.categoryMap, CategoryObject::nameProperty);
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
    
    private void initializeCategoryLazyLoad() {
        // First of all, we have to add the categories in the model
        for (int id : BaseModel.categoryMap.keySet()) {
            CategoryObject categoryObject = BaseModel.categoryMap.get(id);
            Platform.runLater(() -> {
                this.addItem(id, categoryObject);
            });
        }
        
        // Load categories whenever the scrollbar hits the bottom.
        this.getDropDownScrollPane().vvalueProperty().addListener(
            ($1, $2, scrollValue) -> {
                if (scrollValue.doubleValue() == 1) {
                    BaseModel.loadCategories(8);
                }
            }
        );
        
        // The listener above won't work if there is no scrollbar.
        // So here, we add components until the scroll pane gets a scrollbar.
        this.getDropDownScrollPane().viewportBoundsProperty().addListener(($1, $2, newValue) -> {
            double contentHeight = this.getDropdownContainer()
                .getBoundsInLocal()
                .getHeight();
            double viewportHeight = newValue.getHeight();
            if (contentHeight < viewportHeight) {
                BaseModel.loadCategories(4);
            }
        });
        
        // Everything above won't work if the `viewportBoundsProperty` doesn't trigger.
        // So here, we can trigger it by loading initial categories.
        BaseModel.loadCategories(8);
    }
}
