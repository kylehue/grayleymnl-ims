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
        ROLE_ID,
        IS_DISABLED,
        IS_OWNER
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
        Integer roleID,
        Boolean isDisabled,
        Boolean isOwner
    ) {
        HashMap<DBUsers.Column, Object> row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                UPDATE users
                SET password = COALESCE(?, password),
                role_id = COALESCE(?, role_id),
                is_disabled = COALESCE(?, is_disabled),
                is_owner = COALESCE(?, is_owner)
                WHERE id = ?
                RETURNING *;
                """;
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, password);
            preparedStatement.setObject(2, roleID);
            preparedStatement.setObject(3, isDisabled);
            preparedStatement.setObject(4, isOwner);
            preparedStatement.setInt(5, id);
            resultSet = preparedStatement.executeQuery();
            row = extractRowsFromResultSet(resultSet).get(0);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
        
        return row;
    }
    
    public static void remove(int id) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                DELETE FROM users
                WHERE id = ?
                AND is_owner=false;
                """;
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
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
            
            boolean retrievedIsDisabled = resultSet.getBoolean(
                Column.IS_DISABLED.toString()
            );
            data.put(
                Column.IS_DISABLED,
                retrievedIsDisabled
            );
            
            boolean retrievedIsOwner = resultSet.getBoolean(
                Column.IS_OWNER.toString()
            );
            data.put(
                Column.IS_OWNER,
                retrievedIsOwner
            );
            
            rows.add(data);
        }
        
        return rows;
    }
    
    /**
     * Get rows that are in specified range.
     * @param startIndex The starting row index.
     * @param length The limit of rows to retrieve.
     * @return An ArrayList of rows.
     */
    public static ArrayList<HashMap<DBUsers.Column, Object>> getInRange(
        int startIndex,
        int length
    ) {
        ArrayList<HashMap<DBUsers.Column, Object>> rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM users
                ORDER BY joined_date DESC, id ASC
                OFFSET ?
                LIMIT ?;
                """;
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, startIndex);
            preparedStatement.setInt(2, length);
            resultSet = preparedStatement.executeQuery();
            rows = extractRowsFromResultSet(resultSet);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
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
    
    public static ArrayList<HashMap<Column, Object>> search(
        String regexPattern
    ) {
        ArrayList<HashMap<Column, Object>> rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT u.*, r.name FROM users u
                JOIN roles r
                ON u.role_id = r.id
                WHERE CONCAT(
                	u.email,
                	r.name,
                	TO_CHAR(joined_date, 'FMMonth, DD, YYYY'),
                	TO_CHAR(last_activity_date, 'FMMonth, DD, YYYY')
                ) ~* ?
                ORDER BY u.joined_date DESC, u.id;
                """;
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, regexPattern);
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
