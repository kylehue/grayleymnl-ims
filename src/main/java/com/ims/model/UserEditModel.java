package com.ims.model;

import com.ims.model.objects.UserObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public abstract class UserEditModel {
    public static ObjectProperty<UserObject> currentUser = new SimpleObjectProperty<>();
}
