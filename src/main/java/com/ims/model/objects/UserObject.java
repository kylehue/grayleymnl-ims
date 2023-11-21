package com.ims.model.objects;

import java.sql.Date;
import java.sql.Timestamp;

public class UserObject {
    
    private int id;
    private String email;
    private String password;
    private Date joinedDate;
    private Timestamp lastActivityDate;
    private int roleID;
    private boolean isDisabled;
    private boolean isOwner;
    
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
        this.email = email;
        this.password = password;
        this.joinedDate = joinedDate;
        this.lastActivityDate = lastActivityDate;
        this.roleID = roleID;
        this.isDisabled = isDisabled;
        this.isOwner = isOwner;
    }
    
    public int getID() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public Date getJoinedDate() {
        return joinedDate;
    }
    
    public Timestamp getLastActivityDate() {
        return lastActivityDate;
    }
    
    public int getRoleID() {
        return roleID;
    }
    
    public boolean isDisabled() {
        return isDisabled;
    }
    
    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }
    
    public boolean isOwner() {
        return isOwner;
    }
}
