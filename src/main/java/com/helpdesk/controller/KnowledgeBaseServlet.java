package com.helpdesk.controller;

import java.io.IOException;
import java.util.List;

import com.helpdesk.bean.KnowledgeBaseBean;
import com.helpdesk.bean.UserBean;
import com.helpdesk.service.KnowledgeBaseService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class KnowledgeBaseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final KnowledgeBaseService knowledgeBaseService = new KnowledgeBaseService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserBean loggedUser = getLoggedUser(request);
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Please+login+first");
            return;
        }

        String action = request.getParameter("action");
        if ("search".equalsIgnoreCase(action)) {
            searchArticles(request, response);
            return;
        }
        listArticles(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserBean loggedUser = getLoggedUser(request);
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Please+login+first");
            return;
        }

        String action = request.getParameter("action");
        if ("add".equalsIgnoreCase(action)) {
            addArticle(request, response, loggedUser);
            return;
        }
        if ("delete".equalsIgnoreCase(action)) {
            deleteArticle(request, response, loggedUser);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/kb?action=list");
    }

    private void searchArticles(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        try {
            List<KnowledgeBaseBean> articles = knowledgeBaseService.searchArticles(keyword);
            request.setAttribute("articles", articles);
            if (articles == null || articles.isEmpty()) {
                request.setAttribute("message", "No articles found. You can raise a support ticket.");
            }
            request.getRequestDispatcher("knowledgeBase.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to search knowledge base.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private void listArticles(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<KnowledgeBaseBean> articles = knowledgeBaseService.getAllArticles();
            request.setAttribute("articles", articles);
            request.getRequestDispatcher("knowledgeBase.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to load knowledge base.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private void addArticle(HttpServletRequest request, HttpServletResponse response, UserBean loggedUser)
            throws ServletException, IOException {
        if (!"AGENT".equals(loggedUser.getRole()) && !"ADMIN".equals(loggedUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Unauthorized+access");
            return;
        }

        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String category = request.getParameter("category");

        if (isBlank(title) || isBlank(content) || isBlank(category)) {
            request.setAttribute("errorMessage", "Title, content and category are required.");
            request.setAttribute("articles", knowledgeBaseService.getAllArticles());
            request.getRequestDispatcher("knowledgeBase.jsp").forward(request, response);
            return;
        }

        try {
            knowledgeBaseService.addArticle(title, content, category, loggedUser.getUserId());
            response.sendRedirect(request.getContextPath() + "/kb?action=list");
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to add article.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private void deleteArticle(HttpServletRequest request, HttpServletResponse response, UserBean loggedUser)
            throws ServletException, IOException {
        if (!"AGENT".equals(loggedUser.getRole()) && !"ADMIN".equals(loggedUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Unauthorized+access");
            return;
        }

        int articleId = parseInt(request.getParameter("articleId"));
        if (articleId <= 0) {
            response.sendRedirect(request.getContextPath() + "/kb?action=list");
            return;
        }

        try {
            knowledgeBaseService.deleteArticle(articleId);
            response.sendRedirect(request.getContextPath() + "/kb?action=list");
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to delete article.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private UserBean getLoggedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("loggedUser");
        if (user instanceof UserBean) {
            return (UserBean) user;
        }
        return null;
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
