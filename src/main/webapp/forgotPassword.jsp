<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String errorMessage = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Forgot Password - HSTA</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body>
    <div class="container" style="max-width: 500px; padding-top: 60px;">
        <div class="card">
            <div class="topbar">
                <h2 class="title">Forgot Password</h2>
                <a class="btn btn-secondary" href="<%=request.getContextPath()%>/login.jsp">Back</a>
            </div>

            <% if (errorMessage != null) { %>
                <div class="alert alert-error"><%=errorMessage%></div>
            <% } %>

            <p class="small">Enter your registered email and we will send a password reset link.</p>

            <form action="<%=request.getContextPath()%>/login" method="post">
                <input type="hidden" name="action" value="forgotPassword">

                <label>Email</label>
                <input type="email" name="email" required>

                <button class="btn btn-primary" type="submit">Send Reset Link</button>
            </form>
        </div>
    </div>
</body>
</html>
