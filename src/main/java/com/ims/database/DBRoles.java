package com.ims.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DBRoles {
    private static Connection connection = Database.getConnection();
    
    public enum Column {
        ID,
        NAME,
        ALLOW_ADD_CATEGORY,
        ALLOW_DELETE_CATEGORY,
        ALLOW_EDIT_CATEGORY,
        ALLOW_ADD_PRODUCT,
        ALLOW_DELETE_PRODUCT,
        ALLOW_EDIT_PRODUCT,
        LAST_MODIFIED
    }
    
    public static HashMap<DBRoles.Column, Object> add(String name) {
        HashMap<DBRoles.Column, Object> row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                INSERT INTO roles (name)
                VALUES (?)
                RETURNING *;
                """;
            
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
    
    public static HashMap<DBRoles.Column, Object> update(
        int id,
        String name,
        Boolean allowAddCategory,
        Boolean allowDeleteCategory,
        Boolean allowEditCategory,
        Boolean allowAddProduct,
        Boolean allowDeleteProduct,
        Boolean allowEditProduct
    ) {
        HashMap<DBRoles.Column, Object> row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                UPDATE roles
                SET name = ?,
                allow_add_category = ?,
                allow_delete_category = ?,
                allow_edit_category = ?,
                allow_add_product = ?,
                allow_delete_product = ?,
                allow_edit_product = ?
                WHERE id = ?
                RETURNING *;
                """;
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, name);
            preparedStatement.setObject(2, allowAddCategory);
            preparedStatement.setObject(3, allowDeleteCategory);
            preparedStatement.setObject(4, allowEditCategory);
            preparedStatement.setObject(5, allowAddProduct);
            preparedStatement.setObject(6, allowDeleteProduct);
            preparedStatement.setObject(7, allowEditProduct);
            preparedStatement.setObject(8, id);
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
                DELETE FROM roles
                WHERE id = ?;
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
    
    private static ArrayList<HashMap<DBRoles.Column, Object>>
    extractRowsFromResultSet(ResultSet resultSet) throws SQLException {
        ArrayList<HashMap<DBRoles.Column, Object>> rows = new ArrayList<>();
        
        while (resultSet.next()) {
            HashMap<DBRoles.Column, Object> data = new HashMap<>();
            
            int retrievedId = resultSet.getInt(
                DBRoles.Column.ID.toString()
            );
            data.put(
                DBRoles.Column.ID,
                retrievedId
            );
            
            String retrievedName = resultSet.getString(
                DBRoles.Column.NAME.toString()
            );
            data.put(
                DBRoles.Column.NAME,
                retrievedName
            );
            
            boolean retrievedAllowAddCategory = resultSet.getBoolean(
                Column.ALLOW_ADD_CATEGORY.toString()
            );
            data.put(
                Column.ALLOW_ADD_CATEGORY,
                retrievedAllowAddCategory
            );
            
            boolean retrievedAllowDeleteCategory = resultSet.getBoolean(
                Column.ALLOW_DELETE_CATEGORY.toString()
            );
            data.put(
                Column.ALLOW_DELETE_CATEGORY,
                retrievedAllowDeleteCategory
            );
            
            boolean retrievedAllowEditCategory = resultSet.getBoolean(
                Column.ALLOW_EDIT_CATEGORY.toString()
            );
            data.put(
                Column.ALLOW_EDIT_CATEGORY,
                retrievedAllowEditCategory
            );
            
            boolean retrievedAllowAddProduct = resultSet.getBoolean(
                Column.ALLOW_ADD_PRODUCT.toString()
            );
            data.put(
                Column.ALLOW_ADD_PRODUCT,
                retrievedAllowAddProduct
            );
            
            boolean retrievedAllowDeleteProduct = resultSet.getBoolean(
                Column.ALLOW_DELETE_PRODUCT.toString()
            );
            data.put(
                Column.ALLOW_DELETE_PRODUCT,
                retrievedAllowDeleteProduct
            );
            
            boolean retrievedAllowEditProduct = resultSet.getBoolean(
                Column.ALLOW_EDIT_PRODUCT.toString()
            );
            data.put(
                Column.ALLOW_EDIT_PRODUCT,
                retrievedAllowEditProduct
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
    public static ArrayList<HashMap<DBRoles.Column, Object>> getInRange(
        int startIndex,
        int length
    ) {
        ArrayList<HashMap<DBRoles.Column, Object>> rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM roles
                ORDER BY last_modified DESC, id ASC
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
    
    public static ArrayList<HashMap<DBRoles.Column, Object>> get(
        DBRoles.Column columnLabel,
        Object compareValue
    ) {
        ArrayList<HashMap<DBRoles.Column, Object>> rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM roles
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
    
    public static HashMap<DBRoles.Column, Object> getOne(
        DBRoles.Column columnLabel,
        Object compareValue
    ) {
        ArrayList<HashMap<DBRoles.Column, Object>> rows = get(columnLabel, compareValue);
        return !rows.isEmpty() ? rows.getFirst() : null;
    }
}
