package com.helpdesk.service;

import java.util.List;
import java.util.regex.Pattern;

import com.helpdesk.bean.UserBean;
import com.helpdesk.dao.UserDAO;
import java.util.UUID;

public class UserService {
    
    private final MailService mailService = new MailService();

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public UserBean validateUser(String email, String password) {
        if (isBlank(email) || isBlank(password)) {
            return null;
        }
        return userDAO.validateUser(email.trim(), password.trim());
    }

    public boolean registerUser(String name, String email, String password, String confirmPassword, String requestUrl) {
        if (isBlank(name) || isBlank(email) || isBlank(password) || isBlank(confirmPassword)) {
            return false;
        }
        if (!password.equals(confirmPassword)) {
            return false;
        }
        if (!isValidEmail(email.trim())) {
            return false;
        }

        UserBean user = new UserBean();
        user.setName(name.trim());
        user.setEmail(email.trim());
        user.setPassword(password.trim());
        user.setRole("USER");

        String token = UUID.randomUUID().toString();
        // Save as unverified user with a 10-minute verification token expiry
        boolean isPendingRegistered = userDAO.registerPendingUser(user, token, 10);
        
        if (isPendingRegistered) {
            // Send verification email
            boolean sent = mailService.sendVerificationEmail(email.trim(), token, requestUrl);
            if (sent) {
                return true;
            }
            userDAO.deleteUnverifiedUserByEmail(email.trim());
        }
        
        return false;
    }

    public boolean activateUser(String token) {
        UserBean pendingUser = userDAO.getPendingUserByToken(token);
        if (pendingUser != null) {
            return userDAO.deletePendingUser(pendingUser.getUserId());
        }
        return false;
    }

    public boolean requestPasswordReset(String email, String requestUrl) {
        if (!isValidEmail(email)) {
            return false;
        }
        String normalizedEmail = email.trim();
        String token = UUID.randomUUID().toString();
        boolean tokenCreated = userDAO.createPasswordResetToken(normalizedEmail, token, 10);
        if (!tokenCreated) {
            return false;
        }
        return mailService.sendPasswordResetEmail(normalizedEmail, token, requestUrl);
    }

    public boolean resetPassword(String token, String newPassword, String confirmPassword) {
        if (isBlank(token) || isBlank(newPassword) || isBlank(confirmPassword)) {
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            return false;
        }
        return userDAO.resetPasswordByToken(token.trim(), newPassword.trim());
    }

    public UserBean getUserById(int userId) {
        if (userId <= 0) {
            return null;
        }
        return userDAO.getUserById(userId);
    }

    public UserBean getUserByEmail(String email) {
        if (isBlank(email)) {
            return null;
        }
        return userDAO.getUserByEmail(email.trim());
    }

    public List<UserBean> getAllAgents() {
        return userDAO.getAllAgents();
    }

    public List<UserBean> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public boolean updateUserRole(int userId, String newRole) {
        if (userId <= 0 || isBlank(newRole)) {
            return false;
        }
        String role = newRole.trim().toUpperCase();
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

    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
