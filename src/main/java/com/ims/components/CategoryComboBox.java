package com.ims.components;

import com.ims.model.BaseModel;
import com.ims.model.objects.CategoryObject;
import com.ims.utils.LayoutUtils;

public class CategoryComboBox extends ComboBox<Integer, CategoryObject> {
    public CategoryComboBox() {
        this.textField.setFloatingText("Category");
        this.setItems(BaseModel.categoryMap);
        this.setStringifier(CategoryObject::getName);
        this.initilizeCategoryLazyLoad();
    }
    
    private void initilizeCategoryLazyLoad() {
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
