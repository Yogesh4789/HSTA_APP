<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>HSTA Home</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body class="home-page">
    <div class="home-header sticky-top">
        <div class="container-fluid px-3 px-md-4 px-lg-5 py-3 d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2 text-white fw-bold">
                <span class="home-logo"><i class="fa-solid fa-life-ring"></i></span>
                <span>Helpdesk Support Ticket Automation</span>
            </div>
            <div class="d-flex gap-2">
                <a class="btn btn-outline-light" href="<%=request.getContextPath()%>/login.jsp#registerSection">Register</a>
                <a class="btn btn-primary" href="<%=request.getContextPath()%>/login.jsp">Login</a>
            </div>
        </div>
    </div>

    <div class="container-fluid px-3 px-md-4 px-lg-5 mt-4">
        <div class="row">
            <div class="col-12">
                <section class="home-hero p-4 p-md-5 rounded-4 mb-4">
                    <div class="row align-items-center g-4">
                        <div class="col-lg-8">
                            <h1 class="display-5 fw-bold text-white mb-3">Modern Ticketing for Faster Support Resolution</h1>
                            <p class="text-light-emphasis fs-6 fs-md-5 mb-4">
                                HSTA centralizes ticket creation, assignment, SLA tracking and resolution workflows for users, agents and admins.
                            </p>
                            <div class="d-flex flex-wrap gap-2">
                                <a class="btn btn-primary px-4" href="<%=request.getContextPath()%>/login.jsp">Get Started</a>
                                <a class="btn btn-outline-light px-4" href="<%=request.getContextPath()%>/kb?action=list">Knowledge Base</a>
                            </div>
                        </div>
                        <div class="col-lg-4">
                            <div class="home-stat card shadow-sm p-3 mb-2">
                                <h5 class="mb-1 fw-bold">24x7</h5>
                                <p class="mb-0 text-muted">Ticket Visibility</p>
                            </div>
                            <div class="home-stat card shadow-sm p-3 mb-2">
                                <h5 class="mb-1 fw-bold">3 Roles</h5>
                                <p class="mb-0 text-muted">User, Agent, Admin</p>
                            </div>
                            <div class="home-stat card shadow-sm p-3">
                                <h5 class="mb-1 fw-bold">SLA Ready</h5>
                                <p class="mb-0 text-muted">Deadline-driven flow</p>
                            </div>
                        </div>
                    </div>
                </section>
            </div>
        </div>

        <section class="row g-4 mb-4">
            <div class="col-md-6 col-lg-3">
                <div class="card shadow-sm p-3 h-100 home-card">
                    <div class="home-card-icon mb-2"><i class="fa-solid fa-user"></i></div>
                    <h5>User / Requester</h5>
                    <p class="text-muted">Raise issues, track status, and reopen resolved tickets.</p>
                    <a class="btn btn-outline-primary mt-auto" href="<%=request.getContextPath()%>/login.jsp">Raise and Track</a>
                </div>
            </div>
            <div class="col-md-6 col-lg-3">
                <div class="card shadow-sm p-3 h-100 home-card">
                    <div class="home-card-icon mb-2"><i class="fa-solid fa-headset"></i></div>
                    <h5>Support Agent</h5>
                    <p class="text-muted">Handle assigned tickets and update progress with comments.</p>
                    <a class="btn btn-outline-primary mt-auto" href="<%=request.getContextPath()%>/login.jsp">Agent Workspace</a>
                </div>
            </div>
            <div class="col-md-6 col-lg-3">
                <div class="card shadow-sm p-3 h-100 home-card">
                    <div class="home-card-icon mb-2"><i class="fa-solid fa-user-gear"></i></div>
                    <h5>Admin</h5>
                    <p class="text-muted">Assign tickets, configure SLA policies, and view reports.</p>
                    <a class="btn btn-outline-primary mt-auto" href="<%=request.getContextPath()%>/login.jsp">Admin Console</a>
                </div>
            </div>
            <div class="col-md-6 col-lg-3">
                <div class="card shadow-sm p-3 h-100 home-card">
                    <div class="home-card-icon mb-2"><i class="fa-solid fa-book"></i></div>
                    <h5>Knowledge Base</h5>
                    <p class="text-muted">Search articles and solve common issues quickly.</p>
                    <a class="btn btn-outline-primary mt-auto" href="<%=request.getContextPath()%>/kb?action=list">Browse KB</a>
                </div>
            </div>
        </section>

    </div>

    <div class="home-footer mt-4 py-3">
        <div class="container-fluid px-3 px-md-4 px-lg-5 d-flex flex-column flex-md-row justify-content-between align-items-center gap-2 small">
            <span>Helpdesk Support Ticket Automation System</span>
            <div class="d-flex gap-3">
                <a href="<%=request.getContextPath()%>/index.jsp">Home</a>
                <a href="<%=request.getContextPath()%>/login.jsp">Login</a>
                <a href="<%=request.getContextPath()%>/kb?action=list">Knowledge Base</a>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
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
