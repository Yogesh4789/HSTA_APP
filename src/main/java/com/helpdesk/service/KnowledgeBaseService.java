package com.helpdesk.service;

import java.util.List;

import com.helpdesk.bean.KnowledgeBaseBean;
import com.helpdesk.dao.KnowledgeBaseDAO;

public class KnowledgeBaseService {

    private final KnowledgeBaseDAO knowledgeBaseDAO;

    public KnowledgeBaseService() {
        this.knowledgeBaseDAO = new KnowledgeBaseDAO();
    }

    public List<KnowledgeBaseBean> searchArticles(String keyword) {
        return knowledgeBaseDAO.searchArticles(keyword);
    }

    public List<KnowledgeBaseBean> getAllArticles() {
        return knowledgeBaseDAO.getAllArticles();
    }

    public boolean addArticle(String title, String content, String category, int createdBy) {
        if (isBlank(title) || isBlank(content) || isBlank(category) || createdBy <= 0) {
            return false;
        }

        KnowledgeBaseBean article = new KnowledgeBaseBean();
        article.setTitle(title.trim());
        article.setContent(content.trim());
        article.setCategory(category.trim());
        article.setCreatedBy(createdBy);

        return knowledgeBaseDAO.addArticle(article);
    }

    public boolean deleteArticle(int articleId) {
        if (articleId <= 0) {
            return false;
        }
        return knowledgeBaseDAO.deleteArticle(articleId);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
