package com.helpdesk.controller;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helpdesk.service.UserService;

public class VerifyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");

        if (token == null || token.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=" + URLEncoder.encode("Invalid verification link.", "UTF-8"));
            return;
        }

        boolean verified = userService.activateUser(token.trim());

        if (verified) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=" + URLEncoder.encode("Account successfully verified! You can now login.", "UTF-8"));
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=" + URLEncoder.encode("Verification failed. The link may have expired or is invalid.", "UTF-8"));
        }
    }
}
