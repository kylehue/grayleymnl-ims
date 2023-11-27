package com.ims.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DBHistory {
    private static Connection connection = Database.getConnection();
    
    public enum Action {
        ADD_PRODUCT(0),
        REMOVE_PRODUCT(1),
        EDIT_PRODUCT(2),
        ADD_CATEGORY(3),
        REMOVE_CATEGORY(4),
        EDIT_CATEGORY(5);
        
        private final int code;
        
        Action(int code) {
            this.code = code;
        }
        
        public int getCode() {
            return code;
        }
        
        public static Action toAction(int code) {
            if (code == Action.ADD_PRODUCT.getCode()) {
                return ADD_PRODUCT;
            } else if (code == Action.REMOVE_PRODUCT.getCode()) {
                return REMOVE_PRODUCT;
            } else if (code == Action.EDIT_PRODUCT.getCode()) {
                return EDIT_PRODUCT;
            } else if (code == Action.ADD_CATEGORY.getCode()) {
                return ADD_CATEGORY;
            } else if (code == Action.REMOVE_CATEGORY.getCode()) {
                return REMOVE_CATEGORY;
            } else if (code == Action.EDIT_CATEGORY.getCode()) {
                return EDIT_CATEGORY;
            } else {
                throw new Error("Unknown action code.");
            }
        }
    }
    
    public enum Column {
        ID,
        ACTION_CODE,
        SUBJECT,
        USER_ID,
        LAST_MODIFIED
    }
    
    public static HistoryData add(
        Action action,
        String subject,
        Integer userID
    ) {
        HistoryData row = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String query = """
                INSERT INTO history (action_code, subject, user_id)
                VALUES (?, ?, ?)
                RETURNING *;
                """;
            
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, action.getCode());
            preparedStatement.setObject(2, subject);
            preparedStatement.setObject(3, userID);
            resultSet = preparedStatement.executeQuery();
            row = extractRowsFromResultSet(resultSet).get(0);
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeStuff(resultSet, preparedStatement);
        }
        
        return row;
    }
    
    public static HistoryListData getBulk(
        Set<Integer> excludeID,
        int length
    ) {
        HistoryListData rows = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            String query = """
                SELECT * FROM history
                WHERE id NOT IN (%s)
                ORDER BY last_modified ASC, id ASC
                LIMIT ?;
                """.formatted(
                excludeID.isEmpty() ? "-1" :
                    excludeID.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "))
            );
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
    
    private static HistoryListData
    extractRowsFromResultSet(ResultSet resultSet) throws SQLException {
        HistoryListData rows = new HistoryListData();
        
        while (resultSet.next()) {
            int retrievedId = resultSet.getInt(
                Column.ID.toString()
            );
            
            int retrievedAction = resultSet.getInt(
                Column.ACTION_CODE.toString()
            );
            
            String retrievedSubject = resultSet.getString(
                Column.SUBJECT.toString()
            );
            
            int retrievedUserID = resultSet.getInt(
                Column.USER_ID.toString()
            );
            
            Timestamp retrievedLastModified = resultSet.getTimestamp(
                Column.LAST_MODIFIED.toString()
            );
            
            HistoryData historyData = new HistoryData(
                retrievedId,
                Action.toAction(retrievedAction),
                retrievedSubject,
                retrievedUserID,
                retrievedLastModified
            );
            
            rows.add(historyData);
        }
        
        return rows;
    }
    
    public static class HistoryData {
        private final int id;
        private final Action action;
        private final String subject;
        private final int userID;
        private final Timestamp lastModified;
        
        public HistoryData(
            int id,
            Action action,
            String subject,
            int userID,
            Timestamp lastModified
        ) {
            this.id = id;
            this.action = action;
            this.subject = subject;
            this.userID = userID;
            this.lastModified = lastModified;
        }
        
        public int getID() {
            return id;
        }
        
        public Action getAction() {
            return action;
        }
        
        public String getSubject() {
            return subject;
        }
        
        public int getUserID() {
            return userID;
        }
        
        public Timestamp getLastModified() {
            return lastModified;
        }
    }
    
    public static class HistoryListData extends ArrayList<HistoryData> {
    }
}
