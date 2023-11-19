package com.ims.database;

import java.sql.*;
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
        LAST_ACTIVITY_DATE,
        ROLE_ID
    }
    
    public static HashMap<Column, Object> add(String email, String password) {
        HashMap<Column, Object> row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                INSERT INTO users (%s, %s)
                VALUES (?, ?)
                RETURNING *;
                """.formatted(
                DBUsers.Column.EMAIL,
                DBUsers.Column.PASSWORD
            );
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();
            row = extractRowsFromResultSet(resultSet).get(0);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
        
        return row;
    }
    
    public static HashMap<DBUsers.Column, Object> update(
        int id,
        String password,
        Integer roleID
    ) {
        HashMap<DBUsers.Column, Object> row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                UPDATE users
                SET password = COALESCE(?, password),
                role_id = COALESCE(?, role_id)
                WHERE id = ?
                RETURNING *;
                """;
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, password);
            preparedStatement.setObject(2, roleID);
            preparedStatement.setInt(3, id);
            resultSet = preparedStatement.executeQuery();
            row = extractRowsFromResultSet(resultSet).get(0);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
        
        return row;
    }
    
    private static ArrayList<HashMap<Column, Object>>
    extractRowsFromResultSet(ResultSet resultSet) throws SQLException {
        ArrayList<HashMap<Column, Object>> rows = new ArrayList<>();
        
        while (resultSet.next()) {
            HashMap<Column, Object> data = new HashMap<>();
            
            int retrievedId = resultSet.getInt(
                Column.ID.toString()
            );
            data.put(
                Column.ID,
                retrievedId
            );
            
            String retrievedEmail = resultSet.getString(
                Column.EMAIL.toString()
            );
            data.put(
                Column.EMAIL,
                retrievedEmail
            );
            
            String retrievedPassword = resultSet.getString(
                Column.PASSWORD.toString()
            );
            data.put(
                Column.PASSWORD,
                retrievedPassword
            );
            
            Date retrievedJoinedDate = resultSet.getDate(
                Column.JOINED_DATE.toString()
            );
            data.put(
                Column.JOINED_DATE,
                retrievedJoinedDate
            );
            
            Date retrievedLastActivityDate = resultSet.getTimestamp(
                Column.LAST_ACTIVITY_DATE.toString()
            );
            data.put(
                Column.LAST_ACTIVITY_DATE,
                retrievedLastActivityDate
            );
            
            int retrievedRoleID = resultSet.getInt(
                Column.ROLE_ID.toString()
            );
            data.put(
                Column.ROLE_ID,
                retrievedRoleID
            );
            
            rows.add(data);
        }
        
        return rows;
    }
    
    public static ArrayList<HashMap<DBUsers.Column, Object>> get(
        DBUsers.Column columnLabel,
        Object compareValue
    ) {
        ArrayList<HashMap<DBUsers.Column, Object>> rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM users
                WHERE %s = ?;
                """.formatted(
                columnLabel
            );
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, compareValue);
            resultSet = preparedStatement.executeQuery();
            rows = extractRowsFromResultSet(resultSet);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
        
        return rows;
    }
    
    public static HashMap<DBUsers.Column, Object> getOne(
        DBUsers.Column columnLabel,
        Object compareValue
    ) {
        ArrayList<HashMap<DBUsers.Column, Object>> rows = get(columnLabel, compareValue);
        return !rows.isEmpty() ? rows.getFirst() : null;
    }
}
