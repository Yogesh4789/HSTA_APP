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
                    <div class="password-field-wrap">
                        <input type="password" id="newPassword" name="newPassword" required minlength="8" maxlength="16"
                            pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,16}$"
                            title="8-16 chars with uppercase, lowercase, number, and special character. No spaces.">
                        <button type="button" class="password-toggle-btn" data-target="newPassword"
                            aria-label="Show password" title="Show password">&#128065;</button>
                    </div>

                    <label>Confirm New Password</label>
                    <div class="password-field-wrap">
                        <input type="password" id="confirmNewPassword" name="confirmPassword" required minlength="8" maxlength="16"
                            pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,16}$"
                            title="8-16 chars with uppercase, lowercase, number, and special character. No spaces.">
                        <button type="button" class="password-toggle-btn" data-target="confirmNewPassword"
                            aria-label="Show password" title="Show password">&#128065;</button>
                    </div>
                    <div class="small">Password must be 8-16 chars and include uppercase, lowercase, number, and special character (no spaces).</div>

                    <button class="btn btn-primary btn-gap-top" type="submit">Update Password</button>
                </form>
            <% } %>
        </div>
    </div>
    <script>
        (function () {
            var toggleButtons = document.querySelectorAll(".password-toggle-btn");
            toggleButtons.forEach(function (btn) {
                btn.addEventListener("click", function () {
                    var targetId = btn.getAttribute("data-target");
                    var input = document.getElementById(targetId);
                    if (!input) return;
                    if (input.type === "password") {
                        input.type = "text";
                        btn.classList.add("is-visible");
                        btn.setAttribute("aria-label", "Hide password");
                        btn.setAttribute("title", "Hide password");
                    } else {
                        input.type = "password";
                        btn.classList.remove("is-visible");
                        btn.setAttribute("aria-label", "Show password");
                        btn.setAttribute("title", "Show password");
                    }
                    input.focus();
                });
            });
        })();
    </script>
</body>
</html>
