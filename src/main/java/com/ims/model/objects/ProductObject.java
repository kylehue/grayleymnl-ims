package com.ims.model.objects;

import javafx.beans.property.*;

import java.sql.Timestamp;

public class ProductObject {
    private final int id;
    private final StringProperty name;
    private final DoubleProperty price;
    private final IntegerProperty categoryID;
    private final StringProperty imageURL;
    private final IntegerProperty currentStocks;
    private final IntegerProperty expectedStocks;
    private final ObjectProperty<Timestamp> lastModified;
    
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
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.categoryID = new SimpleIntegerProperty(categoryID);
        this.imageURL = new SimpleStringProperty(imageURL);
        this.currentStocks = new SimpleIntegerProperty(currentStocks);
        this.expectedStocks = new SimpleIntegerProperty(expectedStocks);
        this.lastModified = new SimpleObjectProperty<>(lastModified);
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
    
    public IntegerProperty categoryIDProperty() {
        return categoryID;
    }
    
    public Integer getCategoryID() {
        return categoryID.get();
    }
    
    public void setCategoryID(Integer categoryID) {
        this.categoryID.set(categoryID);
    }
    
    public StringProperty imageURLProperty() {
        return imageURL;
    }
    
    public String getImageURL() {
        return imageURL.get();
    }
    
    public void setImageURL(String imageURL) {
        this.imageURL.set(imageURL);
    }
    
    public IntegerProperty currentStocksProperty() {
        return currentStocks;
    }
    
    public int getCurrentStocks() {
        return currentStocks.get();
    }
    
    public void setCurrentStocks(int currentStocks) {
        this.currentStocks.set(currentStocks);
    }
    
    public IntegerProperty expectedStocksProperty() {
        return expectedStocks;
    }
    
    public int getExpectedStocks() {
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
    
    public DoubleProperty priceProperty() {
        return price;
    }
    
    public double getPrice() {
        return price.get();
    }
    
    public void setPrice(double price) {
        this.price.set(price);
    }
}
