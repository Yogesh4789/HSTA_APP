package com.helpdesk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.helpdesk.bean.UserBean;

public class UserDAO {

    public UserBean validateUser(String email, String password) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        UserBean user = null;

        String sql = "SELECT user_id, name, email, password, role, created_at "
                + "FROM `USER` WHERE email = ? AND password = ? AND password != '*' AND is_verified = 1";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                user = mapUser(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while validating user login.", e);
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return user;
    }

    public boolean registerUser(UserBean user) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isRegistered = false;

        String sql = "INSERT INTO `USER` (name, email, password, role) VALUES (?, ?, ?, ?)";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getRole());

            isRegistered = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            if (isDuplicateEmailViolation(e)) {
                return false;
            }
            throw new RuntimeException("Database error while registering user.", e);
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isRegistered;
    }

    public UserBean getUserById(int userId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        UserBean user = null;

        String sql = "SELECT user_id, name, email, password, role, created_at "
                + "FROM `USER` WHERE user_id = ?";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                user = mapUser(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return user;
    }

    public UserBean getUserByEmail(String email) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        UserBean user = null;

        String sql = "SELECT user_id, name, email, password, role, created_at "
                + "FROM `USER` WHERE email = ? AND password != '*'";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                user = mapUser(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return user;
    }

    public List<UserBean> getAllAgents() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<UserBean> agents = new ArrayList<UserBean>();

        String sql = "SELECT user_id, name, email, password, role, created_at "
                + "FROM `USER` WHERE role = 'AGENT' AND password != '*' ORDER BY name";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                agents.add(mapUser(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return agents;
    }

    public List<UserBean> getAllUsers() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<UserBean> users = new ArrayList<UserBean>();

        String sql = "SELECT user_id, name, email, password, role, created_at "
                + "FROM `USER` WHERE password != '*' ORDER BY user_id";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return users;
    }

    public boolean updateUserRole(int userId, String newRole) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isUpdated = false;

        String sql = "UPDATE `USER` SET role = ? WHERE user_id = ?";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newRole);
            preparedStatement.setInt(2, userId);
            isUpdated = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isUpdated;
    }

    public boolean deleteUser(int userId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isDeleted = false;

        String sql = "UPDATE `USER` SET password = '*', email = CONCAT('deleted_', user_id, '_', SUBSTRING(email, 1, 70)), "
                + "reset_token = NULL, reset_expiry = NULL, verification_token = NULL, verification_expiry = NULL "
                + "WHERE user_id = ?";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            isDeleted = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isDeleted;
    }

    public boolean createPasswordResetToken(String email, String token, int expiryMinutes) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isUpdated = false;

        String sql = "UPDATE `USER` SET reset_token = ?, reset_expiry = DATE_ADD(NOW(), INTERVAL ? MINUTE) "
                + "WHERE email = ? AND is_verified = 1 AND password != '*'";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, token);
            preparedStatement.setInt(2, expiryMinutes);
            preparedStatement.setString(3, email);
            isUpdated = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error while creating password reset token.", e);
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isUpdated;
    }

    public boolean resetPasswordByToken(String token, String newPassword) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isUpdated = false;

        String sql = "UPDATE `USER` SET password = ?, reset_token = NULL, reset_expiry = NULL "
                + "WHERE reset_token = ? AND reset_expiry > NOW() AND is_verified = 1 AND password != '*'";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newPassword);
            preparedStatement.setString(2, token);
            isUpdated = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error while resetting password.", e);
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isUpdated;
    }

    public boolean registerPendingUser(UserBean user, String token, int expiryMinutes) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isRegistered = false;

        String sql = "INSERT INTO `USER` (name, email, password, role, is_verified, verification_token, verification_expiry) "
                + "VALUES (?, ?, ?, ?, 0, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE))";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getRole());
            preparedStatement.setString(5, token);
            preparedStatement.setInt(6, expiryMinutes);

            isRegistered = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            if (isDuplicateEmailViolation(e)) {
                return refreshUnverifiedUser(user, token, expiryMinutes);
            }
            throw new RuntimeException("Database error while creating unverified user.", e);
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isRegistered;
    }

    public UserBean getPendingUserByToken(String token) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        UserBean user = null;

        String sql = "SELECT user_id, name, email, password FROM `USER` "
                + "WHERE verification_token = ? AND verification_expiry > NOW() AND is_verified = 0 AND password != '*'";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, token);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                user = new UserBean();
                user.setUserId(resultSet.getInt("user_id"));
                user.setName(resultSet.getString("name"));
                user.setEmail(resultSet.getString("email"));
                user.setPassword(resultSet.getString("password"));
                user.setRole("USER");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return user;
    }

    public boolean deletePendingUser(int pendingUserId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isDeleted = false;

        String sql = "UPDATE `USER` SET is_verified = 1, verification_token = NULL, verification_expiry = NULL "
                + "WHERE user_id = ? AND is_verified = 0";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, pendingUserId);
            isDeleted = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }
        return isDeleted;
    }

    private UserBean mapUser(ResultSet resultSet) throws SQLException {
        UserBean user = new UserBean();
        user.setUserId(resultSet.getInt("user_id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setRole(resultSet.getString("role"));
        user.setCreatedAt(resultSet.getTimestamp("created_at"));
        return user;
    }

    private void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isDuplicateEmailViolation(SQLException e) {
        return e != null && ("23000".equals(e.getSQLState()) || e.getErrorCode() == 1062);
    }

    private boolean refreshUnverifiedUser(UserBean user, String token, int expiryMinutes) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isUpdated = false;

        String sql = "UPDATE `USER` SET name = ?, password = ?, role = ?, is_verified = 0, "
                + "verification_token = ?, verification_expiry = DATE_ADD(NOW(), INTERVAL ? MINUTE) "
                + "WHERE email = ? AND is_verified = 0 AND password != '*'";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getRole());
            preparedStatement.setString(4, token);
            preparedStatement.setInt(5, expiryMinutes);
            preparedStatement.setString(6, user.getEmail());
            isUpdated = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error while refreshing unverified user.", e);
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isUpdated;
    }
}
