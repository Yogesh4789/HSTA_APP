package com.helpdesk.controller;

import java.io.IOException;
import java.util.Map;

import com.helpdesk.bean.UserBean;
import com.helpdesk.service.AdminService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserBean loggedUser = getLoggedUser(request);
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Please+login+first");
            return;
        }
        if (!"ADMIN".equals(loggedUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Unauthorized+access");
            return;
        }

        String action = request.getParameter("action");
        if ("reports".equalsIgnoreCase(action)) {
            loadReports(request, response);
            return;
        }
        if ("users".equalsIgnoreCase(action)) {
            loadUsers(request, response);
            return;
        }
        loadDashboard(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserBean loggedUser = getLoggedUser(request);
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Please+login+first");
            return;
        }
        if (!"ADMIN".equals(loggedUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Unauthorized+access");
            return;
        }

        String action = request.getParameter("action");
        if ("assign".equalsIgnoreCase(action)) {
            assignTicket(request, response);
            return;
        }
        if ("updateRole".equalsIgnoreCase(action)) {
            updateUserRole(request, response, loggedUser);
            return;
        }
        if ("deleteUser".equalsIgnoreCase(action)) {
            deleteUser(request, response, loggedUser);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/admin?action=dashboard");
    }

    private void loadDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("tickets", adminService.getAllTickets());
            request.setAttribute("agents", adminService.getAllAgents());
            request.getRequestDispatcher("adminDashboard.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to load admin dashboard.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private void loadUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("allUsers", adminService.getAllUsers());
            request.setAttribute("tickets", adminService.getAllTickets());
            request.setAttribute("agents", adminService.getAllAgents());
            request.setAttribute("activeSection", "users");
            request.getRequestDispatcher("adminDashboard.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to load user list.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private void assignTicket(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        int ticketId = parseInt(request.getParameter("ticketId"));
        int agentId = parseInt(request.getParameter("agentId"));
        if (ticketId <= 0 || agentId <= 0) {
            response.sendRedirect(request.getContextPath() + "/admin?action=dashboard");
            return;
        }

        try {
            boolean assigned = adminService.assignTicketToAgent(ticketId, agentId);
            if (!assigned) {
                request.setAttribute("errorMessage", "Could not assign ticket. Selected user may not be an agent.");
                request.setAttribute("tickets", adminService.getAllTickets());
                request.setAttribute("agents", adminService.getAllAgents());
                request.getRequestDispatcher("adminDashboard.jsp").forward(request, response);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/admin?action=dashboard");
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to assign ticket.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private void updateUserRole(HttpServletRequest request, HttpServletResponse response, UserBean loggedUser)
            throws IOException, ServletException {
        int userId = parseInt(request.getParameter("userId"));
        String newRole = request.getParameter("newRole");

        if (userId <= 0 || isBlank(newRole)) {
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        // Prevent admin from changing their own role
        if (userId == loggedUser.getUserId()) {
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        try {
            adminService.updateUserRole(userId, newRole.trim());
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to update user role.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response, UserBean loggedUser)
            throws IOException, ServletException {
        int userId = parseInt(request.getParameter("userId"));
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        // Prevent admin from deleting themselves
        if (userId == loggedUser.getUserId()) {
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        try {
            adminService.deleteUser(userId);
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to delete user.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private void loadReports(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Map<String, Object> report = adminService.generateReports();

            request.setAttribute("totalTickets", report.get("totalTickets"));
            request.setAttribute("resolvedTickets", report.get("resolvedTickets"));
            request.setAttribute("slaBreachedTickets", report.get("slaBreachedTickets"));
            request.setAttribute("perAgentStats", report.get("perAgentStats"));
            request.setAttribute("tickets", report.get("tickets"));
            request.setAttribute("agents", report.get("agents"));
            request.setAttribute("activeSection", "reports");
            request.getRequestDispatcher("adminDashboard.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to generate reports.");
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
