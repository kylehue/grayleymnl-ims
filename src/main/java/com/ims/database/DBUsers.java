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
    
    public static void add(String email, String password) {
        try {
            String query = """
                INSERT INTO USERS (%s, %s) VALUES (?, ?);
                """.formatted(
                DBUsersColumn.EMAIL,
                DBUsersColumn.PASSWORD
            );
            
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    
    public static ArrayList<HashMap<DBUsersColumn, Object>> get(
        DBUsersColumn columnLabel,
        Object compareValue
    ) {
        ArrayList<HashMap<DBUsersColumn, Object>> rows = new ArrayList<>();
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
            
            HashMap<DBUsersColumn, Object> data = new HashMap<>();
            while (resultSet.next()) {
                int retrievedId = resultSet.getInt(
                    DBUsersColumn.ID.toString()
                );
                data.put(
                    DBUsersColumn.ID,
                    retrievedId
                );
                
                String retrievedEmail = resultSet.getString(
                    DBUsersColumn.EMAIL.toString()
                );
                data.put(
                    DBUsersColumn.EMAIL,
                    retrievedEmail
                );
                
                String retrievedPassword = resultSet.getString(
                    DBUsersColumn.PASSWORD.toString()
                );
                data.put(
                    DBUsersColumn.PASSWORD,
                    retrievedPassword
                );
                
                Date retrievedJoinedDate = resultSet.getDate(
                    DBUsersColumn.JOINED_DATE.toString()
                );
                data.put(
                    DBUsersColumn.JOINED_DATE,
                    retrievedJoinedDate
                );
                
                Date retrievedLastActivityDate = resultSet.getTimestamp(
                    DBUsersColumn.LAST_ACTIVITY_DATE.toString()
                );
                data.put(
                    DBUsersColumn.LAST_ACTIVITY_DATE,
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
