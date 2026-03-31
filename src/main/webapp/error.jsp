<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String errorMessage = (String) request.getAttribute("errorMessage");
if (errorMessage == null || errorMessage.trim().isEmpty()) {
    errorMessage = "Something went wrong. Please try again.";
}
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Error - HSTA</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body>
    <div class="container" style="max-width: 700px; padding-top: 60px;">
        <div class="card">
            <h2 class="title">Oops, we hit an issue</h2>
            <div class="alert alert-error"><%=errorMessage%></div>
            <a class="btn btn-primary" href="<%=request.getContextPath()%>/dashboard.jsp">Back to Dashboard</a>
            <a class="btn btn-secondary" href="<%=request.getContextPath()%>/login.jsp">Login Page</a>
        </div>
    </div>
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
