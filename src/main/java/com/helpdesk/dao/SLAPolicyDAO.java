package com.helpdesk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.helpdesk.bean.SLAPolicyBean;

public class SLAPolicyDAO {

    public SLAPolicyBean getSLAByPriority(String priority) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        SLAPolicyBean policy = null;

        String sql = "SELECT policy_id, priority, response_time_hours, resolution_time_hours "
                + "FROM `SLA_POLICY` WHERE priority = ?";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, priority);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                policy = mapPolicy(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return policy;
    }

    public boolean updateSLAPolicy(SLAPolicyBean policy) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isUpdated = false;

        String sql = "UPDATE `SLA_POLICY` SET response_time_hours = ?, resolution_time_hours = ? "
                + "WHERE priority = ?";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, policy.getResponseTimeHours());
            preparedStatement.setInt(2, policy.getResolutionTimeHours());
            preparedStatement.setString(3, policy.getPriority());
            isUpdated = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isUpdated;
    }

    public List<SLAPolicyBean> getAllPolicies() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<SLAPolicyBean> policies = new ArrayList<SLAPolicyBean>();

        String sql = "SELECT policy_id, priority, response_time_hours, resolution_time_hours "
                + "FROM `SLA_POLICY` ORDER BY policy_id";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                policies.add(mapPolicy(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return policies;
    }

    private SLAPolicyBean mapPolicy(ResultSet resultSet) throws SQLException {
        SLAPolicyBean policy = new SLAPolicyBean();
        policy.setPolicyId(resultSet.getInt("policy_id"));
        policy.setPriority(resultSet.getString("priority"));
        policy.setResponseTimeHours(resultSet.getInt("response_time_hours"));
        policy.setResolutionTimeHours(resultSet.getInt("resolution_time_hours"));
        return policy;
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
}
