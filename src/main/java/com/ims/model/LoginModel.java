package com.ims.model;

import com.ims.database.DBUsers;
import com.ims.model.objects.UserObject;
import com.ims.utils.SceneManager;
import com.ims.utils.Utils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class LoginModel {
    public static StringProperty emailProperty = new SimpleStringProperty("");
    public static StringProperty passwordProperty = new SimpleStringProperty("");
    public static BooleanProperty validProperty = new SimpleBooleanProperty(true);
    
    public static void login() {
        String email = emailProperty.get();
        String password = passwordProperty.get();
        if (email.isEmpty() || password.isEmpty()) {
            validProperty.set(false);
            return;
        }
        Task<ArrayList<HashMap<DBUsers.Column, Object>>> task = new Task<>() {
            @Override
            protected ArrayList<HashMap<DBUsers.Column, Object>> call() {
                return DBUsers.get(
                    DBUsers.Column.EMAIL,
                    email
                );
            }
        };
        
        task.setOnSucceeded(e -> {
            try {
                ArrayList<HashMap<DBUsers.Column, Object>> users = task.get();
                if (users.size() != 1) {
                    validProperty.set(false);
                    return;
                }
                
                HashMap<DBUsers.Column, Object> user = users.getFirst();
                boolean isCorrectPassword = Utils.checkPassword(
                    password,
                    user.get(DBUsers.Column.PASSWORD).toString()
                );
                
                if (isCorrectPassword) {
                    SceneManager.setScene("base");
                    validProperty.set(true);
                    
                    int id = (int) user.get(DBUsers.Column.ID);
                    
                    UserSessionModel.currentUser.set(
                        getUser(id)
                    );
                } else {
                    validProperty.set(false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
    
    public static UserObject getUser(int id) {
        Task<UserObject> task = new Task<>() {
            @Override
            protected UserObject call() throws Exception {
                HashMap<DBUsers.Column, Object> row = DBUsers.getOne(DBUsers.Column.ID, id);
                if (row != null) {
                    UserObject userObject = new UserObject(
                        id,
                        (String) row.get(DBUsers.Column.EMAIL),
                        (String) row.get(
                            DBUsers.Column.PASSWORD
                        ),
                        (Date) row.get(
                            DBUsers.Column.JOINED_DATE
                        ),
                        (Timestamp) row.get(
                            DBUsers.Column.LAST_ACTIVITY_DATE
                        ),
                        (Integer) row.get(
                            DBUsers.Column.ROLE_ID
                        ),
                        (boolean) row.get(
                            DBUsers.Column.IS_DISABLED
                        ),
                        (boolean) row.get(
                            DBUsers.Column.IS_OWNER
                        )
                    );
                    return userObject;
                }
                
                return null;
            }
        };
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
        
        UserObject userObject = null;
        try {
            userObject = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return userObject;
    }
}
