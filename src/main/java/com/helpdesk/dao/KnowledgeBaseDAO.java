package com.helpdesk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.helpdesk.bean.KnowledgeBaseBean;

public class KnowledgeBaseDAO {

    public boolean addArticle(KnowledgeBaseBean article) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isAdded = false;

        String sql = "INSERT INTO `KNOWLEDGE_BASE` (title, content, category, created_by) VALUES (?, ?, ?, ?)";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, article.getTitle());
            preparedStatement.setString(2, article.getContent());
            preparedStatement.setString(3, article.getCategory());
            preparedStatement.setInt(4, article.getCreatedBy());
            isAdded = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isAdded;
    }

    public List<KnowledgeBaseBean> searchArticles(String keyword) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<KnowledgeBaseBean> articles = new ArrayList<KnowledgeBaseBean>();

        String sql = "SELECT k.article_id, k.title, k.content, k.category, k.created_by, k.created_at, u.name as created_by_name "
                + "FROM `KNOWLEDGE_BASE` k LEFT JOIN `USER` u ON k.created_by = u.user_id "
                + "WHERE k.title LIKE ? OR k.content LIKE ? OR k.category LIKE ? "
                + "ORDER BY k.created_at DESC";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            String searchToken = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            preparedStatement.setString(1, searchToken);
            preparedStatement.setString(2, searchToken);
            preparedStatement.setString(3, searchToken);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                articles.add(mapArticle(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return articles;
    }

    public List<KnowledgeBaseBean> getAllArticles() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<KnowledgeBaseBean> articles = new ArrayList<KnowledgeBaseBean>();

        String sql = "SELECT k.article_id, k.title, k.content, k.category, k.created_by, k.created_at, u.name as created_by_name "
                + "FROM `KNOWLEDGE_BASE` k LEFT JOIN `USER` u ON k.created_by = u.user_id "
                + "ORDER BY k.created_at DESC";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                articles.add(mapArticle(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(resultSet);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return articles;
    }

    public boolean deleteArticle(int articleId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isDeleted = false;

        String sql = "DELETE FROM `KNOWLEDGE_BASE` WHERE article_id = ?";

        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, articleId);
            isDeleted = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }

        return isDeleted;
    }

    private KnowledgeBaseBean mapArticle(ResultSet resultSet) throws SQLException {
        KnowledgeBaseBean article = new KnowledgeBaseBean();
        article.setArticleId(resultSet.getInt("article_id"));
        article.setTitle(resultSet.getString("title"));
        article.setContent(resultSet.getString("content"));
        article.setCategory(resultSet.getString("category"));
        article.setCreatedBy(resultSet.getInt("created_by"));
        article.setCreatedAt(resultSet.getTimestamp("created_at"));
        try {
            article.setCreatedByName(resultSet.getString("created_by_name"));
        } catch (SQLException e) {
            // Field might not exist in old queries
        }
        return article;
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
