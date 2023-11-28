package com.ims.model;

import com.ims.database.DBUsers;
import com.ims.model.objects.UserObject;
import com.ims.utils.AsyncCaller;
import com.ims.utils.Utils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class UserEditModel {
    public static ObjectProperty<UserObject> currentUser = new SimpleObjectProperty<>();
    
    public static AsyncCaller<Void> transferOwnershipToCurrentUser() {
        UserObject fromUserObject = UserSessionModel.currentUser.get();
        UserObject toUserObject = currentUser.get();
        return new AsyncCaller<Void>(task -> {
            if (!UserSessionModel.currentUserIsOwner()) {
                System.out.println("The user has insufficient permissions.");
                return null;
            }
            
            DBUsers.transferOwnership(
                toUserObject.getID()
            );
            
            return null;
        }, Utils.executor).onSucceeded(e -> {
            fromUserObject.setOwner(false);
            toUserObject.setOwner(true);
        });
    }
}
