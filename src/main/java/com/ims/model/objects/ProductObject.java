package com.ims.model.objects;

import java.sql.Timestamp;

public class ProductObject {
    private final int id;
    private final String name;
    private final double price;
    private final int categoryID;
    private final String imageURL;
    private final int currentStocks;
    private final int expectedStocks;
    private final Timestamp lastModified;
    
    public ProductObject(
        int id,
        String name,
        double price,
        int categoryID,
        String imageURL,
        int currentStocks,
        int expectedStocks,
        Timestamp lastModified
    ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryID = categoryID;
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
    
    public int getCategoryID() {
        return categoryID;
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
    
    public double getPrice() {
        return price;
    }
}
