package com.ims.model.objects;

import com.ims.components.Role;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.sql.Timestamp;
import java.util.Observable;
import java.util.concurrent.Flow;

public class RoleObject {
    private final int id;
    private final StringProperty name;
    private final BooleanProperty allowAddCategory;
    private final BooleanProperty allowDeleteCategory;
    private final BooleanProperty allowEditCategory;
    private final BooleanProperty allowAddProduct;
    private final BooleanProperty allowDeleteProduct;
    private final BooleanProperty allowEditProduct;
    private final ObjectProperty<Timestamp> lastModified;
    private final boolean isNew;
    
    public RoleObject(
        int id,
        String name,
        boolean allowAddCategory,
        boolean allowDeleteCategory,
        boolean allowEditCategory,
        boolean allowAddProduct,
        boolean allowDeleteProduct,
        boolean allowEditProduct,
        Timestamp lastModified,
        boolean isNew
    ) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.allowAddCategory = new SimpleBooleanProperty(allowAddCategory);
        this.allowDeleteCategory = new SimpleBooleanProperty(allowDeleteCategory);
        this.allowEditCategory = new SimpleBooleanProperty(allowEditCategory);
        this.allowAddProduct = new SimpleBooleanProperty(allowAddProduct);
        this.allowDeleteProduct = new SimpleBooleanProperty(allowDeleteProduct);
        this.allowEditProduct = new SimpleBooleanProperty(allowEditProduct);
        this.lastModified = new SimpleObjectProperty<>(lastModified);
        this.isNew = isNew;
    }
    
    public boolean isNew() {
        return isNew;
    }
    
    public int getID() {
        return id;
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getName() {
        return this.name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public BooleanProperty allowAddCategoryProperty() {
        return allowAddCategory;
    }
    
    public boolean isAllowAddCategory() {
        return allowAddCategory.get();
    }
    
    public void setAllowAddCategory(boolean allowAddCategory) {
        this.allowAddCategory.set(allowAddCategory);
    }
    
    public BooleanProperty allowAddProductProperty() {
        return allowAddProduct;
    }
    
    public boolean isAllowAddProduct() {
        return allowAddProduct.get();
    }
    
    public void setAllowAddProduct(boolean allowAddProduct) {
        this.allowAddProduct.set(allowAddProduct);
    }
    
    public BooleanProperty allowDeleteCategoryProperty() {
        return allowDeleteCategory;
    }
    
    public boolean isAllowDeleteCategory() {
        return allowDeleteCategory.get();
    }
    
    public void setAllowDeleteCategory(boolean allowDeleteCategory) {
        this.allowDeleteCategory.set(allowDeleteCategory);
    }
    
    public BooleanProperty allowDeleteProductProperty() {
        return allowDeleteProduct;
    }
    
    public boolean isAllowDeleteProduct() {
        return allowDeleteProduct.get();
    }
    
    public void setAllowDeleteProduct(boolean allowDeleteProduct) {
        this.allowDeleteProduct.set(allowDeleteProduct);
    }
    
    public BooleanProperty allowEditCategoryProperty() {
        return allowEditCategory;
    }
    
    public boolean isAllowEditCategory() {
        return allowEditCategory.get();
    }
    
    public void setAllowEditCategory(boolean allowEditCategory) {
        this.allowEditCategory.set(allowEditCategory);
    }
    
    public BooleanProperty allowEditProductProperty() {
        return allowEditProduct;
    }
    
    public boolean isAllowEditProduct() {
        return allowEditProduct.get();
    }
    
    public void setAllowEditProduct(boolean allowEditProduct) {
        this.allowEditProduct.set(allowEditProduct);
    }
    
    public ObjectProperty<Timestamp> lastModifiedProperty() {
        return lastModified;
    }
    
    public Timestamp getLastModified() {
        return lastModified.get();
    }
    
    public void setLastModified(Timestamp lastModified) {
        this.lastModified.set(lastModified);
    }
}
