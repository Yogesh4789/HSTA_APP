<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.helpdesk.bean.UserBean" %>
<%@ page import="com.helpdesk.bean.TicketBean" %>
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
List<TicketBean> tickets = (List<TicketBean>) request.getAttribute("tickets");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>View Tickets - HSTA</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body>
    <div class="container">
        <div class="card">
            <div class="topbar">
                <h2 class="title">
                    <% if ("AGENT".equals(loggedUser.getRole())) { %>
                        My Assigned Tickets
                    <% } else if ("ADMIN".equals(loggedUser.getRole())) { %>
                        All Tickets
                    <% } else { %>
                        My Tickets
                    <% } %>
                </h2>
                <div>
                    <% if ("USER".equals(loggedUser.getRole()) || "ADMIN".equals(loggedUser.getRole())) { %>
                        <a class="btn btn-primary" href="<%=request.getContextPath()%>/raiseTicket.jsp">New Ticket</a>
                    <% } %>
                    <a class="btn btn-secondary" href="<%=request.getContextPath()%>/dashboard.jsp">Back</a>
                </div>
            </div>

            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Title</th>
                        <th>Category</th>
                        <th>Priority</th>
                        <th>Status</th>
                        <th>Assigned To</th>
                        <th>SLA Deadline</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (tickets == null || tickets.isEmpty()) { %>
                        <tr>
                            <td colspan="8">No tickets found.</td>
                        </tr>
                    <% } else {
                        for (TicketBean t : tickets) { %>
                        <tr>
                            <td><%=t.getTicketId()%></td>
                            <td><%=t.getTitle()%></td>
                            <td><%=t.getCategory()%></td>
                            <td>
                                <span class="badge <% if ("CRITICAL".equals(t.getPriority())) { %>badge-open<% } else if ("HIGH".equals(t.getPriority())) { %>badge-progress<% } else { %>badge-resolved<% } %>">
                                    <%=t.getPriority()%>
                                </span>
                            </td>
                            <td>
                                <% if ("IN_PROGRESS".equals(t.getStatus()) || "ASSIGNED".equals(t.getStatus())) { %>
                                    <span class="badge badge-progress"><%=t.getStatus()%></span>
                                <% } else if ("RESOLVED".equals(t.getStatus()) || "CLOSED".equals(t.getStatus())) { %>
                                    <span class="badge badge-resolved"><%=t.getStatus()%></span>
                                <% } else { %>
                                    <span class="badge badge-open"><%=t.getStatus()%></span>
                                <% } %>
                            </td>
                            <td>
                                <%
                                    if (t.getAssignedTo() > 0) {
                                        String assignedDisplayName = t.getAssignedToName();
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
                                    Agent #<%=t.getAssignedTo()%>
                                <%
                                        }
                                    } else {
                                %>
                                    Unassigned
                                <%
                                    }
                                %>
                            </td>
                            <td><%=t.getSlaDeadline()%></td>
                            <td>
                                <a class="btn btn-primary" href="<%=request.getContextPath()%>/ticket?action=detail&ticketId=<%=t.getTicketId()%>">Details</a>
                            </td>
                        </tr>
                    <% } } %>
                </tbody>
            </table>
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
