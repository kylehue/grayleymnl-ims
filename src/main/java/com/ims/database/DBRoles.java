package com.ims.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        ALLOW_EDIT_PRODUCT
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
            
            rows.add(data);
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
