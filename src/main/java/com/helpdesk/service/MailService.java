package com.helpdesk.service;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailService {

    private final String username;
    private final String password;
    private final Properties props;

    public MailService() {
        // Read credentials from environment variables for Render deployment
        this.username = System.getenv("SMTP_USER");
        this.password = System.getenv("SMTP_PASS");

        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        // Default to Gmail if not specified, can be externalized as well
        props.put("mail.smtp.host", System.getenv("SMTP_HOST") != null ? System.getenv("SMTP_HOST") : "smtp.gmail.com");
        props.put("mail.smtp.port", System.getenv("SMTP_PORT") != null ? System.getenv("SMTP_PORT") : "587");
    }

    public boolean sendVerificationEmail(String toEmail, String token, String requestUrl) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            System.err.println("SMTP credentials are not configured. Cannot send verification email to " + toEmail);
            // In a development environment without credentials, print the link so we can test locally
            System.out.println("VERIFICATION LINK: " + requestUrl + "?token=" + token);
            return false;
        }

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("HSTA - Account Verification required");
            
            String verificationLink = requestUrl + "?token=" + token;
            
            String htmlContent = "<h3>Welcome to Helpdesk Support Ticket Automation</h3>" +
                    "<p>We've received a request to create an account for this email address.</p>" +
                    "<p>Please click the link below to verify your account. <b>This link will expire in 10 minutes.</b></p>" +
                    "<p><a href=\"" + verificationLink + "\">" + verificationLink + "</a></p>" +
                    "<br><p>If you did not request this, please ignore this email.</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Verification email successfully sent to " + toEmail);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
