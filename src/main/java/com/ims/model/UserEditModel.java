package com.ims.model;

import com.ims.database.DBUsers;
import com.ims.model.objects.UserObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class UserEditModel {
    public static ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public static ObjectProperty<UserObject> currentUser = new SimpleObjectProperty<>();
    
    public static void transferOwnershipToCurrentUser() {
        if (!UserSessionModel.currentUserIsOwner()) {
            System.out.println("The user has insufficient permissions.");
            return;
        }
        
        UserObject fromUserObject = UserSessionModel.currentUser.get();
        UserObject toUserObject = currentUser.get();
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                DBUsers.transferOwnership(
                    toUserObject.getID()
                );
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            fromUserObject.setOwner(false);
            toUserObject.setOwner(true);
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        executor.submit(task);
    }
}
