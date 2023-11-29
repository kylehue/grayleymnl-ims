package com.ims.database;

import com.ims.Config;

import java.sql.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class DBProducts {
    public enum Column {
        ID,
        NAME,
        PRICE,
        CATEGORY_ID,
        IMAGE_URL,
        CURRENT_STOCKS,
        EXPECTED_STOCKS,
        LAST_MODIFIED
    }
    
    public static ProductData add(
        String name,
        Integer categoryID,
        String imageURL,
        Integer currentStocks,
        Integer expectedStocks
    ) {
        ProductData row = null;
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
            
            Connection connection = Database.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("You are not connected to the database.");
            }
            
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
    
    public static ProductData update(
        int id,
        String name,
        Double price,
        Integer categoryID,
        String imageURL,
        Integer currentStocks,
        Integer expectedStocks
    ) {
        ProductData row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                UPDATE products
                SET name = COALESCE(?, name),
                price = COALESCE(?, price),
                category_id = COALESCE(?, category_id),
                image_url = COALESCE(?, image_url),
                current_stocks = COALESCE(?, current_stocks),
                expected_stocks = COALESCE(?, expected_stocks)
                WHERE id = ?
                RETURNING *;
                """;
            
            Connection connection = Database.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("You are not connected to the database.");
            }
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, name);
            preparedStatement.setObject(2, price);
            preparedStatement.setObject(3, categoryID);
            preparedStatement.setObject(4, imageURL);
            preparedStatement.setObject(5, currentStocks);
            preparedStatement.setObject(6, expectedStocks);
            preparedStatement.setObject(7, id);
            resultSet = preparedStatement.executeQuery();
            row = extractRowsFromResultSet(resultSet).get(0);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
        
        return row;
    }
    
    public static ProductData remove(int id) {
        ProductData row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                DELETE FROM products
                WHERE id = ?;
                """;
            
            Connection connection = Database.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("You are not connected to the database.");
            }
            
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
    
    private static ProductListData
    extractRowsFromResultSet(ResultSet resultSet) throws SQLException {
        ProductListData rows = new ProductListData();
        
        while (resultSet.next()) {
            int retrievedId = resultSet.getInt(
                Column.ID.toString()
            );
            
            String retrievedName = (String) resultSet.getObject(
                Column.NAME.toString()
            );
            
            Double retrievedPrice = (Double) resultSet.getObject(
                Column.PRICE.toString()
            );
            
            Integer retrievedCategoryID = (Integer) resultSet.getObject(
                Column.CATEGORY_ID.toString()
            );
            
            String retrievedImageURL = (String) resultSet.getObject(
                Column.IMAGE_URL.toString()
            );
            
            Integer retrievedCurrentStocks = (Integer) resultSet.getObject(
                Column.CURRENT_STOCKS.toString()
            );
            
            Integer retrievedExpectedStocks = (Integer) resultSet.getObject(
                Column.EXPECTED_STOCKS.toString()
            );
            
            Timestamp retrievedLastModified = (Timestamp) resultSet.getObject(
                Column.LAST_MODIFIED.toString()
            );
            
            ProductData data = new ProductData(
                retrievedId,
                retrievedName,
                retrievedPrice,
                retrievedCategoryID,
                retrievedImageURL,
                retrievedCurrentStocks,
                retrievedExpectedStocks,
                retrievedLastModified
            );
            
            rows.add(data);
        }
        
        return rows;
    }
    
    public static ProductListData getBulk(
        Set<Integer> excludeID,
        int length
    ) {
        ProductListData rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            String query = """
                SELECT * FROM products
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
     *
     * @param columnLabel  The column name where the `compareValue` will be compared to.
     * @param compareValue The value used to check if it matches the column's value.
     * @return A row if found, and null if not.
     */
    public static ProductListData get(
        Column columnLabel,
        Object compareValue
    ) {
        ProductListData rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT * FROM products
                WHERE %s = ?
                ORDER BY last_modified DESC, id ASC;
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
    
    public static ProductListData search(
        String regexPattern,
        String... categories
    ) {
        ProductListData rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        
        try {
            String query = """
                SELECT p.*, c.name FROM products p
                JOIN categories c
                ON p.category_id = c.id
                WHERE CONCAT(
                    p.name,
                    c.name,
                    p.current_stocks,
                    p.expected_stocks,
                    p.price
                ) ~* ?
                %s
                ORDER BY p.last_modified DESC, p.id;
                """.formatted(
                    categories.length == 0 ? "" :
                        "AND c.name IN (%s)".formatted(
                            "'" + String.join("', '", categories) + "'"
                        )
            );
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
    
    public static int getTotalProductsCount() {
        int count = 0;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            String query = """
                SELECT COUNT(*) AS count FROM products;
                """;
            Connection connection = Database.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("You are not connected to the database.");
            }
            
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            count = resultSet.getInt("count");
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
        
        return count;
    }
    
    public static int getOutOfStockProductsCount() {
        int count = 0;
        ProductListData rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            String query = """
                SELECT COUNT(*) AS count FROM products
                WHERE current_stocks = 0;
                """;
            Connection connection = Database.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("You are not connected to the database.");
            }
            
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            count = resultSet.getInt("count");
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
        
        return count;
    }
    
    public static int getLowStockProductsCount() {
        int count = 0;
        ProductListData rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            String query = """
                SELECT COUNT(*) AS count FROM products
                WHERE expected_stocks > 0
                AND current_stocks > 0
                AND (
                    CAST(current_stocks AS float) /
                    CAST(expected_stocks AS float)
                ) <= %s;
                """.formatted(Config.lowStockRate);
            Connection connection = Database.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("You are not connected to the database.");
            }
            
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            count = resultSet.getInt("count");
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
        
        return count;
    }
    
    public static class ProductData {
        private final int id;
        private final String name;
        private final Double price;
        private final Integer categoryID;
        private final String imageURL;
        private final Integer currentStocks;
        private final Integer expectedStocks;
        private final Timestamp lastModified;
        public ProductData(
            int id,
            String name,
            Double price,
            Integer categoryID,
            String imageURL,
            Integer currentStocks,
            Integer expectedStocks,
            Timestamp lastModified
        ) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.categoryID = categoryID;
            this.imageURL = imageURL;
            this.currentStocks = currentStocks;
            this.expectedStocks = expectedStocks;
            this.lastModified = lastModified;
        }
        
        public int getID() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public String getImageURL() {
            return imageURL;
        }
        
        public Double getPrice() {
            return price;
        }
        
        public Integer getCategoryID() {
            return categoryID;
        }
        
        public Integer getCurrentStocks() {
            return currentStocks;
        }
        
        public Integer getExpectedStocks() {
            return expectedStocks;
        }
        
        public Timestamp getLastModified() {
            return lastModified;
        }
    }
    
    public static class ProductListData extends ArrayList<ProductData> {}
}
