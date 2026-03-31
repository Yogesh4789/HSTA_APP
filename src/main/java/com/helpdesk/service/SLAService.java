package com.helpdesk.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.helpdesk.bean.SLAPolicyBean;
import com.helpdesk.dao.SLAPolicyDAO;

public class SLAService {

    private final SLAPolicyDAO slaPolicyDAO;

    public SLAService() {
        this.slaPolicyDAO = new SLAPolicyDAO();
    }

    public Date calculateDeadline(String priority) {
        String effectivePriority = (priority == null || priority.trim().isEmpty()) ? "HIGH" : priority.trim();
        SLAPolicyBean policy = slaPolicyDAO.getSLAByPriority(effectivePriority);

        // Fallback to HIGH policy if requested policy does not exist
        if (policy == null) {
            policy = slaPolicyDAO.getSLAByPriority("HIGH");
        }

        int resolutionHours = 24;
        if (policy != null && policy.getResolutionTimeHours() > 0) {
            resolutionHours = policy.getResolutionTimeHours();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, resolutionHours);
        return calendar.getTime();
    }

    public List<SLAPolicyBean> getAllPolicies() {
        return slaPolicyDAO.getAllPolicies();
    }

    public boolean updatePolicy(String priority, int responseHours, int resolutionHours) {
        if (priority == null || priority.trim().isEmpty() || responseHours <= 0 || resolutionHours <= 0) {
            return false;
        }
        SLAPolicyBean policy = new SLAPolicyBean();
        policy.setPriority(priority.trim());
        policy.setResponseTimeHours(responseHours);
        policy.setResolutionTimeHours(resolutionHours);
        return slaPolicyDAO.updateSLAPolicy(policy);
    }

    public SLAPolicyBean getPolicyByPriority(String priority) {
        if (priority == null || priority.trim().isEmpty()) {
            return null;
        }
        return slaPolicyDAO.getSLAByPriority(priority.trim());
    }

    public boolean isSlaBreached(Date slaDeadline, String status) {
        if (slaDeadline == null) {
            return false;
        }
        if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
            return false;
        }
        return slaDeadline.before(new Date());
    }
}
