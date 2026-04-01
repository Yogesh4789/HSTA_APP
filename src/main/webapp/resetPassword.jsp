<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String errorMessage = (String) request.getAttribute("errorMessage");
String token = request.getParameter("token");
if (token == null || token.trim().isEmpty()) {
    Object tokenAttr = request.getAttribute("token");
    if (tokenAttr != null) {
        token = String.valueOf(tokenAttr);
    }
}
if (token == null) {
    token = "";
}
String tokenEscaped = token.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Reset Password - HSTA</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body>
    <div class="container" style="max-width: 500px; padding-top: 60px;">
        <div class="card">
            <div class="topbar">
                <h2 class="title">Reset Password</h2>
                <a class="btn btn-secondary" href="<%=request.getContextPath()%>/login.jsp">Back</a>
            </div>

            <% if (errorMessage != null) { %>
                <div class="alert alert-error"><%=errorMessage%></div>
            <% } %>

            <% if (token.trim().isEmpty()) { %>
                <div class="alert alert-error">Reset token is missing or invalid. Please request a new reset link.</div>
                <a class="btn btn-primary" href="<%=request.getContextPath()%>/forgotPassword.jsp">Request New Link</a>
            <% } else { %>
                <form action="<%=request.getContextPath()%>/login" method="post">
                    <input type="hidden" name="action" value="resetPassword">
                    <input type="hidden" name="token" value="<%=tokenEscaped%>">

                    <label>New Password</label>
                    <input type="password" name="newPassword" required minlength="8" maxlength="16"
                        pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,16}$"
                        title="8-16 chars with uppercase, lowercase, number, and special character. No spaces.">

                    <label>Confirm New Password</label>
                    <input type="password" name="confirmPassword" required minlength="8" maxlength="16"
                        pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,16}$"
                        title="8-16 chars with uppercase, lowercase, number, and special character. No spaces.">
                    <div class="small">Password must be 8-16 chars and include uppercase, lowercase, number, and special character (no spaces).</div>

                    <button class="btn btn-primary" type="submit">Update Password</button>
                </form>
            <% } %>
        </div>
    </div>
</body>
</html>
