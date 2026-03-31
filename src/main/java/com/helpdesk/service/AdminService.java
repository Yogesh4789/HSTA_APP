package com.helpdesk.service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.helpdesk.bean.TicketBean;
import com.helpdesk.bean.UserBean;
import com.helpdesk.dao.TicketDAO;
import com.helpdesk.dao.UserDAO;

public class AdminService {

    private final TicketDAO ticketDAO;
    private final UserDAO userDAO;

    public AdminService() {
        this.ticketDAO = new TicketDAO();
        this.userDAO = new UserDAO();
    }

    public List<TicketBean> getAllTickets() {
        return ticketDAO.getAllTickets();
    }

    public List<UserBean> getAllAgents() {
        return userDAO.getAllAgents();
    }

    public List<UserBean> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public boolean assignTicketToAgent(int ticketId, int agentId) {
        if (ticketId <= 0 || agentId <= 0) {
            return false;
        }
        UserBean agent = userDAO.getUserById(agentId);
        if (agent == null || !"AGENT".equals(agent.getRole())) {
            return false;
        }
        return ticketDAO.assignTicket(ticketId, agentId);
    }

    public boolean updateUserRole(int userId, String newRole) {
        if (userId <= 0) {
            return false;
        }
        String role = (newRole == null) ? "" : newRole.trim().toUpperCase();
        if (!"USER".equals(role) && !"AGENT".equals(role) && !"ADMIN".equals(role)) {
            return false;
        }
        return userDAO.updateUserRole(userId, role);
    }

    public boolean deleteUser(int userId) {
        if (userId <= 0) {
            return false;
        }
        return userDAO.deleteUser(userId);
    }

    /**
     * Generate report statistics.
     * Returns a map containing: totalTickets, resolvedTickets, slaBreachedTickets, perAgentStats
     */
    public Map<String, Object> generateReports() {
        List<TicketBean> tickets = ticketDAO.getAllTickets();
        List<UserBean> agents = userDAO.getAllAgents();

        int totalTickets = tickets.size();
        int resolvedTickets = 0;
        int slaBreachedTickets = 0;
        Date now = new Date();

        Map<String, Integer> perAgentStats = new LinkedHashMap<String, Integer>();
        for (UserBean agent : agents) {
            perAgentStats.put(agent.getName(), Integer.valueOf(0));
        }

        for (TicketBean ticket : tickets) {
            if ("RESOLVED".equals(ticket.getStatus()) || "CLOSED".equals(ticket.getStatus())) {
                resolvedTickets++;
            }
            if (ticket.getSlaDeadline() != null
                    && ticket.getSlaDeadline().before(now)
                    && !"RESOLVED".equals(ticket.getStatus())
                    && !"CLOSED".equals(ticket.getStatus())) {
                slaBreachedTickets++;
            }

            if (ticket.getAssignedTo() > 0) {
                for (UserBean agent : agents) {
                    if (agent.getUserId() == ticket.getAssignedTo()) {
                        String key = agent.getName();
                        Integer current = perAgentStats.get(key);
                        if (current == null) {
                            current = Integer.valueOf(0);
                        }
                        perAgentStats.put(key, Integer.valueOf(current.intValue() + 1));
                        break;
                    }
                }
            }
        }

        Map<String, Object> report = new LinkedHashMap<String, Object>();
        report.put("totalTickets", Integer.valueOf(totalTickets));
        report.put("resolvedTickets", Integer.valueOf(resolvedTickets));
        report.put("slaBreachedTickets", Integer.valueOf(slaBreachedTickets));
        report.put("perAgentStats", perAgentStats);
        report.put("tickets", tickets);
        report.put("agents", agents);

        return report;
    }
}
