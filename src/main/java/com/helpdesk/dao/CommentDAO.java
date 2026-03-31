package com.helpdesk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.helpdesk.bean.CommentBean;

public class CommentDAO {

    public boolean addComment(CommentBean comment) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isAdded = false;

        String sql = "INSERT INTO `COMMENT` (ticket_id, commented_by, comment_text, commented_at) "
                + "VALUES (?, ?, ?, ?)";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, comment.getTicketId());
            preparedStatement.setInt(2, comment.getCommentedBy());
            preparedStatement.setString(3, comment.getCommentText());

            if (comment.getCommentedAt() != null) {
                preparedStatement.setTimestamp(4, new Timestamp(comment.getCommentedAt().getTime()));
            } else {
                preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            }

            isAdded = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isAdded;
    }

    public List<CommentBean> getCommentsByTicket(int ticketId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<CommentBean> comments = new ArrayList<CommentBean>();

        String sql = "SELECT c.comment_id, c.ticket_id, c.commented_by, c.comment_text, c.commented_at, "
                + "u.name AS commented_by_name "
                + "FROM `COMMENT` c "
                + "JOIN `USER` u ON c.commented_by = u.user_id "
                + "WHERE c.ticket_id = ? ORDER BY c.commented_at ASC";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, ticketId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                CommentBean comment = mapComment(resultSet);
                comment.setCommentedByName(resultSet.getString("commented_by_name"));
                comments.add(comment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return comments;
    }

    private CommentBean mapComment(ResultSet resultSet) throws SQLException {
        CommentBean comment = new CommentBean();
        comment.setCommentId(resultSet.getInt("comment_id"));
        comment.setTicketId(resultSet.getInt("ticket_id"));
        comment.setCommentedBy(resultSet.getInt("commented_by"));
        comment.setCommentText(resultSet.getString("comment_text"));
        comment.setCommentedAt(resultSet.getTimestamp("commented_at"));
        return comment;
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
