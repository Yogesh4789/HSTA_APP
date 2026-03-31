package com.helpdesk.service;

import java.util.List;

import com.helpdesk.bean.CommentBean;
import com.helpdesk.bean.TicketBean;
import com.helpdesk.dao.CommentDAO;
import com.helpdesk.dao.TicketDAO;

public class CommentService {

    private final CommentDAO commentDAO;
    private final TicketDAO ticketDAO;

    public CommentService() {
        this.commentDAO = new CommentDAO();
        this.ticketDAO = new TicketDAO();
    }

    /**
     * Add a comment to a ticket.
     * Returns null on success, or an error message string on failure.
     */
    public String addComment(int ticketId, int userId, String commentText) {
        if (ticketId <= 0) {
            return "Invalid ticket ID.";
        }
        if (userId <= 0) {
            return "Invalid user.";
        }
        if (isBlank(commentText)) {
            return "Comment text cannot be empty.";
        }

        TicketBean ticket = ticketDAO.getTicketById(ticketId);
        if (ticket == null) {
            return "Ticket not found.";
        }
        if ("CLOSED".equals(ticket.getStatus())) {
            return "Cannot comment on a closed ticket.";
        }

        CommentBean comment = new CommentBean();
        comment.setTicketId(ticketId);
        comment.setCommentedBy(userId);
        comment.setCommentText(commentText.trim());

        boolean added = commentDAO.addComment(comment);
        return added ? null : "Failed to save comment.";
    }

    public List<CommentBean> getCommentsByTicket(int ticketId) {
        return commentDAO.getCommentsByTicket(ticketId);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
