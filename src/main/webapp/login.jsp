<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String errorMessage = (String) request.getAttribute("errorMessage");
String message = request.getParameter("message");
String action = request.getParameter("action");
boolean defaultRegisterView = "register".equalsIgnoreCase(action);
String rememberedEmail = (String) request.getAttribute("rememberedEmail");
if (rememberedEmail == null) {
    rememberedEmail = "";
}
if (rememberedEmail.trim().isEmpty()) {
    javax.servlet.http.Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (javax.servlet.http.Cookie cookie : cookies) {
            if ("rememberedEmail".equals(cookie.getName()) && cookie.getValue() != null) {
                rememberedEmail = cookie.getValue();
                break;
            }
        }
    }
}
String rememberedEmailEscaped = rememberedEmail.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
boolean rememberChecked = Boolean.TRUE.equals(request.getAttribute("rememberChecked"))
        || !rememberedEmail.trim().isEmpty();
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Login - HSTA</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body>
    <div class="container" style="max-width: 500px; padding-top: 60px;">
        <div class="card">
            <div class="topbar">
                <h2 class="title" id="authTitle"><%= defaultRegisterView ? "Create Account" : "Sign In" %></h2>
                <a class="btn btn-secondary" href="<%=request.getContextPath()%>/index.jsp">Back</a>
            </div>

            <% if (errorMessage != null) { %>
                <div class="alert alert-error"><%=errorMessage%></div>
            <% } %>
            <% if (message != null) { %>
                <div class="alert alert-info"><%=message%></div>
            <% } %>

            <div id="loginView" style="<%= defaultRegisterView ? "display:none;" : "" %>">
                <form action="<%=request.getContextPath()%>/login" method="post">
                    <input type="hidden" name="action" value="login">
                    <label>Email</label>
                    <input type="email" name="email" value="<%=rememberedEmailEscaped%>" required>

                    <label>Password</label>
                    <input type="password" name="password" required>

                    <label style="display:flex; align-items:center; gap:8px; margin-bottom:14px; font-weight:500;">
                        <input type="checkbox" name="rememberEmail" style="width:auto; margin:0;" <%= rememberChecked ? "checked" : "" %>>
                        Remember my email
                    </label>

                    <button class="btn btn-primary" type="submit">Login</button>
                    <div class="auth-helper-row">
                        <a href="<%=request.getContextPath()%>/forgotPassword.jsp" class="auth-link-text">Forgot Password?</a>
                    </div>
                </form>
                <hr style="margin: 22px 0; border: 0; border-top: 1px solid #eaecf0;">
                <a id="registerToggleBtn" class="btn btn-secondary" href="<%=request.getContextPath()%>/login.jsp#registerSection">New User? Create Account</a>
            </div>

            <div id="registerView" style="<%= defaultRegisterView ? "" : "display:none;" %>">
                <form action="<%=request.getContextPath()%>/login" method="post">
                    <input type="hidden" name="action" value="register">

                    <label>Name</label>
                    <input type="text" name="name" required>

                    <label>Email</label>
                    <input type="email" name="email" required>

                    <label>Password</label>
                    <div class="password-field-wrap">
                        <input type="password" id="registerPassword" name="password" required minlength="8" maxlength="16"
                            pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,16}$"
                            title="8-16 chars with uppercase, lowercase, number, and special character. No spaces.">
                        <button type="button" class="password-toggle-btn" data-target="registerPassword"
                            aria-label="Show password" title="Show password">&#128065;</button>
                    </div>

                    <label>Confirm Password</label>
                    <div class="password-field-wrap">
                        <input type="password" id="registerConfirmPassword" name="confirmPassword" required minlength="8" maxlength="16"
                            pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,16}$"
                            title="8-16 chars with uppercase, lowercase, number, and special character. No spaces.">
                        <button type="button" class="password-toggle-btn" data-target="registerConfirmPassword"
                            aria-label="Show password" title="Show password">&#128065;</button>
                    </div>
                    <div class="small" style="margin-bottom: 10px;">Password must be 8-16 chars and include uppercase, lowercase, number, and special character (no spaces).</div>

                    <button class="btn btn-secondary" type="submit">Create Account</button>
                </form>
                <hr style="margin: 22px 0; border: 0; border-top: 1px solid #eaecf0;">
                <a id="loginToggleBtn" class="btn btn-primary" href="<%=request.getContextPath()%>/login.jsp">Back to Sign In</a>
            </div>
        </div>
    </div>
    <script>
        (function () {
            var authTitle = document.getElementById("authTitle");
            var loginView = document.getElementById("loginView");
            var registerView = document.getElementById("registerView");
            var toggleBtn = document.getElementById("registerToggleBtn");
            var loginToggleBtn = document.getElementById("loginToggleBtn");
            var registerPasswordInput = document.getElementById("registerPassword");
            var registerConfirmPasswordInput = document.getElementById("registerConfirmPassword");
            var registerToggleButtons = document.querySelectorAll("#registerView .password-toggle-btn");
            var passwordPolicyMessage = "Invalid password. Use 8-16 chars with uppercase, lowercase, number, and special character (no spaces).";

            function showRegisterView() {
                if (loginView) loginView.style.display = "none";
                if (registerView) registerView.style.display = "block";
                if (authTitle) authTitle.textContent = "Create Account";
            }

            function showLoginView() {
                if (registerView) registerView.style.display = "none";
                if (loginView) loginView.style.display = "block";
                if (authTitle) authTitle.textContent = "Sign In";
            }

            if (window.location.hash === "#registerSection") {
                showRegisterView();
            }

            if (toggleBtn) {
                toggleBtn.addEventListener("click", function (event) {
                    event.preventDefault();
                    showRegisterView();
                    if (history.replaceState) {
                        history.replaceState(null, "", "#registerSection");
                    } else {
                        window.location.hash = "registerSection";
                    }
                });
            }

            if (loginToggleBtn) {
                loginToggleBtn.addEventListener("click", function (event) {
                    event.preventDefault();
                    showLoginView();
                    if (history.replaceState) {
                        history.replaceState(null, "", "<%=request.getContextPath()%>/login.jsp");
                    } else {
                        window.location.hash = "";
                    }
                });
            }

            function passwordIsValid() {
                return registerPasswordInput && registerPasswordInput.checkValidity();
            }

            if (registerPasswordInput) {
                registerPasswordInput.addEventListener("input", function () {
                    registerPasswordInput.setCustomValidity("");
                });

                registerPasswordInput.addEventListener("invalid", function () {
                    registerPasswordInput.setCustomValidity(passwordPolicyMessage);
                });

                registerPasswordInput.addEventListener("keydown", function (event) {
                    if (event.key === "Tab" && !passwordIsValid()) {
                        event.preventDefault();
                        registerPasswordInput.setCustomValidity(passwordPolicyMessage);
                        registerPasswordInput.reportValidity();
                    }
                });
            }

            if (registerConfirmPasswordInput) {
                registerConfirmPasswordInput.addEventListener("focus", function () {
                    if (!passwordIsValid()) {
                        registerPasswordInput.setCustomValidity(passwordPolicyMessage);
                        registerPasswordInput.reportValidity();
                        registerPasswordInput.focus();
                    }
                });
            }

            registerToggleButtons.forEach(function (btn) {
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
    <script>
        (function () {
            document.addEventListener("DOMContentLoaded", function () {
                var navbar = document.querySelector(".navbar, .home-header");
                if (navbar) {
                    var updateNavbarState = function () {
                        navbar.classList.toggle("scrolled", window.scrollY > 50);
                    };
                    updateNavbarState();
                    window.addEventListener("scroll", updateNavbarState);
                }

                var heroHeading = document.querySelector(".home-hero h1, .card .title, h1");
                if (heroHeading) {
                    heroHeading.classList.add("hero-text");
                }

                var revealTargets = document.querySelectorAll(".card, table, form");
                revealTargets.forEach(function (el) {
                    el.classList.add("hidden");
                });

                if ("IntersectionObserver" in window) {
                    var observer = new IntersectionObserver(function (entries) {
                        entries.forEach(function (entry) {
                            if (entry.isIntersecting) {
                                entry.target.classList.add("show");
                                observer.unobserve(entry.target);
                            }
                        });
                    }, { threshold: 0.12 });

                    revealTargets.forEach(function (el) {
                        observer.observe(el);
                    });
                } else {
                    revealTargets.forEach(function (el) {
                        el.classList.add("show");
                    });
                }
            });
        })();
    </script>
</body>
</html>
