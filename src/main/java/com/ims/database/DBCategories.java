package com.ims.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DBCategories {
    private static Connection connection = Database.getConnection();
    
    public static HashMap<DBCategoriesColumn, Object> add(String name) {
        HashMap<DBCategoriesColumn, Object> row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                INSERT INTO CATEGORIES (%s) VALUES (?) RETURNING *;
                """.formatted(
                DBCategoriesColumn.NAME
            );
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();
            row = extractRowsFromResultSet(resultSet).get(0);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            closeStuff(resultSet, preparedStatement);
        }
        
        return row;
    }
    
    public static HashMap<DBCategoriesColumn, Object> update(int id, String name) {
        HashMap<DBCategoriesColumn, Object> row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                UPDATE CATEGORIES SET %s=? WHERE %s=? RETURNING *;
                """.formatted(
                DBCategoriesColumn.NAME,
                DBCategoriesColumn.ID
            );
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);
            resultSet = preparedStatement.executeQuery();
            row = extractRowsFromResultSet(resultSet).get(0);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            closeStuff(resultSet, preparedStatement);
        }
        
        return row;
    }
    
    public static HashMap<DBCategoriesColumn, Object> remove(int id) {
        HashMap<DBCategoriesColumn, Object> row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                DELETE FROM CATEGORIES WHERE %s=?;
                """.formatted(
                DBCategoriesColumn.ID
            );
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            row = extractRowsFromResultSet(resultSet).get(0);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            closeStuff(resultSet, preparedStatement);
        }
        
        return row;
    }
    
    private static ArrayList<HashMap<DBCategoriesColumn, Object>>
    extractRowsFromResultSet(ResultSet resultSet) throws SQLException {
        ArrayList<HashMap<DBCategoriesColumn, Object>> rows = new ArrayList<>();
        
        while (resultSet.next()) {
            HashMap<DBCategoriesColumn, Object> data = new HashMap<>();
            
            int retrievedId = resultSet.getInt(
                DBCategoriesColumn.ID.toString()
            );
            data.put(
                DBCategoriesColumn.ID,
                retrievedId
            );
            
            String retrievedEmail = resultSet.getString(
                DBCategoriesColumn.NAME.toString()
            );
            data.put(
                DBCategoriesColumn.NAME,
                retrievedEmail
            );
            
            Timestamp retrievedLastModified = resultSet.getTimestamp(
                DBCategoriesColumn.LAST_MODIFIED.toString()
            );
            data.put(
                DBCategoriesColumn.LAST_MODIFIED,
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
    public static ArrayList<HashMap<DBCategoriesColumn, Object>> getInRange(
        int startIndex,
        int length
    ) {
        ArrayList<HashMap<DBCategoriesColumn, Object>> rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM CATEGORIES OFFSET %s LIMIT %s;
                """.formatted(
                startIndex,
                length
            );
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            rows = extractRowsFromResultSet(resultSet);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            closeStuff(resultSet, preparedStatement);
        }
        
        return rows;
    }
    
    /**
     * Find a row that matches the specified conditions.
     * @param columnLabel The column name where the `compareValue` will be compared to.
     * @param compareValue The value used to check if it matches the column's value.
     * @return A row if found, and null if not.
     */
    public static ArrayList<HashMap<DBCategoriesColumn, Object>> get(
        DBCategoriesColumn columnLabel,
        Object compareValue
    ) {
        ArrayList<HashMap<DBCategoriesColumn, Object>> rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM CATEGORIES WHERE %s = ?;
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
            closeStuff(resultSet, preparedStatement);
        }
        
        return rows;
    }
    
    public static HashMap<DBCategoriesColumn, Object> getOne(
        DBCategoriesColumn columnLabel,
        Object compareValue
    ) {
        return get(columnLabel, compareValue).get(0);
    }
    
    
    
    private static void closeStuff(
        ResultSet resultSet,
        PreparedStatement preparedStatement
    ) {
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
}
