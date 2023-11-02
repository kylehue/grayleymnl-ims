package com.ims.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DBUsers {
    private static Connection connection = Database.getConnection();
    
    public enum Column {
        ID,
        EMAIL,
        PASSWORD,
        JOINED_DATE,
        LAST_ACTIVITY_DATE
    }
    
    public static void add(String email, String password) {
        try {
            String query = """
                INSERT INTO USERS (%s, %s) VALUES (?, ?);
                """.formatted(
                DBUsers.Column.EMAIL,
                DBUsers.Column.PASSWORD
            );
            
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    
    public static ArrayList<HashMap<DBUsers.Column, Object>> get(
        DBUsers.Column columnLabel,
        Object compareValue
    ) {
        ArrayList<HashMap<DBUsers.Column, Object>> rows = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM USERS WHERE %s = ?;
                """.formatted(
                columnLabel
            );
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, compareValue);
            resultSet = preparedStatement.executeQuery();
            
            HashMap<DBUsers.Column, Object> data = new HashMap<>();
            while (resultSet.next()) {
                int retrievedId = resultSet.getInt(
                    DBUsers.Column.ID.toString()
                );
                data.put(
                    DBUsers.Column.ID,
                    retrievedId
                );
                
                String retrievedEmail = resultSet.getString(
                    DBUsers.Column.EMAIL.toString()
                );
                data.put(
                    DBUsers.Column.EMAIL,
                    retrievedEmail
                );
                
                String retrievedPassword = resultSet.getString(
                    DBUsers.Column.PASSWORD.toString()
                );
                data.put(
                    DBUsers.Column.PASSWORD,
                    retrievedPassword
                );
                
                Date retrievedJoinedDate = resultSet.getDate(
                    DBUsers.Column.JOINED_DATE.toString()
                );
                data.put(
                    DBUsers.Column.JOINED_DATE,
                    retrievedJoinedDate
                );
                
                Date retrievedLastActivityDate = resultSet.getTimestamp(
                    DBUsers.Column.LAST_ACTIVITY_DATE.toString()
                );
                data.put(
                    DBUsers.Column.LAST_ACTIVITY_DATE,
                    retrievedLastActivityDate
                );
                
                rows.add(data);
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
        }
        
        return rows;
    }
}
