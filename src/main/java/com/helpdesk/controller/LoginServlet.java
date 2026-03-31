package com.helpdesk.controller;

import java.io.IOException;
import java.net.URLEncoder;

import com.helpdesk.bean.UserBean;
import com.helpdesk.service.UserService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equalsIgnoreCase(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=Logged+out+successfully");
            return;
        }
        String rememberedEmail = readRememberedEmailFromCookie(request);
        if (!isBlank(rememberedEmail)) {
            request.setAttribute("rememberedEmail", rememberedEmail);
            request.setAttribute("rememberChecked", Boolean.TRUE);
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("login.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("register".equalsIgnoreCase(action)) {
            registerNewUser(request, response);
            return;
        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        boolean rememberEmail = "on".equalsIgnoreCase(request.getParameter("rememberEmail"));

        if (isBlank(email) || isBlank(password)) {
            request.setAttribute("errorMessage", "Email and password are required.");
            request.setAttribute("rememberedEmail", email);
            request.setAttribute("rememberChecked", Boolean.valueOf(rememberEmail));
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        try {
            UserBean user = userService.validateUser(email.trim(), password.trim());
            if (user == null) {
                UserBean existingUser = userService.getUserByEmail(email.trim());
                if (existingUser == null) {
                    request.setAttribute("errorMessage", "Account not found. Please create an account.");
                } else {
                    request.setAttribute("errorMessage", "Invalid credentials. Please try again.");
                }
                request.setAttribute("rememberedEmail", email);
                request.setAttribute("rememberChecked", Boolean.valueOf(rememberEmail));
                request.getRequestDispatcher("login.jsp").forward(request, response);
                return;
            }

            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("loggedUser", user);
            updateRememberEmailCookie(request, response, rememberEmail, email.trim());

            if ("ADMIN".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/admin?action=dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard.jsp");
            }
        } catch (Exception e) {
            String reason = e.getMessage();
            Throwable root = e.getCause();
            while (root != null) {
                reason = root.getMessage();
                root = root.getCause();
            }
            if (reason == null || reason.trim().isEmpty()) {
                reason = "Unknown database error";
            }
            request.setAttribute("errorMessage",
                    "Login failed due to database issue: " + reason);
            request.setAttribute("rememberedEmail", email);
            request.setAttribute("rememberChecked", Boolean.valueOf(rememberEmail));
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }

    private void registerNewUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (isBlank(name) || isBlank(email) || isBlank(password) || isBlank(confirmPassword)) {
            request.setAttribute("errorMessage", "All registration fields are required.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Password and confirm password do not match.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        if (!userService.isValidEmail(email.trim())) {
            request.setAttribute("errorMessage", "Please enter a valid email address.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        try {
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();
            String requestUrl = scheme + "://" + serverName + ":" + serverPort + contextPath + "/verify";

            boolean registered = userService.registerUser(name, email, password, confirmPassword, requestUrl);
            if (registered) {
                response.sendRedirect(request.getContextPath()
                        + "/login.jsp?message=" + URLEncoder.encode("Registration initiated. Please check your email to verify your account within 10 minutes.", "UTF-8"));
                return;
            }

            request.setAttribute("errorMessage", "Unable to register. Email may already be in use.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Registration failed due to database issue.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String readRememberedEmailFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "";
        }
        for (Cookie cookie : cookies) {
            if ("rememberedEmail".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return "";
    }

    private void updateRememberEmailCookie(HttpServletRequest request, HttpServletResponse response,
            boolean rememberEmail, String email) {
        Cookie cookie = new Cookie("rememberedEmail", rememberEmail ? email : "");
        String contextPath = request.getContextPath();
        if (isBlank(contextPath)) {
            contextPath = "/";
        }
        cookie.setPath(contextPath);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(rememberEmail ? 60 * 60 * 24 * 30 : 0);
        response.addCookie(cookie);
    }
}
