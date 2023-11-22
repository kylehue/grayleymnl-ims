package com.ims.model;

import com.ims.model.objects.ProductObject;
import javafx.beans.property.*;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;

import java.util.Observable;

public abstract class ProductModel {
    public static ObjectProperty<ProductObject> currentProduct = new SimpleObjectProperty<>();
}
