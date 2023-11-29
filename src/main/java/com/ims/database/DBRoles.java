package com.ims.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class DBRoles {
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
    
    public static RoleData add(String name) {
        RoleData row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                INSERT INTO roles (name)
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
    
    public static RoleData update(
        int id,
        String name,
        Boolean allowAddCategory,
        Boolean allowDeleteCategory,
        Boolean allowEditCategory,
        Boolean allowAddProduct,
        Boolean allowDeleteProduct,
        Boolean allowEditProduct
    ) {
        RoleData row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                UPDATE roles
                SET name = COALESCE(?, name),
                allow_add_category = COALESCE(?, allow_add_category),
                allow_delete_category = COALESCE(?, allow_delete_category),
                allow_edit_category = COALESCE(?, allow_edit_category),
                allow_add_product = COALESCE(?, allow_add_product),
                allow_delete_product = COALESCE(?, allow_delete_product),
                allow_edit_product = COALESCE(?, allow_edit_product)
                WHERE id = ?
                RETURNING *;
                """;
            
            Connection connection = Database.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("You are not connected to the database.");
            }
            
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
    
    private static RoleListData
    extractRowsFromResultSet(ResultSet resultSet) throws SQLException {
        RoleListData rows = new RoleListData();
        
        while (resultSet.next()) {
            int retrievedId = resultSet.getInt(
                DBRoles.Column.ID.toString()
            );
            
            String retrievedName = resultSet.getString(
                DBRoles.Column.NAME.toString()
            );
            
            boolean retrievedAllowAddCategory = resultSet.getBoolean(
                Column.ALLOW_ADD_CATEGORY.toString()
            );
            
            boolean retrievedAllowDeleteCategory = resultSet.getBoolean(
                Column.ALLOW_DELETE_CATEGORY.toString()
            );
            
            boolean retrievedAllowEditCategory = resultSet.getBoolean(
                Column.ALLOW_EDIT_CATEGORY.toString()
            );
            
            boolean retrievedAllowAddProduct = resultSet.getBoolean(
                Column.ALLOW_ADD_PRODUCT.toString()
            );
            
            boolean retrievedAllowDeleteProduct = resultSet.getBoolean(
                Column.ALLOW_DELETE_PRODUCT.toString()
            );
            
            boolean retrievedAllowEditProduct = resultSet.getBoolean(
                Column.ALLOW_EDIT_PRODUCT.toString()
            );
            
            Timestamp retrievedLastModified = resultSet.getTimestamp(
                Column.LAST_MODIFIED.toString()
            );
            
            RoleData roleData = new RoleData(
                retrievedId,
                retrievedName,
                retrievedAllowAddCategory,
                retrievedAllowDeleteCategory,
                retrievedAllowEditCategory,
                retrievedAllowAddProduct,
                retrievedAllowDeleteProduct,
                retrievedAllowEditProduct,
                retrievedLastModified
            );
            
            rows.add(roleData);
        }
        
        return rows;
    }
    
    public static RoleListData getBulk(
        Set<Integer> excludeID,
        int length
    ) {
        RoleListData rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            String query = """
                SELECT * FROM roles
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
    
    public static RoleListData get(
        DBRoles.Column columnLabel,
        Object compareValue
    ) {
        RoleListData rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM roles
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
    
    public static RoleListData search(
        String regexPattern
    ) {
        RoleListData rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM roles
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
    
    public static RoleData getOne(
        DBRoles.Column columnLabel,
        Object compareValue
    ) {
        RoleListData rows = get(columnLabel, compareValue);
        return !rows.isEmpty() ? rows.getFirst() : null;
    }
    
    public static class RoleData {
        private final int id;
        private final String name;
        private final Boolean isAllowAddCategory;
        private final Boolean isAllowDeleteCategory;
        private final Boolean isAllowEditCategory;
        private final Boolean isAllowAddProduct;
        private final Boolean isAllowDeleteProduct;
        private final Boolean isAllowEditProduct;
        private final Timestamp lastModified;
        
        public RoleData(
            int id,
            String name,
            Boolean isAllowAddCategory,
            Boolean isAllowDeleteCategory,
            Boolean isAllowEditCategory,
            Boolean isAllowAddProduct,
            Boolean isAllowDeleteProduct,
            Boolean isAllowEditProduct,
            Timestamp lastModified
        ) {
            this.id = id;
            this.name = name;
            this.isAllowAddCategory = isAllowAddCategory;
            this.isAllowDeleteCategory = isAllowDeleteCategory;
            this.isAllowEditCategory = isAllowEditCategory;
            this.isAllowAddProduct = isAllowAddProduct;
            this.isAllowDeleteProduct = isAllowDeleteProduct;
            this.isAllowEditProduct = isAllowEditProduct;
            this.lastModified = lastModified;
        }
        
        public int getID() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public Boolean isAllowAddCategory() {
            return isAllowAddCategory;
        }
        
        public Boolean isAllowDeleteCategory() {
            return isAllowDeleteCategory;
        }
        
        public Boolean isAllowEditCategory() {
            return isAllowEditCategory;
        }
        
        public Boolean isAllowAddProduct() {
            return isAllowAddProduct;
        }
        
        public Boolean isAllowDeleteProduct() {
            return isAllowDeleteProduct;
        }
        
        public Boolean isAllowEditProduct() {
            return isAllowEditProduct;
        }
        
        public Timestamp getLastModified() {
            return lastModified;
        }
    }
    
    public static class RoleListData extends ArrayList<RoleData> {
    }
}
