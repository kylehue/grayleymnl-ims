package com.ims.model.objects;

import java.sql.Timestamp;

public class CategoryObject {
    private final int id;
    private String name;
    private Timestamp lastModified;
    
    public CategoryObject(int id, String name, Timestamp lastModified) {
        this.id = id;
        this.name = name;
        this.lastModified = lastModified;
    }
    
    public int getID() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Timestamp getLastModified() {
        return lastModified;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }
}
