<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.helpdesk.bean.UserBean" %>
<%@ page import="com.helpdesk.bean.TicketBean" %>
<%@ page import="com.helpdesk.bean.SLAPolicyBean" %>
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
if (!"ADMIN".equals(loggedUser.getRole())) {
    response.sendRedirect(request.getContextPath() + "/login.jsp?message=Unauthorized+access");
    return;
}
List<TicketBean> tickets = (List<TicketBean>) request.getAttribute("tickets");
List<UserBean> agents = (List<UserBean>) request.getAttribute("agents");
List<SLAPolicyBean> slaPolicies = (List<SLAPolicyBean>) request.getAttribute("slaPolicies");
List<UserBean> allUsers = (List<UserBean>) request.getAttribute("allUsers");
Integer totalTickets = (Integer) request.getAttribute("totalTickets");
Integer resolvedTickets = (Integer) request.getAttribute("resolvedTickets");
Integer slaBreachedTickets = (Integer) request.getAttribute("slaBreachedTickets");
Map<String, Integer> perAgentStats = (Map<String, Integer>) request.getAttribute("perAgentStats");
String activeSection = (String) request.getAttribute("activeSection");
String errorMessage = (String) request.getAttribute("errorMessage");
if (activeSection == null || activeSection.trim().isEmpty()) {
    activeSection = request.getParameter("section");
}
if (activeSection == null || activeSection.trim().isEmpty()) {
    activeSection = "dashboard";
}
String section = activeSection.trim().toLowerCase();
boolean showTicketSection = "dashboard".equals(section);
boolean showUsersSection = "users".equals(section);
boolean showSlaSection = "sla".equals(section);
boolean showReportsSection = "reports".equals(section);
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Admin Dashboard - HSTA</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body>
    <div class="container">
        <div class="card">
            <div class="topbar">
                <h2 class="title">Admin Dashboard</h2>
                <div>
                    <a class="btn btn-secondary" href="<%=request.getContextPath()%>/dashboard.jsp">Back</a>
                    <a class="btn btn-secondary" href="<%=request.getContextPath()%>/login?action=logout">Logout</a>
                </div>
            </div>

            <% if (errorMessage != null) { %>
                <div class="alert alert-error"><%=errorMessage%></div>
            <% } %>

            <a class="btn btn-primary" href="<%=request.getContextPath()%>/admin?action=dashboard">Ticket Assignment</a>
            <a class="btn btn-primary" href="<%=request.getContextPath()%>/admin?action=users">Manage Users</a>
            <a class="btn btn-primary" href="<%=request.getContextPath()%>/sla?section=sla">SLA Policies</a>
            <a class="btn btn-primary" href="<%=request.getContextPath()%>/admin?action=reports&section=reports">Reports</a>
        </div>

        <% if (showTicketSection) { %>
        <div class="card" id="ticketAssignmentSection">
            <h3>Ticket Assignment</h3>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Title</th>
                        <th>Priority</th>
                        <th>Status</th>
                        <th>Raised By</th>
                        <th>Assigned To</th>
                        <th>Assign/Reassign</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (tickets == null || tickets.isEmpty()) { %>
                        <tr><td colspan="7">No tickets found.</td></tr>
                    <% } else {
                        for (TicketBean t : tickets) { %>
                        <tr>
                            <td><%=t.getTicketId()%></td>
                            <td><a class="admin-ticket-title" href="<%=request.getContextPath()%>/ticket?action=detail&ticketId=<%=t.getTicketId()%>"><%=t.getTitle()%></a></td>
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
                            <td><%=t.getRaisedByName() != null ? t.getRaisedByName() : String.valueOf(t.getRaisedBy())%></td>
                            <td>
                                <% if (t.getAssignedTo() > 0) { %>
                                    <%
                                        String assignedDisplayName = t.getAssignedToName();
                                        if (assignedDisplayName != null) {
                                            assignedDisplayName = assignedDisplayName.trim();
                                            if (assignedDisplayName.toLowerCase().startsWith("agent ")) {
                                                assignedDisplayName = assignedDisplayName.substring(6).trim();
                                            }
                                        }
                                    %>
                                    <%= (assignedDisplayName != null && !assignedDisplayName.isEmpty())
                                            ? ("Agent " + assignedDisplayName)
                                            : ("Agent #" + t.getAssignedTo()) %>
                                <% } else { %>
                                    Unassigned
                                <% } %>
                            </td>
                            <td>
                                <form action="<%=request.getContextPath()%>/admin" method="post">
                                    <input type="hidden" name="action" value="assign">
                                    <input type="hidden" name="ticketId" value="<%=t.getTicketId()%>">
                                    <select name="agentId" required>
                                        <option value="">Select agent</option>
                                        <% if (agents != null) {
                                            for (UserBean a : agents) { %>
                                            <%
                                                String optionAgentName = a.getName() == null ? "" : a.getName().trim();
                                                if (optionAgentName.toLowerCase().startsWith("agent ")) {
                                                    optionAgentName = optionAgentName.substring(6).trim();
                                                }
                                            %>
                                            <option value="<%=a.getUserId()%>">Agent <%=optionAgentName%></option>
                                        <% } } %>
                                    </select>
                                    <button class="btn btn-primary" type="submit">Assign</button>
                                </form>
                            </td>
                        </tr>
                    <% } } %>
                </tbody>
            </table>
        </div>
        <% } %>

        <% if (showUsersSection) { %>
            <div class="card" id="usersSection">
                <h3>User Management</h3>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>Created</th>
                            <th>Change Role</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (allUsers == null || allUsers.isEmpty()) { %>
                            <tr><td colspan="7">No users found.</td></tr>
                        <% } else {
                            for (UserBean u : allUsers) { %>
                            <%
                                String displayUserName = u.getName() == null ? "" : u.getName().trim();
                                if ("USER".equals(u.getRole()) && displayUserName.toLowerCase().endsWith(" user")) {
                                    displayUserName = displayUserName.substring(0, displayUserName.length() - 5).trim();
                                }
                                if ("AGENT".equals(u.getRole())) {
                                    if (displayUserName.toLowerCase().startsWith("agent ")) {
                                        displayUserName = displayUserName.substring(6).trim();
                                    }
                                    displayUserName = "Agent " + displayUserName;
                                }
                            %>
                            <tr>
                                <td><%=u.getUserId()%></td>
                                <td><%=displayUserName%></td>
                                <td><%=u.getEmail()%></td>
                                <td>
                                    <span class="badge <% if ("ADMIN".equals(u.getRole())) { %>badge-open<% } else if ("AGENT".equals(u.getRole())) { %>badge-progress<% } else { %>badge-resolved<% } %>">
                                        <%=u.getRole()%>
                                    </span>
                                </td>
                                <td><%=u.getCreatedAt()%></td>
                                <td>
                                    <% if (u.getUserId() != loggedUser.getUserId()) { %>
                                    <form action="<%=request.getContextPath()%>/admin" method="post" style="display:inline;">
                                        <input type="hidden" name="action" value="updateRole">
                                        <input type="hidden" name="userId" value="<%=u.getUserId()%>">
                                        <select name="newRole" required>
                                            <option value="">Select role</option>
                                            <option value="USER" <%="USER".equals(u.getRole()) ? "selected" : ""%>>USER</option>
                                            <option value="AGENT" <%="AGENT".equals(u.getRole()) ? "selected" : ""%>>AGENT</option>
                                            <option value="ADMIN" <%="ADMIN".equals(u.getRole()) ? "selected" : ""%>>ADMIN</option>
                                        </select>
                                        <button class="btn btn-primary" type="submit">Update</button>
                                    </form>
                                    <% } else { %>
                                        <span class="small">(Current Admin)</span>
                                    <% } %>
                                </td>
                                <td>
                                    <% if (u.getUserId() != loggedUser.getUserId()) { %>
                                    <form action="<%=request.getContextPath()%>/admin" method="post" style="display:inline;">
                                        <input type="hidden" name="action" value="deleteUser">
                                        <input type="hidden" name="userId" value="<%=u.getUserId()%>">
                                        <button class="btn btn-danger" type="submit" onclick="return confirm('Delete user <%=u.getName()%>?');">Delete</button>
                                    </form>
                                    <% } %>
                                </td>
                            </tr>
                        <% } } %>
                    </tbody>
                </table>
            </div>
        <% } %>

        <% if (showSlaSection) { %>
            <div class="card" id="slaSection">
                <h3>SLA Policies</h3>
                <table>
                    <thead>
                        <tr>
                            <th>Priority</th>
                            <th>Response Hours</th>
                            <th>Resolution Hours</th>
                            <th>Update</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (slaPolicies == null || slaPolicies.isEmpty()) { %>
                            <tr><td colspan="4">No SLA policies found.</td></tr>
                        <% } else {
                            for (SLAPolicyBean p : slaPolicies) { %>
                            <tr>
                                <td><%=p.getPriority()%></td>
                                <td><%=p.getResponseTimeHours()%></td>
                                <td><%=p.getResolutionTimeHours()%></td>
                                <td>
                                    <form action="<%=request.getContextPath()%>/sla" method="post">
                                        <input type="hidden" name="action" value="update">
                                        <input type="hidden" name="priority" value="<%=p.getPriority()%>">
                                        <input type="number" name="responseTimeHours" value="<%=p.getResponseTimeHours()%>" min="1" required>
                                        <input type="number" name="resolutionTimeHours" value="<%=p.getResolutionTimeHours()%>" min="1" required>
                                        <button class="btn btn-primary" type="submit">Save</button>
                                    </form>
                                </td>
                            </tr>
                        <% } } %>
                    </tbody>
                </table>
            </div>
        <% } %>

        <% if (showReportsSection) { %>
            <div class="card" id="reportsSection">
                <h3>Reports</h3>
                <div class="grid-2">
                    <div class="card">
                        <h4>Total Tickets</h4>
                        <p style="font-size:28px; font-weight:800; color:#0f766e;"><%= totalTickets == null ? 0 : totalTickets.intValue() %></p>
                    </div>
                    <div class="card">
                        <h4>Resolved / Closed</h4>
                        <p style="font-size:28px; font-weight:800; color:#027a48;"><%= resolvedTickets == null ? 0 : resolvedTickets.intValue() %></p>
                    </div>
                    <div class="card">
                        <h4>SLA Breaches</h4>
                        <p style="font-size:28px; font-weight:800; color:#b42318;"><%= slaBreachedTickets == null ? 0 : slaBreachedTickets.intValue() %></p>
                    </div>
                    <div class="card">
                        <h4>Open Rate</h4>
                        <p style="font-size:28px; font-weight:800; color:#b54708;">
                            <%
                            int total = totalTickets == null ? 0 : totalTickets.intValue();
                            int resolved = resolvedTickets == null ? 0 : resolvedTickets.intValue();
                            int open = total - resolved;
                            String rate = total > 0 ? String.valueOf(Math.round((open * 100.0) / total)) + "%" : "0%";
                            %>
                            <%=rate%>
                        </p>
                    </div>
                </div>

                <h4 style="margin-top:16px;">Per-Agent Ticket Count</h4>
                <table>
                    <thead><tr><th>Agent</th><th>Assigned Tickets</th></tr></thead>
                    <tbody>
                        <% if (perAgentStats == null || perAgentStats.isEmpty()) { %>
                            <tr><td colspan="2">No report data available.</td></tr>
                        <% } else {
                            for (Map.Entry<String, Integer> e : perAgentStats.entrySet()) { %>
                            <%
                                String reportAgentName = e.getKey() == null ? "" : e.getKey().trim();
                                if (reportAgentName.toLowerCase().startsWith("agent ")) {
                                    reportAgentName = reportAgentName.substring(6).trim();
                                }
                                if (reportAgentName.isEmpty()) {
                                    reportAgentName = "Unknown";
                                }
                            %>
                            <tr>
                                <td>Agent <%=reportAgentName%></td>
                                <td><%=e.getValue()%></td>
                            </tr>
                        <% } } %>
                    </tbody>
                </table>
            </div>
        <% } %>
    </div>
    <% if ("sla".equalsIgnoreCase(section) || "reports".equalsIgnoreCase(section) || "users".equalsIgnoreCase(section)) { %>
    <script>
        window.addEventListener("load", function () {
            var targetId = "<%= "reports".equalsIgnoreCase(section) ? "reportsSection" : ("users".equalsIgnoreCase(section) ? "usersSection" : "slaSection") %>";
            var section = document.getElementById(targetId);
            if (section) {
                section.scrollIntoView({ behavior: "smooth", block: "start" });
            }
        });
    </script>
    <% } %>
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
