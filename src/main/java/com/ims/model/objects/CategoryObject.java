package com.ims.model.objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.Timestamp;

public class CategoryObject {
    private final int id;
    private final StringProperty name;
    private final ObjectProperty<Timestamp> lastModified;
    private final boolean isNew;
    
    public CategoryObject(int id, String name, Timestamp lastModified, boolean isNew) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
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
        return name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
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
