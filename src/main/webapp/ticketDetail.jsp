<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.helpdesk.bean.UserBean" %>
<%@ page import="com.helpdesk.bean.TicketBean" %>
<%@ page import="com.helpdesk.bean.CommentBean" %>
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
TicketBean ticket = (TicketBean) request.getAttribute("ticket");
List<CommentBean> comments = (List<CommentBean>) request.getAttribute("comments");
String errorMessage = (String) request.getAttribute("errorMessage");
String message = request.getParameter("message");
Boolean isSlaBreached = (Boolean) request.getAttribute("isSlaBreached");
if (isSlaBreached == null) {
    isSlaBreached = Boolean.FALSE;
}
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Ticket Details - HSTA</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body>
    <div class="container">
        <div class="card">
            <div class="topbar">
                <h2 class="title">Ticket Details</h2>
                <a class="btn btn-secondary" href="<%=request.getContextPath()%>/ticket?action=view">Back</a>
            </div>

            <% if (ticket == null) { %>
                <div class="alert alert-error">Ticket not found.</div>
            <% } else { %>
                <% if (errorMessage != null) { %>
                    <div class="alert alert-error"><%=errorMessage%></div>
                <% } %>
                <% if (message != null && !message.trim().isEmpty()) { %>
                    <div class="alert alert-info"><%=message%></div>
                <% } %>
                <% if (Boolean.TRUE.equals(isSlaBreached)) { %>
                    <div class="alert alert-error">SLA breached: this ticket has crossed its deadline.</div>
                <% } %>

                <div class="card">
                    <p><strong>ID:</strong> <%=ticket.getTicketId()%></p>
                    <p><strong>Title:</strong> <%=ticket.getTitle()%></p>
                    <p><strong>Description:</strong> <%=ticket.getDescription()%></p>
                    <p><strong>Category:</strong> <%=ticket.getCategory()%></p>
                    <p><strong>Priority:</strong>
                        <span class="badge <% if ("CRITICAL".equals(ticket.getPriority())) { %>badge-open<% } else if ("HIGH".equals(ticket.getPriority())) { %>badge-progress<% } else { %>badge-resolved<% } %>">
                            <%=ticket.getPriority()%>
                        </span>
                    </p>
                    <p><strong>Status:</strong>
                        <% if ("IN_PROGRESS".equals(ticket.getStatus()) || "ASSIGNED".equals(ticket.getStatus())) { %>
                            <span class="badge badge-progress"><%=ticket.getStatus()%></span>
                        <% } else if ("RESOLVED".equals(ticket.getStatus()) || "CLOSED".equals(ticket.getStatus())) { %>
                            <span class="badge badge-resolved"><%=ticket.getStatus()%></span>
                        <% } else { %>
                            <span class="badge badge-open"><%=ticket.getStatus()%></span>
                        <% } %>
                    </p>
                    <p><strong>Raised By:</strong> <%=ticket.getRaisedByName() != null ? ticket.getRaisedByName() : String.valueOf(ticket.getRaisedBy())%></p>
                    <p><strong>Assigned To:</strong>
                        <%
                            if (ticket.getAssignedTo() > 0) {
                                String assignedDisplayName = ticket.getAssignedToName();
                                if (assignedDisplayName != null) {
                                    assignedDisplayName = assignedDisplayName.trim();
                                    if (assignedDisplayName.toLowerCase().startsWith("agent ")) {
                                        assignedDisplayName = assignedDisplayName.substring(6).trim();
                                    }
                                }
                                if (assignedDisplayName != null && !assignedDisplayName.isEmpty()) {
                        %>
                            Agent <%=assignedDisplayName%>
                        <%
                                } else {
                        %>
                            Agent #<%=ticket.getAssignedTo()%>
                        <%
                                }
                            } else {
                        %>
                            Unassigned
                        <%
                            }
                        %>
                    </p>
                    <p><strong>Created At:</strong> <%=ticket.getCreatedAt()%></p>
                    <p><strong>SLA Deadline:</strong> <%=ticket.getSlaDeadline()%></p>
                    <% if (ticket.getResolvedAt() != null) { %>
                        <p><strong>Resolved At:</strong> <%=ticket.getResolvedAt()%></p>
                    <% } %>
                </div>

                <div class="card actions-inline">
                    <% if ("USER".equals(loggedUser.getRole()) || "ADMIN".equals(loggedUser.getRole())) { %>
                        <form action="<%=request.getContextPath()%>/ticket" method="post">
                            <input type="hidden" name="action" value="close">
                            <input type="hidden" name="ticketId" value="<%=ticket.getTicketId()%>">
                            <button class="btn btn-danger" type="submit">Close Ticket</button>
                        </form>
                    <% } %>

                    <% if ("USER".equals(loggedUser.getRole()) || "ADMIN".equals(loggedUser.getRole())) { %>
                        <form action="<%=request.getContextPath()%>/ticket" method="post">
                            <input type="hidden" name="action" value="reopen">
                            <input type="hidden" name="ticketId" value="<%=ticket.getTicketId()%>">
                            <button class="btn btn-secondary" type="submit">Reopen Ticket</button>
                        </form>
                    <% } %>

                    <% if ("AGENT".equals(loggedUser.getRole()) || "ADMIN".equals(loggedUser.getRole())) { %>
                        <form action="<%=request.getContextPath()%>/ticket" method="post">
                            <input type="hidden" name="action" value="updateStatus">
                            <input type="hidden" name="ticketId" value="<%=ticket.getTicketId()%>">
                            <select name="status" required>
                                <option value="">Change status</option>
                                <option value="IN_PROGRESS">IN_PROGRESS</option>
                                <option value="RESOLVED">RESOLVED</option>
                            </select>
                            <button class="btn btn-primary" type="submit">Update Status</button>
                        </form>
                    <% } %>
                </div>

                <div class="card">
                    <h3>Comments</h3>
                    <table>
                        <thead>
                            <tr>
                                <th>By</th>
                                <th>Comment</th>
                                <th>Time</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (comments == null || comments.isEmpty()) { %>
                                <tr><td colspan="3">No comments yet.</td></tr>
                            <% } else {
                                for (CommentBean c : comments) { %>
                                <tr>
                                    <td><%=c.getCommentedByName() != null ? c.getCommentedByName() : String.valueOf(c.getCommentedBy())%></td>
                                    <td><%=c.getCommentText()%></td>
                                    <td><%=c.getCommentedAt()%></td>
                                </tr>
                            <% } } %>
                        </tbody>
                    </table>
                </div>

                <% if (!"CLOSED".equals(ticket.getStatus())) { %>
                <div class="card">
                    <h3>Add Comment</h3>
                    <form action="<%=request.getContextPath()%>/comment" method="post">
                        <input type="hidden" name="action" value="add">
                        <input type="hidden" name="ticketId" value="<%=ticket.getTicketId()%>">
                        <textarea name="commentText" required></textarea>
                        <button class="btn btn-primary" type="submit">Add Comment</button>
                    </form>
                </div>
                <% } %>
            <% } %>
        </div>
    </div>
    <script>
        (function () {
            document.addEventListener("DOMContentLoaded", function () {
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
