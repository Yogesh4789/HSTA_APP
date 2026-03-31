package com.helpdesk.controller;

import java.io.IOException;
import java.util.List;

import com.helpdesk.bean.SLAPolicyBean;
import com.helpdesk.bean.UserBean;
import com.helpdesk.service.AdminService;
import com.helpdesk.service.SLAService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SLAServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final SLAService slaService = new SLAService();
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

        try {
            List<SLAPolicyBean> policies = slaService.getAllPolicies();
            request.setAttribute("slaPolicies", policies);
            request.setAttribute("tickets", adminService.getAllTickets());
            request.setAttribute("agents", adminService.getAllAgents());
            request.setAttribute("activeSection", "sla");
            request.getRequestDispatcher("adminDashboard.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to load SLA policies.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
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
        if (!"update".equalsIgnoreCase(action)) {
            response.sendRedirect(request.getContextPath() + "/sla");
            return;
        }

        String priority = request.getParameter("priority");
        int responseHours = parseInt(request.getParameter("responseTimeHours"));
        int resolutionHours = parseInt(request.getParameter("resolutionTimeHours"));

        if (isBlank(priority) || responseHours <= 0 || resolutionHours <= 0) {
            request.setAttribute("errorMessage", "Invalid SLA input values.");
            request.setAttribute("tickets", adminService.getAllTickets());
            request.setAttribute("agents", adminService.getAllAgents());
            request.setAttribute("slaPolicies", slaService.getAllPolicies());
            request.setAttribute("activeSection", "sla");
            request.getRequestDispatcher("adminDashboard.jsp").forward(request, response);
            return;
        }

        try {
            slaService.updatePolicy(priority.trim(), responseHours, resolutionHours);
            response.sendRedirect(request.getContextPath() + "/sla?section=sla");
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to update SLA policy.");
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
