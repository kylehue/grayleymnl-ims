package com.ims.model.objects;

import java.sql.Timestamp;

public class ProductObject {
    private final int id;
    private final String name;
    private final int categoryObject;
    private final String imageURL;
    private final int currentStocks;
    private final int expectedStocks;
    private final Timestamp lastModified;
    
    public ProductObject(
        int id,
        String name,
        int categoryObject,
        String imageURL,
        int currentStocks,
        int expectedStocks,
        Timestamp lastModified
    ) {
        this.id = id;
        this.name = name;
        this.categoryObject = categoryObject;
        this.imageURL = imageURL;
        this.currentStocks = currentStocks;
        this.expectedStocks = expectedStocks;
        this.lastModified = lastModified;
    }
    
    public int getID() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getCategoryObject() {
        return categoryObject;
    }
    
    public String getImageURL() {
        return imageURL;
    }
    
    public int getCurrentStocks() {
        return currentStocks;
    }
    
    public int getExpectedStocks() {
        return expectedStocks;
    }
    
    public Timestamp getLastModified() {
        return lastModified;
    }
}
