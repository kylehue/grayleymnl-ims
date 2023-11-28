package com.ims.components;

import com.ims.Config;
import com.ims.database.DBRoles;
import com.ims.model.BaseModel;
import com.ims.model.UserManagerModel;
import com.ims.model.objects.CategoryObject;
import com.ims.model.objects.RoleObject;
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

public class RoleComboBox extends ComboBox<Integer, RoleObject> {
    private final ObservableMap<Integer, RoleObject> model = FXCollections.observableHashMap();
    
    public RoleComboBox() {
        this.textField.setFloatingText("Role");
        this.setItems(model, RoleObject::nameProperty);
        this.setStringifier(RoleObject::getName);
        this.initializeRoleLazyLoad();
        
        ChangeListener<String> roleNameListener = (e, oldValue, newValue) -> {
            Platform.runLater(() -> {
                this.textField.setText(newValue);
            });
        };
        
        this.addSelectionListener((roleObject, oldRoleObject) -> {
            if (oldRoleObject != null) {
                oldRoleObject.nameProperty().removeListener(roleNameListener);
            }
            roleObject.nameProperty().addListener(roleNameListener);
        });
        
        UserManagerModel.roleMap.addListener(
            (MapChangeListener<Integer, RoleObject>) change -> {
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
                loadRoles(Config.roleLoadLimit);
                return null;
            }
            
            String searchPattern = Utils.textToSearchPattern(searchText);
            DBRoles.RoleListData result = DBRoles.search(
                searchPattern
            );
            
            for (DBRoles.RoleData row : result) {
                loadRole(row);
            }
            
            return null;
        }, Utils.executor).execute();
    }
    
    private void loadRoles(int limit) {
        UserManagerModel.loadRolesToMap(limit, model);
    }
    
    private void loadRole(DBRoles.RoleData role) {
        UserManagerModel.loadRoleToMap(role, model, false);
    }
    
    private void initializeRoleLazyLoad() {
        LazyLoader lazyLoader = new LazyLoader(
            this.getDropDownScrollPane(),
            this.getDropdownContainer(),
            model
        );
        
        lazyLoader.setLoader((requestType) -> {
            if (!this.getSearchText().isEmpty()) return;
            loadRoles(Config.roleLoadLimit);
        });
    }
}
