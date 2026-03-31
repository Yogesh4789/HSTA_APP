-- Helpdesk Support Ticket Automation (HSTA)
-- Step 1: Database schema + seed data
-- MySQL 8.x

DROP DATABASE IF EXISTS helpdesk_db;
CREATE DATABASE helpdesk_db;
USE helpdesk_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `COMMENT`;
DROP TABLE IF EXISTS `KNOWLEDGE_BASE`;
DROP TABLE IF EXISTS `TICKET`;
DROP TABLE IF EXISTS `SLA_POLICY`;
DROP TABLE IF EXISTS `USER`;
DROP TABLE IF EXISTS `PENDING_USER`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `PENDING_USER` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    token VARCHAR(100) NOT NULL UNIQUE,
    expiry_time DATETIME NOT NULL
);

CREATE TABLE `USER` (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'AGENT', 'ADMIN') NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `SLA_POLICY` (
    policy_id INT PRIMARY KEY AUTO_INCREMENT,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL UNIQUE,
    response_time_hours INT NOT NULL,
    resolution_time_hours INT NOT NULL
);

CREATE TABLE `TICKET` (
    ticket_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL,
    status ENUM('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') DEFAULT 'OPEN',
    raised_by INT NOT NULL,
    assigned_to INT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sla_deadline DATETIME NOT NULL,
    resolved_at DATETIME NULL,
    CONSTRAINT fk_ticket_raised_by FOREIGN KEY (raised_by) REFERENCES `USER`(user_id),
    CONSTRAINT fk_ticket_assigned_to FOREIGN KEY (assigned_to) REFERENCES `USER`(user_id)
);

CREATE TABLE `COMMENT` (
    comment_id INT PRIMARY KEY AUTO_INCREMENT,
    ticket_id INT NOT NULL,
    commented_by INT NOT NULL,
    comment_text TEXT NOT NULL,
    commented_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES `TICKET`(ticket_id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (commented_by) REFERENCES `USER`(user_id)
);

CREATE TABLE `KNOWLEDGE_BASE` (
    article_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    created_by INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kb_user FOREIGN KEY (created_by) REFERENCES `USER`(user_id)
);

CREATE INDEX idx_ticket_raised_by ON `TICKET`(raised_by);
CREATE INDEX idx_ticket_assigned_to ON `TICKET`(assigned_to);
CREATE INDEX idx_ticket_status ON `TICKET`(status);
CREATE INDEX idx_ticket_priority ON `TICKET`(priority);
CREATE INDEX idx_comment_ticket ON `COMMENT`(ticket_id);
CREATE INDEX idx_kb_category ON `KNOWLEDGE_BASE`(category);

INSERT INTO `USER` (name, email, password, role) VALUES
('Admin User', 'admin@helpdesk.com', 'admin123', 'ADMIN'),
('Agent Smith', 'agent1@helpdesk.com', 'agent123', 'AGENT'),
('Agent Markov', 'agent2@helpdesk.com', 'agent123', 'AGENT'),
('Rohit User', 'rohit@helpdesk.com', 'user123', 'USER'),
('Ananya User', 'ananya@helpdesk.com', 'user123', 'USER');

INSERT INTO `SLA_POLICY` (priority, response_time_hours, resolution_time_hours) VALUES
('LOW', 8, 72),
('MEDIUM', 4, 48),
('HIGH', 2, 24),
('CRITICAL', 1, 8);

INSERT INTO `TICKET` (
    title, description, category, priority, status,
    raised_by, assigned_to, created_at, sla_deadline, resolved_at
) VALUES
(
    'Unable to login to LMS',
    'I am getting invalid credentials even with correct password.',
    'Authentication',
    'HIGH',
    'ASSIGNED',
    4,
    2,
    NOW() - INTERVAL 2 HOUR,
    DATE_ADD(NOW() - INTERVAL 2 HOUR, INTERVAL 24 HOUR),
    NULL
),
(
    'Lab PC not connecting to WiFi',
    'Device fails to obtain IP address in lab block.',
    'Network',
    'MEDIUM',
    'IN_PROGRESS',
    5,
    3,
    NOW() - INTERVAL 5 HOUR,
    DATE_ADD(NOW() - INTERVAL 5 HOUR, INTERVAL 48 HOUR),
    NULL
),
(
    'Need software installation approval',
    'Please approve Python package installation for ML project.',
    'Software',
    'LOW',
    'OPEN',
    4,
    NULL,
    NOW() - INTERVAL 1 HOUR,
    DATE_ADD(NOW() - INTERVAL 1 HOUR, INTERVAL 72 HOUR),
    NULL
);

INSERT INTO `COMMENT` (ticket_id, commented_by, comment_text, commented_at) VALUES
(1, 2, 'Issue acknowledged. Checking authentication logs.', NOW() - INTERVAL 90 MINUTE),
(2, 3, 'Assigned and troubleshooting DHCP configuration.', NOW() - INTERVAL 2 HOUR),
(2, 5, 'Thank you. Please update once fixed.', NOW() - INTERVAL 1 HOUR);

INSERT INTO `KNOWLEDGE_BASE` (title, content, category, created_by) VALUES
(
    'How to reset portal password',
    'Use Forgot Password on login page, verify OTP and set a strong password.',
    'Authentication',
    2
),
(
    'Fixing common campus WiFi issues',
    'Forget network, reconnect using institute credentials, then renew IP.',
    'Network',
    3
);

-- use helpdesk_db;
-- show tables;
-- select email,role from `USER`;

-- use helpdesk_db;
-- select user_id, name, email, role, created_at, password 
-- from `USER`
-- order by user_id desc;


