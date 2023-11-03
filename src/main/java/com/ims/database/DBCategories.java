package com.ims.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DBCategories {
    private static Connection connection = Database.getConnection();
    
    public enum Column {
        ID,
        NAME,
        LAST_MODIFIED
    }
    
    public static HashMap<Column, Object> add(String name) {
        HashMap<Column, Object> row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                INSERT INTO categories (%s)
                VALUES (?)
                RETURNING *;
                """.formatted(
                Column.NAME
            );
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();
            row = extractRowsFromResultSet(resultSet).get(0);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
        
        return row;
    }
    
    public static HashMap<Column, Object> update(int id, String name) {
        HashMap<Column, Object> row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                UPDATE categories
                SET %s=?
                WHERE %s=?
                RETURNING *;
                """.formatted(
                Column.NAME,
                Column.ID
            );
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);
            resultSet = preparedStatement.executeQuery();
            row = extractRowsFromResultSet(resultSet).get(0);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
        
        return row;
    }
    
    public static HashMap<Column, Object> remove(int id) {
        HashMap<Column, Object> row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                DELETE FROM categories
                WHERE %s=?;
                """.formatted(
                Column.ID
            );
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
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
            
            String retrievedName = resultSet.getString(
                Column.NAME.toString()
            );
            data.put(
                Column.NAME,
                retrievedName
            );
            
            Timestamp retrievedLastModified = resultSet.getTimestamp(
                Column.LAST_MODIFIED.toString()
            );
            data.put(
                Column.LAST_MODIFIED,
                retrievedLastModified
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
    public static ArrayList<HashMap<Column, Object>> getInRange(
        int startIndex,
        int length
    ) {
        ArrayList<HashMap<Column, Object>> rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM categories
                ORDER BY %s DESC
                OFFSET %s
                LIMIT %s;
                """.formatted(
                Column.LAST_MODIFIED,
                startIndex,
                length
            );
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            rows = extractRowsFromResultSet(resultSet);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
        
        return rows;
    }
    
    /**
     * Find a row that matches the specified conditions.
     * @param columnLabel The column name where the `compareValue` will be compared to.
     * @param compareValue The value used to check if it matches the column's value.
     * @return A row if found, and null if not.
     */
    public static ArrayList<HashMap<Column, Object>> get(
        Column columnLabel,
        Object compareValue
    ) {
        ArrayList<HashMap<Column, Object>> rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM categories
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
    
    public static HashMap<Column, Object> getOne(
        Column columnLabel,
        Object compareValue
    ) {
        return get(columnLabel, compareValue).get(0);
    }
}
