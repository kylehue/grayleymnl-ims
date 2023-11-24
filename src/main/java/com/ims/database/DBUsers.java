package com.ims.database;

import java.sql.*;
import java.util.ArrayList;

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
    
    public static UserData add(String email, String password) {
        UserData row = null;
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
    
    public static UserData update(
        int id,
        String password,
        Integer roleID,
        Boolean isDisabled,
        Boolean isOwner
    ) {
        UserData row = null;
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
    
    private static UserListData
    extractRowsFromResultSet(ResultSet resultSet) throws SQLException {
        UserListData rows = new UserListData();
        
        while (resultSet.next()) {
            int retrievedId = resultSet.getInt(
                Column.ID.toString()
            );
            
            String retrievedEmail = resultSet.getString(
                Column.EMAIL.toString()
            );
            
            String retrievedPassword = resultSet.getString(
                Column.PASSWORD.toString()
            );
            
            Date retrievedJoinedDate = resultSet.getDate(
                Column.JOINED_DATE.toString()
            );
            
            Timestamp retrievedLastActivityDate = resultSet.getTimestamp(
                Column.LAST_ACTIVITY_DATE.toString()
            );
            
            int retrievedRoleID = resultSet.getInt(
                Column.ROLE_ID.toString()
            );
            
            boolean retrievedIsDisabled = resultSet.getBoolean(
                Column.IS_DISABLED.toString()
            );
            
            boolean retrievedIsOwner = resultSet.getBoolean(
                Column.IS_OWNER.toString()
            );
            
            UserData userData = new UserData(
                retrievedId,
                retrievedEmail,
                retrievedPassword,
                retrievedJoinedDate,
                retrievedLastActivityDate,
                retrievedRoleID,
                retrievedIsDisabled,
                retrievedIsOwner
            );
            
            rows.add(userData);
        }
        
        return rows;
    }
    
    /**
     * Get rows that are in specified range.
     * @param startIndex The starting row index.
     * @param length The limit of rows to retrieve.
     * @return An ArrayList of rows.
     */
    public static UserListData getInRange(
        int startIndex,
        int length
    ) {
        UserListData rows = null;
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
    
    public static UserListData get(
        DBUsers.Column columnLabel,
        Object compareValue
    ) {
        UserListData rows = null;
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
    
    public static UserListData search(
        String regexPattern
    ) {
        UserListData rows = null;
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
    
    public static UserData getOne(
        DBUsers.Column columnLabel,
        Object compareValue
    ) {
        UserListData rows = get(columnLabel, compareValue);
        return !rows.isEmpty() ? rows.getFirst() : null;
    }
    
    public static class UserData {
        private final int id;
        private final String email;
        private final String password;
        private final Date joinedDate;
        private final Timestamp lastActivityDate;
        private final Integer roleID;
        private final Boolean isDisabled;
        private final Boolean isOwner;
        
        public UserData(
            int id,
            String email,
            String password,
            Date joinedDate,
            Timestamp lastActivityDate,
            Integer roleID,
            Boolean isDisabled,
            Boolean isOwner
        ) {
            this.id = id;
            this.email = email;
            this.password = password;
            this.joinedDate = joinedDate;
            this.lastActivityDate = lastActivityDate;
            this.roleID = roleID;
            this.isDisabled = isDisabled;
            this.isOwner = isOwner;
        }
        
        public int getID() {
            return id;
        }
        
        public String getEmail() {
            return email;
        }
        
        public String getPassword() {
            return password;
        }
        
        public Date getJoinedDate() {
            return joinedDate;
        }
        
        public Timestamp getLastActivityDate() {
            return lastActivityDate;
        }
        
        public Integer getRoleID() {
            return roleID;
        }
        
        public Boolean isDisabled() {
            return isDisabled;
        }
        
        public Boolean isOwner() {
            return isOwner;
        }
    }
    
    public static class UserListData extends ArrayList<UserData> {}
}
