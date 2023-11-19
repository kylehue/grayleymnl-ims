package com.ims.model.objects;

import java.sql.Timestamp;

public class RoleObject {
    private int id;
    private String name;
    private boolean allowAddCategory;
    private boolean allowDeleteCategory;
    private boolean allowEditCategory;
    private boolean allowAddProduct;
    private boolean allowDeleteProduct;
    private boolean allowEditProduct;
    private Timestamp lastModified;
    
    public RoleObject(
        int id,
        String name,
        boolean allowAddCategory,
        boolean allowDeleteCategory,
        boolean allowEditCategory,
        boolean allowAddProduct,
        boolean allowDeleteProduct,
        boolean allowEditProduct,
        Timestamp lastModified
    ) {
        this.id = id;
        this.name = name;
        this.allowAddCategory = allowAddCategory;
        this.allowDeleteCategory = allowDeleteCategory;
        this.allowEditCategory = allowEditCategory;
        this.allowAddProduct = allowAddProduct;
        this.allowDeleteProduct = allowDeleteProduct;
        this.allowEditProduct = allowEditProduct;
        this.lastModified = lastModified;
    }
    
    public int getID() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isAllowAddCategory() {
        return allowAddCategory;
    }
    
    public boolean isAllowAddProduct() {
        return allowAddProduct;
    }
    
    public boolean isAllowDeleteCategory() {
        return allowDeleteCategory;
    }
    
    public boolean isAllowDeleteProduct() {
        return allowDeleteProduct;
    }
    
    public boolean isAllowEditCategory() {
        return allowEditCategory;
    }
    
    public boolean isAllowEditProduct() {
        return allowEditProduct;
    }
    
    public Timestamp getLastModified() {
        return lastModified;
    }
}
