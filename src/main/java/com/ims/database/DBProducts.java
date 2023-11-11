package com.ims.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DBProducts {
    private static Connection connection = Database.getConnection();
    
    public enum Column {
        ID,
        NAME,
        CATEGORY_ID,
        IMAGE_URL,
        CURRENT_STOCKS,
        EXPECTED_STOCKS,
        LAST_MODIFIED
    }
    
    public static HashMap<Column, Object> add(
        String name,
        int categoryID,
        String imageURL,
        int currentStocks,
        int expectedStocks
    ) {
        HashMap<Column, Object> row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                INSERT INTO products (
                    name,
                    category_id,
                    image_url,
                    current_stocks,
                    expected_stocks
                )
                VALUES (?, ?, ?, ?, ?)
                RETURNING *;
                """;
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, categoryID);
            preparedStatement.setString(3, imageURL);
            preparedStatement.setInt(4, currentStocks);
            preparedStatement.setInt(5, expectedStocks);
            resultSet = preparedStatement.executeQuery();
            row = extractRowsFromResultSet(resultSet).get(0);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
        
        return row;
    }
    
    public static HashMap<Column, Object> update(
        int id,
        String name,
        Integer categoryID,
        String imageURL,
        Integer currentStocks,
        Integer expectedStocks
    ) {
        HashMap<Column, Object> row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                UPDATE products
                SET name = COALESCE(?, name),
                category_id = COALESCE(?, category_id),
                image_url = COALESCE(?, image_url),
                current_stocks = COALESCE(?, current_stocks),
                expected_stocks = COALESCE(?, expected_stocks)
                WHERE id = ?
                RETURNING *;
                """;
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, categoryID);
            preparedStatement.setString(3, imageURL);
            preparedStatement.setInt(4, currentStocks);
            preparedStatement.setInt(5, expectedStocks);
            preparedStatement.setInt(6, id);
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
                DELETE FROM products
                WHERE id = ?;
                """;
            
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
            
            int retrievedCategoryID = resultSet.getInt(
                Column.CATEGORY_ID.toString()
            );
            data.put(
                Column.CATEGORY_ID,
                retrievedCategoryID
            );
            
            String retrievedImageURL = resultSet.getString(
                Column.IMAGE_URL.toString()
            );
            data.put(
                Column.IMAGE_URL,
                retrievedImageURL
            );
            
            int retrievedCurrentStocks = resultSet.getInt(
                Column.CURRENT_STOCKS.toString()
            );
            data.put(
                Column.CURRENT_STOCKS,
                retrievedCurrentStocks
            );
            
            int retrievedExpectedStocks = resultSet.getInt(
                Column.EXPECTED_STOCKS.toString()
            );
            data.put(
                Column.EXPECTED_STOCKS,
                retrievedExpectedStocks
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
     *
     * @param startIndex The starting row index.
     * @param length     The limit of rows to retrieve.
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
                SELECT * FROM products
                ORDER BY last_modified DESC
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
    
    /**
     * Find a row that matches the specified conditions.
     *
     * @param columnLabel  The column name where the `compareValue` will be compared to.
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
                SELECT * FROM products
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
}
