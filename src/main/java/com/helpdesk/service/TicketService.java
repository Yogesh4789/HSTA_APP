package com.helpdesk.service;

import java.util.Date;
import java.util.List;

import com.helpdesk.bean.TicketBean;
import com.helpdesk.dao.TicketDAO;

public class TicketService {

    private final TicketDAO ticketDAO;
    private final PriorityRoutingService priorityRoutingService;
    private final SLAService slaService;

    public TicketService() {
        this.ticketDAO = new TicketDAO();
        this.priorityRoutingService = new PriorityRoutingService();
        this.slaService = new SLAService();
    }

    public boolean createTicket(TicketBean ticket) {
        if (ticket == null || ticket.getRaisedBy() <= 0 || isBlank(ticket.getTitle()) || isBlank(ticket.getDescription())
                || isBlank(ticket.getCategory())) {
            return false;
        }

        // Block duplicate ticket: same user + same title within last 10 minutes (optimized DB query)
        if (ticketDAO.hasRecentDuplicate(ticket.getRaisedBy(), ticket.getTitle(), 10)) {
            return false;
        }

        String priority = priorityRoutingService.assignPriority(ticket.getCategory());
        ticket.setPriority(priority);

        int agentId = priorityRoutingService.findAvailableAgentId();
        if (agentId > 0) {
            ticket.setAssignedTo(agentId);
            ticket.setStatus("ASSIGNED");
        } else {
            ticket.setAssignedTo(0);
            ticket.setStatus("OPEN");
        }

        Date slaDeadline = slaService.calculateDeadline(priority);
        ticket.setSlaDeadline(slaDeadline);

        return ticketDAO.createTicket(ticket);
    }

    public TicketBean getTicketById(int ticketId) {
        if (ticketId <= 0) {
            return null;
        }
        return ticketDAO.getTicketById(ticketId);
    }

    public List<TicketBean> getTicketsByUser(int userId) {
        return ticketDAO.getTicketsByUser(userId);
    }

    public List<TicketBean> getTicketsByAgent(int agentId) {
        return ticketDAO.getTicketsByAgent(agentId);
    }

    public List<TicketBean> getAllTickets() {
        return ticketDAO.getAllTickets();
    }

    public boolean updateTicketStatus(int ticketId, String status) {
        if (ticketId <= 0 || isBlank(status)) {
            return false;
        }
        return ticketDAO.updateTicketStatus(ticketId, status.trim());
    }

    public boolean assignTicket(int ticketId, int agentId) {
        if (ticketId <= 0 || agentId <= 0) {
            return false;
        }
        return ticketDAO.assignTicket(ticketId, agentId);
    }

    public boolean updateTicketPriority(int ticketId, String priority) {
        if (ticketId <= 0 || isBlank(priority)) {
            return false;
        }
        return ticketDAO.updateTicketPriority(ticketId, priority.trim());
    }

    public boolean isSlaBreached(TicketBean ticket) {
        if (ticket == null) {
            return false;
        }
        return slaService.isSlaBreached(ticket.getSlaDeadline(), ticket.getStatus());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
