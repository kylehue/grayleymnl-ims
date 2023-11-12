package com.ims.model;

import javafx.beans.property.*;

import java.util.Observable;

public abstract class ProductModel {
    public static IntegerProperty idProperty = new SimpleIntegerProperty(-1);
    public static StringProperty nameProperty = new SimpleStringProperty();
    public static DoubleProperty priceProperty = new SimpleDoubleProperty();
    public static IntegerProperty categoryIDProperty = new SimpleIntegerProperty();
    public static StringProperty imageURLProperty = new SimpleStringProperty();
    public static IntegerProperty currentStocksProperty = new SimpleIntegerProperty();
    public static IntegerProperty expectedStocksProperty = new SimpleIntegerProperty();
    
    public static void clearState() {
        idProperty.setValue(null);
        nameProperty.setValue(null);
        priceProperty.setValue(null);
        categoryIDProperty.setValue(null);
        imageURLProperty.setValue(null);
        currentStocksProperty.setValue(null);
        expectedStocksProperty.setValue(null);
    }
}
