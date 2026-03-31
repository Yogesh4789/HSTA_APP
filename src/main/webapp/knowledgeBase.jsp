<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.helpdesk.bean.UserBean" %>
<%@ page import="com.helpdesk.bean.KnowledgeBaseBean" %>
<%
Object loggedUserObj = session.getAttribute("loggedUser");
UserBean loggedUser = (loggedUserObj instanceof UserBean) ? (UserBean) loggedUserObj : null;
if (loggedUser == null) {
    if (loggedUserObj != null) {
        session.removeAttribute("loggedUser");
    }
    response.sendRedirect(request.getContextPath() + "/login.jsp?message=Please+login+first");
    return;
}
List<KnowledgeBaseBean> articles = (List<KnowledgeBaseBean>) request.getAttribute("articles");
String message = (String) request.getAttribute("message");
String errorMessage = (String) request.getAttribute("errorMessage");
String action = request.getParameter("action");
boolean shouldAutoScroll = "search".equalsIgnoreCase(action);
String keyword = request.getParameter("keyword");
if (keyword == null) {
    keyword = "";
}
String keywordEscaped = keyword.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Knowledge Base - HSTA</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body>
    <div class="container">
        <div class="card">
            <div class="topbar">
                <h2 class="title">Knowledge Base</h2>
                <a class="btn btn-secondary" href="<%=request.getContextPath()%>/dashboard.jsp">Back</a>
            </div>

            <% if (errorMessage != null) { %>
                <div class="alert alert-error"><%=errorMessage%></div>
            <% } %>
            <% if (message != null) { %>
                <div class="alert alert-info"><%=message%></div>
            <% } %>

            <form action="<%=request.getContextPath()%>/kb" method="get">
                <input type="hidden" name="action" value="search">
                <label>Search</label>
                <input type="text" name="keyword" value="<%=keywordEscaped%>" placeholder="Search by title, content, or category">
                <button class="btn btn-primary" type="submit">Search</button>
                <a class="btn btn-secondary" href="<%=request.getContextPath()%>/kb?action=list">Reset</a>
            </form>
        </div>

        <% if ("AGENT".equals(loggedUser.getRole()) || "ADMIN".equals(loggedUser.getRole())) { %>
        <div class="card">
            <h3>Add Article</h3>
            <form action="<%=request.getContextPath()%>/kb" method="post">
                <input type="hidden" name="action" value="add">
                <label>Title</label>
                <input type="text" name="title" required>
                <label>Category</label>
                <input type="text" name="category" required>
                <label>Content</label>
                <textarea name="content" required></textarea>
                <button class="btn btn-primary" type="submit">Add Article</button>
            </form>
        </div>
        <% } %>

        <div class="card" id="articlesSection">
            <h3>Articles</h3>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Title</th>
                        <th>Category</th>
                        <th>Created By</th>
                        <th>Content</th>
                        <% if ("AGENT".equals(loggedUser.getRole()) || "ADMIN".equals(loggedUser.getRole())) { %>
                        <th>Action</th>
                        <% } %>
                    </tr>
                </thead>
                <tbody>
                    <% if (articles == null || articles.isEmpty()) { %>
                        <tr><td colspan="6">No articles available.</td></tr>
                    <% } else {
                        for (KnowledgeBaseBean kb : articles) { %>
                        <tr>
                            <td><%=kb.getArticleId()%></td>
                            <td><%=kb.getTitle()%></td>
                            <td><%=kb.getCategory()%></td>
                            <%
                                String kbAgentName = kb.getCreatedByName();
                                if (kbAgentName != null) {
                                    kbAgentName = kbAgentName.trim();
                                    if (kbAgentName.toLowerCase().startsWith("agent ")) {
                                        kbAgentName = kbAgentName.substring(6).trim();
                                    }
                                }
                                String displayName = (kbAgentName != null && !kbAgentName.isEmpty())
                                        ? ("Agent " + kbAgentName)
                                        : ("Agent #" + kb.getCreatedBy());
                            %>
                            <td><%=displayName%></td>
                            <td><%=kb.getContent()%></td>
                            <% if ("AGENT".equals(loggedUser.getRole()) || "ADMIN".equals(loggedUser.getRole())) { %>
                            <td>
                                <form action="<%=request.getContextPath()%>/kb" method="post">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="articleId" value="<%=kb.getArticleId()%>">
                                    <button class="btn btn-danger" type="submit">Delete</button>
                                </form>
                            </td>
                            <% } %>
                        </tr>
                    <% } } %>
                </tbody>
            </table>
        </div>
    </div>
    <% if (shouldAutoScroll) { %>
    <script>
        window.addEventListener("load", function () {
            var section = document.getElementById("articlesSection");
            if (section) {
                section.scrollIntoView({ behavior: "smooth", block: "start" });
            }
        });
    </script>
    <% } %>
    <script>
        (function () {
            document.addEventListener("DOMContentLoaded", function () {
                var navbar = document.querySelector(".navbar, .home-header");
                if (navbar) {
                    var updateNavbarState = function () {
                        navbar.classList.toggle("scrolled", window.scrollY > 50);
                    };
                    updateNavbarState();
                    window.addEventListener("scroll", updateNavbarState);
                }

                var heroHeading = document.querySelector(".home-hero h1, .card .title, h1");
                if (heroHeading) {
                    heroHeading.classList.add("hero-text");
                }

                var revealTargets = document.querySelectorAll(".card, table, form");
                revealTargets.forEach(function (el) {
                    el.classList.add("hidden");
                });

                if ("IntersectionObserver" in window) {
                    var observer = new IntersectionObserver(function (entries) {
                        entries.forEach(function (entry) {
                            if (entry.isIntersecting) {
                                entry.target.classList.add("show");
                                observer.unobserve(entry.target);
                            }
                        });
                    }, { threshold: 0.12 });

                    revealTargets.forEach(function (el) {
                        observer.observe(el);
                    });
                } else {
                    revealTargets.forEach(function (el) {
                        el.classList.add("show");
                    });
                }
            });
        })();
    </script>
</body>
</html>
