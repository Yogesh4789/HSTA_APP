package com.helpdesk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.helpdesk.bean.TicketBean;

public class TicketDAO {

    public boolean createTicket(TicketBean ticket) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isCreated = false;

        String sql = "INSERT INTO `TICKET` "
                + "(title, description, category, priority, status, raised_by, assigned_to, sla_deadline) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, ticket.getTitle());
            preparedStatement.setString(2, ticket.getDescription());
            preparedStatement.setString(3, ticket.getCategory());
            preparedStatement.setString(4, ticket.getPriority());
            preparedStatement.setString(5, ticket.getStatus());
            preparedStatement.setInt(6, ticket.getRaisedBy());

            if (ticket.getAssignedTo() > 0) {
                preparedStatement.setInt(7, ticket.getAssignedTo());
            } else {
                preparedStatement.setNull(7, java.sql.Types.INTEGER);
            }

            preparedStatement.setTimestamp(8, new Timestamp(ticket.getSlaDeadline().getTime()));

            isCreated = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isCreated;
    }

    public TicketBean getTicketById(int ticketId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        TicketBean ticket = null;

        String sql = "SELECT t.ticket_id, t.title, t.description, t.category, t.priority, t.status, "
                + "t.raised_by, t.assigned_to, t.created_at, t.sla_deadline, t.resolved_at, "
                + "u1.name AS raised_by_name, u2.name AS assigned_to_name "
                + "FROM `TICKET` t "
                + "JOIN `USER` u1 ON t.raised_by = u1.user_id "
                + "LEFT JOIN `USER` u2 ON t.assigned_to = u2.user_id "
                + "WHERE t.ticket_id = ?";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, ticketId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                ticket = mapTicketWithNames(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return ticket;
    }

    public List<TicketBean> getTicketsByUser(int userId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<TicketBean> tickets = new ArrayList<TicketBean>();

        String sql = "SELECT t.ticket_id, t.title, t.description, t.category, t.priority, t.status, "
                + "t.raised_by, t.assigned_to, t.created_at, t.sla_deadline, t.resolved_at, "
                + "u1.name AS raised_by_name, u2.name AS assigned_to_name "
                + "FROM `TICKET` t "
                + "JOIN `USER` u1 ON t.raised_by = u1.user_id "
                + "LEFT JOIN `USER` u2 ON t.assigned_to = u2.user_id "
                + "WHERE t.raised_by = ? ORDER BY t.created_at DESC";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                tickets.add(mapTicketWithNames(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return tickets;
    }

    public List<TicketBean> getTicketsByAgent(int agentId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<TicketBean> tickets = new ArrayList<TicketBean>();

        String sql = "SELECT t.ticket_id, t.title, t.description, t.category, t.priority, t.status, "
                + "t.raised_by, t.assigned_to, t.created_at, t.sla_deadline, t.resolved_at, "
                + "u1.name AS raised_by_name, u2.name AS assigned_to_name "
                + "FROM `TICKET` t "
                + "JOIN `USER` u1 ON t.raised_by = u1.user_id "
                + "LEFT JOIN `USER` u2 ON t.assigned_to = u2.user_id "
                + "WHERE t.assigned_to = ? ORDER BY t.created_at DESC";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, agentId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                tickets.add(mapTicketWithNames(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return tickets;
    }

    public boolean updateTicketStatus(int ticketId, String status) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isUpdated = false;

        String sql = "UPDATE `TICKET` SET status = ?, "
                + "resolved_at = CASE WHEN ? = 'RESOLVED' THEN NOW() ELSE resolved_at END "
                + "WHERE ticket_id = ?";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, status);
            preparedStatement.setInt(3, ticketId);
            isUpdated = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isUpdated;
    }

    public boolean assignTicket(int ticketId, int agentId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isAssigned = false;

        String sql = "UPDATE `TICKET` SET assigned_to = ?, "
                + "status = CASE WHEN status = 'OPEN' THEN 'ASSIGNED' ELSE status END "
                + "WHERE ticket_id = ?";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, agentId);
            preparedStatement.setInt(2, ticketId);
            isAssigned = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isAssigned;
    }

    public boolean updateTicketPriority(int ticketId, String priority) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isUpdated = false;

        String sql = "UPDATE `TICKET` SET priority = ? WHERE ticket_id = ?";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, priority);
            preparedStatement.setInt(2, ticketId);
            isUpdated = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isUpdated;
    }

    public List<TicketBean> getAllTickets() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<TicketBean> tickets = new ArrayList<TicketBean>();

        String sql = "SELECT t.ticket_id, t.title, t.description, t.category, t.priority, t.status, "
                + "t.raised_by, t.assigned_to, t.created_at, t.sla_deadline, t.resolved_at, "
                + "u1.name AS raised_by_name, u2.name AS assigned_to_name "
                + "FROM `TICKET` t "
                + "JOIN `USER` u1 ON t.raised_by = u1.user_id "
                + "LEFT JOIN `USER` u2 ON t.assigned_to = u2.user_id "
                + "ORDER BY t.created_at DESC";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                tickets.add(mapTicketWithNames(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return tickets;
    }

    /**
     * Optimized duplicate check: queries DB directly for same user + title within N minutes.
     */
    public boolean hasRecentDuplicate(int userId, String title, int minutesAgo) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        boolean hasDuplicate = false;

        String sql = "SELECT COUNT(*) AS cnt FROM `TICKET` "
                + "WHERE raised_by = ? AND LOWER(TRIM(title)) = LOWER(TRIM(?)) "
                + "AND created_at >= DATE_SUB(NOW(), INTERVAL ? MINUTE)";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, title);
            preparedStatement.setInt(3, minutesAgo);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                hasDuplicate = resultSet.getInt("cnt") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return hasDuplicate;
    }

    private TicketBean mapTicketWithNames(ResultSet resultSet) throws SQLException {
        TicketBean ticket = mapTicket(resultSet);
        ticket.setRaisedByName(resultSet.getString("raised_by_name"));
        ticket.setAssignedToName(resultSet.getString("assigned_to_name"));
        return ticket;
    }

    private TicketBean mapTicket(ResultSet resultSet) throws SQLException {
        TicketBean ticket = new TicketBean();
        ticket.setTicketId(resultSet.getInt("ticket_id"));
        ticket.setTitle(resultSet.getString("title"));
        ticket.setDescription(resultSet.getString("description"));
        ticket.setCategory(resultSet.getString("category"));
        ticket.setPriority(resultSet.getString("priority"));
        ticket.setStatus(resultSet.getString("status"));
        ticket.setRaisedBy(resultSet.getInt("raised_by"));

        Integer assignedTo = (Integer) resultSet.getObject("assigned_to");
        ticket.setAssignedTo(assignedTo == null ? 0 : assignedTo.intValue());

        ticket.setCreatedAt(resultSet.getTimestamp("created_at"));
        ticket.setSlaDeadline(resultSet.getTimestamp("sla_deadline"));
        ticket.setResolvedAt(resultSet.getTimestamp("resolved_at"));
        return ticket;
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
