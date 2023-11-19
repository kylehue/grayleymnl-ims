package com.ims.components;

import com.ims.model.UserManagerModel;
import com.ims.model.objects.RoleObject;

public class RoleComboBox extends ComboBox<Integer, RoleObject> {
    public RoleComboBox() {
        this.textField.setFloatingText("Role");
        this.setItems(UserManagerModel.roleMap);
        this.setStringifier(RoleObject::getName);
        this.initializeRoleLazyLoad();
    }
    
    private void initializeRoleLazyLoad() {
        // Load roles whenever the scrollbar hits the bottom.
        this.getDropDownScrollPane().vvalueProperty().addListener(
            ($1, $2, scrollValue) -> {
                if (scrollValue.doubleValue() == 1) {
                    UserManagerModel.loadRoles(8);
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
                UserManagerModel.loadRoles(4);
            }
        });
        
        // Everything above won't work if the `viewportBoundsProperty` doesn't trigger.
        // So here, we can trigger it by loading initial roles.
        UserManagerModel.loadRoles(8);
    }
}
