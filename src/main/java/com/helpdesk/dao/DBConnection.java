package com.helpdesk.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private DBConnection() {
        // Utility class
    }

    public static Connection getConnection() throws SQLException {
        String url = getRequiredConfig("HELPDESK_DB_URL", "helpdesk.db.url");
        String username = getRequiredConfig("HELPDESK_DB_USER", "helpdesk.db.user");
        String password = getRequiredConfig("HELPDESK_DB_PASSWORD", "helpdesk.db.password");
        url = ensureMySqlCompatibilityParams(url);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found in classpath.", e);
        }

        return DriverManager.getConnection(url, username, password);
    }

    private static String ensureMySqlCompatibilityParams(String url) {
        if (url == null) {
            return null;
        }
        String normalized = url.trim();
        String lower = normalized.toLowerCase();
        if (!lower.startsWith("jdbc:mysql:")) {
            return normalized;
        }
        if (lower.contains("allowpublickeyretrieval=")) {
            return normalized;
        }
        char joiner = normalized.contains("?") ? '&' : '?';
        return normalized + joiner + "allowPublicKeyRetrieval=true";
    }

    private static String getRequiredConfig(String envKey, String systemPropertyKey) throws SQLException {
        String value = System.getenv(envKey);
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(systemPropertyKey);
        }
        if (value == null || value.trim().isEmpty()) {
            throw new SQLException("Missing DB config. Set env " + envKey
                    + " or VM arg -D" + systemPropertyKey + "=<value>.");
        }
        return value.trim();
    }
}
