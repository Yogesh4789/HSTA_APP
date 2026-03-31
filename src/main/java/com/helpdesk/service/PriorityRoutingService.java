package com.helpdesk.service;

import java.util.List;

import com.helpdesk.bean.TicketBean;
import com.helpdesk.bean.UserBean;
import com.helpdesk.dao.TicketDAO;
import com.helpdesk.dao.UserDAO;

public class PriorityRoutingService {

    private final UserDAO userDAO;
    private final TicketDAO ticketDAO;

    public PriorityRoutingService() {
        this.userDAO = new UserDAO();
        this.ticketDAO = new TicketDAO();
    }

    public String assignPriority(String category) {
        if (category == null) {
            return "MEDIUM";
        }

        String normalized = category.trim().toLowerCase();
        if (normalized.contains("security") || normalized.contains("outage") || normalized.contains("payment")) {
            return "CRITICAL";
        }
        if (normalized.contains("authentication") || normalized.contains("network") || normalized.contains("database")) {
            return "HIGH";
        }
        if (normalized.contains("software") || normalized.contains("application") || normalized.contains("performance")) {
            return "MEDIUM";
        }
        return "LOW";
    }

    public int findAvailableAgentId() {
        List<UserBean> agents = userDAO.getAllAgents();
        if (agents == null || agents.isEmpty()) {
            return 0;
        }

        int selectedAgentId = 0;
        int minimumOpenLoad = Integer.MAX_VALUE;

        for (UserBean agent : agents) {
            List<TicketBean> assignedTickets = ticketDAO.getTicketsByAgent(agent.getUserId());
            int activeCount = 0;

            for (TicketBean ticket : assignedTickets) {
                if ("OPEN".equals(ticket.getStatus()) || "ASSIGNED".equals(ticket.getStatus())
                        || "IN_PROGRESS".equals(ticket.getStatus())) {
                    activeCount++;
                }
            }

            if (activeCount < minimumOpenLoad) {
                minimumOpenLoad = activeCount;
                selectedAgentId = agent.getUserId();
            }
        }

        return selectedAgentId;
    }
}
