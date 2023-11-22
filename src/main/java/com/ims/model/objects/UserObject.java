package com.ims.model.objects;

import javafx.beans.property.*;

import java.sql.Date;
import java.sql.Timestamp;

public class UserObject {
    private final int id;
    private final StringProperty email;
    private final ObjectProperty<Date> joinedDate;
    private final StringProperty password;
    private final ObjectProperty<Timestamp> lastActivityDate;
    private final IntegerProperty roleID;
    private final BooleanProperty isDisabled;
    private final BooleanProperty isOwner;
    
    public UserObject(
        int id,
        String email,
        String password,
        Date joinedDate,
        Timestamp lastActivityDate,
        int roleID,
        boolean isDisabled,
        boolean isOwner
    ) {
        this.id = id;
        this.email = new SimpleStringProperty(email);
        this.joinedDate = new SimpleObjectProperty<>(joinedDate);
        this.password = new SimpleStringProperty(password);
        this.lastActivityDate = new SimpleObjectProperty<>(lastActivityDate);
        this.roleID = new SimpleIntegerProperty(roleID);
        this.isDisabled = new SimpleBooleanProperty(isDisabled);
        this.isOwner = new SimpleBooleanProperty(isOwner);
    }
    
    public int getID() {
        return id;
    }
    
    public StringProperty emailProperty() {
        return email;
    }
    
    public String getEmail() {
        return email.get();
    }
    
    public StringProperty passwordProperty() {
        return password;
    }
    
    public String getPassword() {
        return password.get();
    }
    
    public void setPassword(String password) {
        this.password.set(password);
    }
    
    public ObjectProperty<Date> joinedDateProperty() {
        return joinedDate;
    }
    
    public Date getJoinedDate() {
        return joinedDate.get();
    }
    
    public ObjectProperty<Timestamp> lastActivityDateProperty() {
        return lastActivityDate;
    }
    
    public Timestamp getLastActivityDate() {
        return lastActivityDate.get();
    }
    
    public void setLastActivityDate(Timestamp lastActivityDate) {
        this.lastActivityDate.set(lastActivityDate);
    }
    
    public IntegerProperty roleIDProperty() {
        return roleID;
    }
    
    public int getRoleID() {
        return roleID.get();
    }
    
    public void setRoleID(int roleID) {
        this.roleID.set(roleID);
    }
    
    public BooleanProperty isDisabledProperty() {
        return isDisabled;
    }
    
    public boolean isDisabled() {
        return isDisabled.get();
    }
    
    public void setDisabled(boolean disabled) {
        isDisabled.set(disabled);
    }
    
    public BooleanProperty isOwnerProperty() {
        return isOwner;
    }
    
    public boolean isOwner() {
        return isOwner.get();
    }
    
    public void setOwner(boolean owner) {
        isOwner.set(owner);
    }
}
