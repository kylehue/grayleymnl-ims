package com.ims.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class DBCategories {
    public enum Column {
        ID,
        NAME,
        LAST_MODIFIED
    }
    
    public static CategoryData add(String name) {
        CategoryData row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                INSERT INTO categories (name)
                VALUES (?)
                RETURNING *;
                """;
            
            Connection connection = Database.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("You are not connected to the database.");
            }
            
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
    
    public static CategoryData update(int id, String name) {
        CategoryData row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                UPDATE categories
                SET name = ?
                WHERE id = ?
                RETURNING *;
                """;
            
            Connection connection = Database.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("You are not connected to the database.");
            }
            
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
    
    public static void remove(int id) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                DELETE FROM categories
                WHERE id = ?;
                """;
            
            Connection connection = Database.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("You are not connected to the database.");
            }
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
    }
    
    private static CategoryListData
    extractRowsFromResultSet(ResultSet resultSet) throws SQLException {
        CategoryListData rows = new CategoryListData();
        
        while (resultSet.next()) {
            
            int retrievedId = resultSet.getInt(
                Column.ID.toString()
            );
            
            String retrievedName = resultSet.getString(
                Column.NAME.toString()
            );
            
            Timestamp retrievedLastModified = resultSet.getTimestamp(
                Column.LAST_MODIFIED.toString()
            );
            
            CategoryData categoryData = new CategoryData(
                retrievedId,
                retrievedName,
                retrievedLastModified
            );
            
            rows.add(categoryData);
        }
        
        return rows;
    }
    
    public static CategoryListData getBulk(
        Set<Integer> excludeID,
        int length
    ) {
        CategoryListData rows = new CategoryListData();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            String query = """
                SELECT * FROM categories
                WHERE id NOT IN (%s)
                ORDER BY last_modified ASC, id ASC
                LIMIT ?;
                """.formatted(
                excludeID.isEmpty() ? "-1" :
                    excludeID.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "))
            );
            Connection connection = Database.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("You are not connected to the database.");
            }
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, length);
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
    public static CategoryListData get(
        Column columnLabel,
        Object compareValue
    ) {
        CategoryListData rows = new CategoryListData();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM categories
                WHERE %s = ?;
                """.formatted(
                columnLabel
            );
            Connection connection = Database.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("You are not connected to the database.");
            }
            
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
    
    public static CategoryListData search(
        String regexPattern
    ) {
        CategoryListData rows = new CategoryListData();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM categories
                WHERE name ~* ?
                ORDER BY last_modified DESC, id;
                """;
            Connection connection = Database.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("You are not connected to the database.");
            }
            
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
    
    public static CategoryData getOne(
        Column columnLabel,
        Object compareValue
    ) {
        CategoryListData rows = get(columnLabel, compareValue);
        return !rows.isEmpty() ? rows.getFirst() : null;
    }
    
    public static class CategoryData {
        private final int id;
        private final String name;
        private final Timestamp lastModified;
        
        public CategoryData(
            int id,
            String name,
            Timestamp lastModified
        ) {
            this.id = id;
            this.name = name;
            this.lastModified = lastModified;
        }
        
        public int getID() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public Timestamp getLastModified() {
            return lastModified;
        }
    }
    
    public static class CategoryListData extends ArrayList<CategoryData> {}
}
