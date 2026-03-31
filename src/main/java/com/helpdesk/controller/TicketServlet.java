package com.helpdesk.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import com.helpdesk.bean.CommentBean;
import com.helpdesk.bean.TicketBean;
import com.helpdesk.bean.UserBean;
import com.helpdesk.service.CommentService;
import com.helpdesk.service.TicketService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class TicketServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final TicketService ticketService = new TicketService();
    private final CommentService commentService = new CommentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserBean loggedUser = getLoggedUser(request);
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Please+login+first");
            return;
        }

        String action = request.getParameter("action");
        if ("detail".equalsIgnoreCase(action)) {
            handleTicketDetail(request, response, loggedUser);
            return;
        }
        handleTicketList(request, response, loggedUser);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserBean loggedUser = getLoggedUser(request);
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Please+login+first");
            return;
        }

        String action = request.getParameter("action");
        if ("create".equalsIgnoreCase(action)) {
            createTicket(request, response, loggedUser);
            return;
        }
        if ("close".equalsIgnoreCase(action)) {
            closeTicket(request, response, loggedUser);
            return;
        }
        if ("reopen".equalsIgnoreCase(action)) {
            reopenTicket(request, response, loggedUser);
            return;
        }
        if ("updateStatus".equalsIgnoreCase(action)) {
            updateStatus(request, response, loggedUser);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/ticket?action=view");
    }

    private void handleTicketList(HttpServletRequest request, HttpServletResponse response, UserBean loggedUser)
            throws ServletException, IOException {
        try {
            List<TicketBean> tickets;
            if ("ADMIN".equals(loggedUser.getRole())) {
                tickets = ticketService.getAllTickets();
            } else if ("AGENT".equals(loggedUser.getRole())) {
                tickets = ticketService.getTicketsByAgent(loggedUser.getUserId());
            } else {
                tickets = ticketService.getTicketsByUser(loggedUser.getUserId());
            }

            request.setAttribute("tickets", tickets);
            request.getRequestDispatcher("viewTickets.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to load tickets now. Please try again.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private void handleTicketDetail(HttpServletRequest request, HttpServletResponse response, UserBean loggedUser)
            throws ServletException, IOException {
        int ticketId = parseInt(request.getParameter("ticketId"));
        if (ticketId <= 0) {
            response.sendRedirect(request.getContextPath() + "/ticket?action=view");
            return;
        }

        try {
            TicketBean ticket = ticketService.getTicketById(ticketId);
            if (ticket == null) {
                response.sendRedirect(request.getContextPath() + "/ticket?action=view");
                return;
            }

            if (!canViewTicket(loggedUser, ticket)) {
                response.sendRedirect(request.getContextPath() + "/login.jsp?message=Unauthorized+access");
                return;
            }

            List<CommentBean> comments = commentService.getCommentsByTicket(ticketId);
            request.setAttribute("ticket", ticket);
            request.setAttribute("comments", comments);
            request.setAttribute("isSlaBreached", Boolean.valueOf(ticketService.isSlaBreached(ticket)));
            request.getRequestDispatcher("ticketDetail.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to load ticket details.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private void createTicket(HttpServletRequest request, HttpServletResponse response, UserBean loggedUser)
            throws ServletException, IOException {
        if (!"USER".equals(loggedUser.getRole()) && !"ADMIN".equals(loggedUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Unauthorized+access");
            return;
        }

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String category = request.getParameter("category");
        String customCategory = request.getParameter("customCategory");

        if ("Other".equalsIgnoreCase(category) && !isBlank(customCategory)) {
            category = customCategory.trim();
        }

        if (isBlank(title) || isBlank(description) || isBlank(category)) {
            request.setAttribute("errorMessage", "Title, description and category are required.");
            request.getRequestDispatcher("raiseTicket.jsp").forward(request, response);
            return;
        }

        try {
            TicketBean ticket = new TicketBean();
            ticket.setTitle(title.trim());
            ticket.setDescription(description.trim());
            ticket.setCategory(category.trim());
            ticket.setRaisedBy(loggedUser.getUserId());

            boolean created = ticketService.createTicket(ticket);
            if (!created) {
                request.setAttribute("errorMessage",
                        "Ticket was not created. It may be a duplicate in the last 10 minutes.");
                request.getRequestDispatcher("raiseTicket.jsp").forward(request, response);
                return;
            }

            response.sendRedirect(request.getContextPath() + "/ticket?action=view");
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to create ticket right now.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private void closeTicket(HttpServletRequest request, HttpServletResponse response, UserBean loggedUser)
            throws ServletException, IOException {
        if ("AGENT".equals(loggedUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Unauthorized+access");
            return;
        }

        int ticketId = parseInt(request.getParameter("ticketId"));
        if (ticketId <= 0) {
            response.sendRedirect(request.getContextPath() + "/ticket?action=view");
            return;
        }

        try {
            TicketBean ticket = ticketService.getTicketById(ticketId);
            if (ticket == null) {
                response.sendRedirect(request.getContextPath() + "/ticket?action=view");
                return;
            }

            if ("USER".equals(loggedUser.getRole()) && ticket.getRaisedBy() != loggedUser.getUserId()) {
                response.sendRedirect(request.getContextPath() + "/login.jsp?message=Unauthorized+access");
                return;
            }

            ticketService.updateTicketStatus(ticketId, "CLOSED");
            response.sendRedirect(request.getContextPath() + "/ticket?action=detail&ticketId=" + ticketId);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to close ticket.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private void reopenTicket(HttpServletRequest request, HttpServletResponse response, UserBean loggedUser)
            throws ServletException, IOException {
        int ticketId = parseInt(request.getParameter("ticketId"));
        if (ticketId <= 0) {
            response.sendRedirect(request.getContextPath() + "/ticket?action=view");
            return;
        }

        try {
            TicketBean ticket = ticketService.getTicketById(ticketId);
            if (ticket == null) {
                response.sendRedirect(request.getContextPath() + "/ticket?action=view");
                return;
            }

            if (!"ADMIN".equals(loggedUser.getRole()) && ticket.getRaisedBy() != loggedUser.getUserId()) {
                response.sendRedirect(request.getContextPath() + "/login.jsp?message=Unauthorized+access");
                return;
            }

            if (!"RESOLVED".equals(ticket.getStatus()) && !"CLOSED".equals(ticket.getStatus())) {
                response.sendRedirect(buildDetailRedirect(request, ticketId,
                        "Only RESOLVED or CLOSED tickets can be reopened."));
                return;
            }

            ticketService.updateTicketStatus(ticketId, "OPEN");
            response.sendRedirect(request.getContextPath() + "/ticket?action=detail&ticketId=" + ticketId);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to reopen ticket.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response, UserBean loggedUser)
            throws ServletException, IOException {
        if (!"AGENT".equals(loggedUser.getRole()) && !"ADMIN".equals(loggedUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Unauthorized+access");
            return;
        }

        int ticketId = parseInt(request.getParameter("ticketId"));
        String newStatus = request.getParameter("status");

        if (ticketId <= 0 || isBlank(newStatus)) {
            response.sendRedirect(request.getContextPath() + "/ticket?action=view");
            return;
        }

        try {
            TicketBean ticket = ticketService.getTicketById(ticketId);
            if (ticket == null) {
                response.sendRedirect(request.getContextPath() + "/ticket?action=view");
                return;
            }

            if (!isValidStatusUpdate(newStatus)) {
                response.sendRedirect(buildDetailRedirect(request, ticketId, "Invalid status value."));
                return;
            }

            if ("AGENT".equals(loggedUser.getRole()) && ticket.getAssignedTo() != loggedUser.getUserId()) {
                response.sendRedirect(request.getContextPath() + "/login.jsp?message=Unauthorized+access");
                return;
            }

            // Ticket must be assigned before moving from OPEN to work states
            if ("OPEN".equals(ticket.getStatus()) && ticket.getAssignedTo() <= 0
                    && ("IN_PROGRESS".equals(newStatus) || "RESOLVED".equals(newStatus))) {
                response.sendRedirect(buildDetailRedirect(request, ticketId,
                        "Assign the ticket before updating status."));
                return;
            }

            ticketService.updateTicketStatus(ticketId, newStatus.trim());
            response.sendRedirect(request.getContextPath() + "/ticket?action=detail&ticketId=" + ticketId);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unable to update ticket status.");
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

    private boolean canViewTicket(UserBean user, TicketBean ticket) {
        if ("ADMIN".equals(user.getRole())) {
            return true;
        }
        if ("AGENT".equals(user.getRole())) {
            return ticket.getAssignedTo() == user.getUserId();
        }
        return ticket.getRaisedBy() == user.getUserId();
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

    private boolean isValidStatusUpdate(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        return "IN_PROGRESS".equals(normalized) || "RESOLVED".equals(normalized);
    }

    private String buildDetailRedirect(HttpServletRequest request, int ticketId, String message) {
        try {
            return request.getContextPath() + "/ticket?action=detail&ticketId=" + ticketId
                    + "&message=" + URLEncoder.encode(message, "UTF-8");
        } catch (Exception e) {
            return request.getContextPath() + "/ticket?action=detail&ticketId=" + ticketId;
        }
    }
}
