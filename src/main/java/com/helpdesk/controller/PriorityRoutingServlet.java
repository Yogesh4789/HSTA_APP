package com.helpdesk.controller;

import java.io.IOException;

import com.helpdesk.bean.TicketBean;
import com.helpdesk.bean.UserBean;
import com.helpdesk.service.PriorityRoutingService;
import com.helpdesk.service.TicketService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PriorityRoutingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final PriorityRoutingService routingService = new PriorityRoutingService();
    private final TicketService ticketService = new TicketService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserBean loggedUser = getLoggedUser(request);
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Please+login+first");
            return;
        }
        if (!"ADMIN".equals(loggedUser.getRole()) && !"AGENT".equals(loggedUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Unauthorized+access");
            return;
        }

        int ticketId = parseInt(request.getParameter("ticketId"));
        String category = request.getParameter("category");
        if (ticketId <= 0 || isBlank(category)) {
            response.sendRedirect(request.getContextPath() + "/ticket?action=view");
            return;
        }

        try {
            TicketBean ticket = ticketService.getTicketById(ticketId);
            if (ticket == null) {
                response.sendRedirect(request.getContextPath() + "/ticket?action=view");
                return;
            }

            String priority = routingService.assignPriority(category);
            int agentId = routingService.findAvailableAgentId();
            ticketService.updateTicketPriority(ticketId, priority);

            if (agentId > 0) {
                ticketService.assignTicket(ticketId, agentId);
                request.getSession().setAttribute("routeMessage",
                        "Ticket routed with priority " + priority + " and assigned to agent #" + agentId);
            } else {
                request.getSession().setAttribute("routeMessage",
                        "Ticket routed with priority " + priority + " but no agent available.");
            }

            response.sendRedirect(request.getContextPath() + "/ticket?action=detail&ticketId=" + ticketId);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to route ticket right now.");
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
