package com.ims.model.objects;

import javafx.beans.property.*;

import java.sql.Timestamp;

public class ProductObject {
    private final int id;
    private final ObjectProperty<String> name;
    private final ObjectProperty<Double> price;
    private final ObjectProperty<Integer> categoryID;
    private final ObjectProperty<String> imageURL;
    private final ObjectProperty<Integer> currentStocks;
    private final ObjectProperty<Integer> expectedStocks;
    private final ObjectProperty<Timestamp> lastModified;
    
    public ProductObject(
        int id,
        String name,
        Double price,
        Integer categoryID,
        String imageURL,
        Integer currentStocks,
        Integer expectedStocks,
        Timestamp lastModified
    ) {
        this.id = id;
        this.name = new SimpleObjectProperty<>(name);
        this.price = new SimpleObjectProperty<>(price);
        this.categoryID = new SimpleObjectProperty<>(categoryID);
        this.imageURL = new SimpleObjectProperty<>(imageURL);
        this.currentStocks = new SimpleObjectProperty<>(currentStocks);
        this.expectedStocks = new SimpleObjectProperty<>(expectedStocks);
        this.lastModified = new SimpleObjectProperty<>(lastModified);
    }
    
    public int getID() {
        return id;
    }
    
    public ObjectProperty<String> nameProperty() {
        return name;
    }
    
    public String getName() {
        return name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public ObjectProperty<Integer> categoryIDProperty() {
        return categoryID;
    }
    
    public Integer getCategoryID() {
        return categoryID.get();
    }
    
    public void setCategoryID(Integer categoryID) {
        this.categoryID.set(categoryID);
    }
    
    public ObjectProperty<String> imageURLProperty() {
        return imageURL;
    }
    
    public String getImageURL() {
        return imageURL.get();
    }
    
    public void setImageURL(String imageURL) {
        this.imageURL.set(imageURL);
    }
    
    public ObjectProperty<Integer> currentStocksProperty() {
        return currentStocks;
    }
    
    public Integer getCurrentStocks() {
        return currentStocks.get();
    }
    
    public void setCurrentStocks(int currentStocks) {
        this.currentStocks.set(currentStocks);
    }
    
    public ObjectProperty<Integer> expectedStocksProperty() {
        return expectedStocks;
    }
    
    public Integer getExpectedStocks() {
        return expectedStocks.get();
    }
    
    public void setExpectedStocks(int expectedStocks) {
        this.expectedStocks.set(expectedStocks);
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
    
    public ObjectProperty<Double> priceProperty() {
        return price;
    }
    
    public Double getPrice() {
        return price.get();
    }
    
    public void setPrice(double price) {
        this.price.set(price);
    }
}
