package com.mmva.newsapp.infrastructure.email.service;

import com.mmva.newsapp.infrastructure.email.exception.EmailSendingException;
import com.mmva.newsapp.infrastructure.security.config.SecurityAlertNotificationProperties;
import com.mmva.newsapp.infrastructure.security.dto.SecurityAlertDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Implementation of EmailService using Spring Mail.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SecurityAlertNotificationProperties securityAlertNotificationProperties;

    @Value("${spring.mail.username:noreply@thenews.com}")
    private String fromEmail;

    @Value("${app.name:TheNews}")
    private String appName;

    @Override
    @Async("emailExecutor")
    public void sendVerificationCode(String to, String verificationCode) {
        String subject = appName + " - Email Verification Code";
        String body = buildVerificationEmailBody(verificationCode);

        try {
            sendHtmlEmail(to, subject, body);
            log.info("Verification email sent successfully to: {}", maskEmail(to));
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", maskEmail(to), e);
            throw new EmailSendingException("Failed to send verification email", e);
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendPasswordResetEmail(String to, String resetLink) {
        String subject = appName + " - Password Reset Request";
        String body = buildPasswordResetEmailBody(resetLink);

        try {
            sendHtmlEmail(to, subject, body);
            log.info("Password reset email sent successfully to: {}", maskEmail(to));
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", maskEmail(to), e);
            throw new EmailSendingException("Failed to send password reset email", e);
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to " + appName + "!";
        String body = buildWelcomeEmailBody(username);

        try {
            sendHtmlEmail(to, subject, body);
            log.info("Welcome email sent successfully to: {}", maskEmail(to));
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", maskEmail(to), e);
            throw new EmailSendingException("Failed to send welcome email", e);
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", maskEmail(to));
        } catch (MailException e) {
            log.error("Failed to send email to: {}", maskEmail(to), e);
            throw new EmailSendingException("Failed to send email", e);
        }
    }

    /**
     * Sends an HTML email.
     */
    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    /**
     * Builds the HTML body for verification emails.
     */
    private String buildVerificationEmailBody(String verificationCode) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background-color: #f9f9f9; }
                        .code { font-size: 32px; font-weight: bold; color: #007bff; text-align: center;
                                padding: 20px; background-color: #e9ecef; border-radius: 8px; letter-spacing: 5px; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                        .warning { color: #dc3545; font-size: 14px; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                        </div>
                        <div class="content">
                            <h2>Email Verification</h2>
                            <p>Your email verification code is:</p>
                            <div class="code">%s</div>
                            <p style="margin-top: 20px;">This code will expire in <strong>15 minutes</strong>.</p>
                            <p class="warning">If you didn't request this code, please ignore this email.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; %d %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(appName, verificationCode, java.time.Year.now().getValue(), appName);
    }

    /**
     * Builds the HTML body for password reset emails.
     */
    private String buildPasswordResetEmailBody(String resetLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background-color: #f9f9f9; }
                        .button { display: inline-block; background-color: #007bff; color: white;
                                  padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                        .warning { color: #dc3545; font-size: 14px; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                        </div>
                        <div class="content">
                            <h2>Password Reset Request</h2>
                            <p>We received a request to reset your password. Click the button below to create a new password:</p>
                            <p style="text-align: center;">
                                <a href="%s" class="button">Reset Password</a>
                            </p>
                            <p>Or copy and paste this link into your browser:</p>
                            <p style="word-break: break-all; font-size: 14px; color: #666;">%s</p>
                            <p>This link will expire in <strong>1 hour</strong>.</p>
                            <p class="warning">If you didn't request a password reset, please ignore this email.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; %d %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(appName, resetLink, resetLink, java.time.Year.now().getValue(), appName);
    }

    /**
     * Builds the HTML body for welcome emails.
     */
    private String buildWelcomeEmailBody(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #28a745; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background-color: #f9f9f9; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Welcome to %s!</h1>
                        </div>
                        <div class="content">
                            <h2>Hello, %s!</h2>
                            <p>Thank you for joining our team. Your admin account has been successfully created.</p>
                            <p>Here's what you can do next:</p>
                            <ul>
                                <li>Verify your email address</li>
                                <li>Complete your profile</li>
                                <li>Explore the admin dashboard</li>
                            </ul>
                            <p>If you have any questions, feel free to reach out to our support team.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; %d %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(appName, username, java.time.Year.now().getValue(), appName);
    }

    /**
     * Masks email for logging purposes (privacy).
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    // ========================================
    // Security Alert Email Methods
    // ========================================

    @Override
    @Async("emailExecutor")
    public void sendNewDeviceLoginAlert(SecurityAlertDto alert) {
        if (!securityAlertNotificationProperties.isEnabled() ||
                !securityAlertNotificationProperties.getNewDeviceAlert().isEnabled()) {
            log.debug("New device login alerts are disabled");
            return;
        }

        String subject = "🔔 " + appName + " - New Device Login Detected";
        String body = buildNewDeviceAlertBody(alert);

        try {
            sendHtmlEmail(alert.userEmail(), subject, body);
            log.info("New device login alert sent to: {}", maskEmail(alert.userEmail()));
        } catch (Exception e) {
            log.error("Failed to send new device login alert to: {}", maskEmail(alert.userEmail()), e);
            // Don't throw - security emails should not block login
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendNewLocationLoginAlert(SecurityAlertDto alert) {
        if (!securityAlertNotificationProperties.isEnabled() ||
                !securityAlertNotificationProperties.getNewLocationAlert().isEnabled()) {
            log.debug("New location login alerts are disabled");
            return;
        }

        String subject = "⚠️ " + appName + " - Login from New Location";
        String body = buildNewLocationAlertBody(alert);

        try {
            sendHtmlEmail(alert.userEmail(), subject, body);
            log.info("New location login alert sent to: {}", maskEmail(alert.userEmail()));
        } catch (Exception e) {
            log.error("Failed to send new location login alert to: {}", maskEmail(alert.userEmail()), e);
            // Don't throw - security emails should not block login
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendSuspiciousActivityAlert(SecurityAlertDto alert) {
        if (!securityAlertNotificationProperties.isEnabled() ||
                !securityAlertNotificationProperties.getSuspiciousActivity().isEnabled()) {
            log.debug("Suspicious activity alerts are disabled");
            return;
        }

        String subject = "🚨 " + appName + " - Suspicious Activity Detected";
        String body = buildSuspiciousActivityAlertBody(alert);

        try {
            sendHtmlEmail(alert.userEmail(), subject, body);
            log.info("Suspicious activity alert sent to: {}", maskEmail(alert.userEmail()));
        } catch (Exception e) {
            log.error("Failed to send suspicious activity alert to: {}", maskEmail(alert.userEmail()), e);
            // Don't throw - security emails should not block login
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendSecurityAlert(SecurityAlertDto alert) {
        if (!securityAlertNotificationProperties.isEnabled()) {
            log.debug("Security emails are disabled");
            return;
        }

        String subject = appName + " - " + alert.alertType().getDisplayName();
        String body = buildGenericSecurityAlertBody(alert);

        try {
            sendHtmlEmail(alert.userEmail(), subject, body);
            log.info("Security alert ({}) sent to: {}", alert.alertType(), maskEmail(alert.userEmail()));
        } catch (Exception e) {
            log.error("Failed to send security alert to: {}", maskEmail(alert.userEmail()), e);
        }
    }

    // ========================================
    // Security Alert Email Templates
    // ========================================

    private String buildNewDeviceAlertBody(SecurityAlertDto alert) {
        var config = securityAlertNotificationProperties.getNewDeviceAlert();
        var links = securityAlertNotificationProperties.getLinks();
        var appearance = securityAlertNotificationProperties.getAppearance();
        int year = java.time.Year.now().getValue();

        StringBuilder detailsHtml = new StringBuilder();

        if (config.isIncludeDeviceDetails()) {
            detailsHtml.append("""
                    <tr>
                        <td style="padding: 8px 15px; border-bottom: 1px solid #eee;"><strong>Device</strong></td>
                        <td style="padding: 8px 15px; border-bottom: 1px solid #eee;">%s</td>
                    </tr>
                    """.formatted(alert.getDeviceDescription()));
        }

        if (config.isIncludeLocation() && alert.country() != null) {
            detailsHtml.append("""
                    <tr>
                        <td style="padding: 8px 15px; border-bottom: 1px solid #eee;"><strong>Location</strong></td>
                        <td style="padding: 8px 15px; border-bottom: 1px solid #eee;">%s</td>
                    </tr>
                    """.formatted(alert.getLocationDescription()));
        }

        detailsHtml.append("""
                <tr>
                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee;"><strong>Time</strong></td>
                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee;">%s</td>
                </tr>
                """.formatted(alert.getFormattedLoginTime()));

        if (config.isIncludeIpAddress() && alert.ipAddress() != null) {
            detailsHtml.append("""
                    <tr>
                        <td style="padding: 8px 15px;"><strong>IP Address</strong></td>
                        <td style="padding: 8px 15px;">%s</td>
                    </tr>
                    """.formatted(alert.getMaskedIpAddress()));
        }

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5; }
                        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                        .header { background-color: %s; color: white; padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 24px; }
                        .icon { font-size: 48px; margin-bottom: 10px; }
                        .content { padding: 30px; }
                        .greeting { font-size: 18px; margin-bottom: 20px; }
                        .message { margin-bottom: 25px; color: #555; }
                        .details-table { width: 100%%; border-collapse: collapse; background-color: #f9f9f9; border-radius: 8px; overflow: hidden; margin: 20px 0; }
                        .button-container { text-align: center; margin: 30px 0; }
                        .button { display: inline-block; background-color: %s; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; font-weight: 600; }
                        .button-secondary { display: inline-block; background-color: #6c757d; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; margin-left: 10px; }
                        .warning-box { background-color: #fff3cd; border: 1px solid #ffc107; border-radius: 8px; padding: 15px; margin: 20px 0; }
                        .warning-box p { margin: 0; color: #856404; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #999; background-color: #f9f9f9; }
                        .footer a { color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="icon">🔔</div>
                            <h1>New Device Login</h1>
                        </div>
                        <div class="content">
                            <p class="greeting">Hi %s,</p>
                            <p class="message">We noticed a new sign-in to your %s account from a device we don't recognize.</p>

                            <table class="details-table">
                                %s
                            </table>

                            <div class="warning-box">
                                <p><strong>Was this you?</strong> If yes, you can safely ignore this email. If no, please secure your account immediately.</p>
                            </div>

                            <div class="button-container">
                                <a href="%s" class="button">Secure My Account</a>
                                <a href="%s" class="button-secondary">View Devices</a>
                            </div>
                        </div>
                        <div class="footer">
                            <p>This is an automated security notification from %s.</p>
                            <p>&copy; %d %s. All rights reserved.</p>
                            <p><a href="%s">Unsubscribe</a> | <a href="%s">Help Center</a></p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        appearance.getPrimaryColor(),
                        appearance.getWarningColor(),
                        alert.userName() != null ? alert.userName() : "there",
                        appName,
                        detailsHtml.toString(),
                        links.getFullUrl(links.getSecureAccountUrl()),
                        links.getFullUrl(links.getLoginHistoryUrl()),
                        appName,
                        year,
                        appearance.getCompanyName(),
                        links.getFullUrl("/account/notifications"),
                        links.getFullUrl("/help"));
    }

    private String buildNewLocationAlertBody(SecurityAlertDto alert) {
        var config = securityAlertNotificationProperties.getNewLocationAlert();
        var links = securityAlertNotificationProperties.getLinks();
        var appearance = securityAlertNotificationProperties.getAppearance();
        int year = java.time.Year.now().getValue();

        String previousLocationHtml = "";
        if (config.isIncludePreviousLocation() && alert.previousCountry() != null) {
            String prevLocation = alert.previousCity() != null ? alert.previousCity() + ", " + alert.previousCountry()
                    : alert.previousCountry();
            previousLocationHtml = """
                    <tr>
                        <td style="padding: 8px 15px; border-bottom: 1px solid #eee;"><strong>Previous Location</strong></td>
                        <td style="padding: 8px 15px; border-bottom: 1px solid #eee;">%s</td>
                    </tr>
                    """
                    .formatted(prevLocation);
        }

        String ipHtml = "";
        if (config.isIncludeIpAddress() && alert.ipAddress() != null) {
            ipHtml = """
                    <tr>
                        <td style="padding: 8px 15px;"><strong>IP Address</strong></td>
                        <td style="padding: 8px 15px;">%s</td>
                    </tr>
                    """.formatted(alert.getMaskedIpAddress());
        }

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5; }
                        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                        .header { background-color: #fd7e14; color: white; padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 24px; }
                        .icon { font-size: 48px; margin-bottom: 10px; }
                        .content { padding: 30px; }
                        .greeting { font-size: 18px; margin-bottom: 20px; }
                        .message { margin-bottom: 25px; color: #555; }
                        .details-table { width: 100%%; border-collapse: collapse; background-color: #f9f9f9; border-radius: 8px; overflow: hidden; margin: 20px 0; }
                        .button-container { text-align: center; margin: 30px 0; }
                        .button { display: inline-block; background-color: %s; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; font-weight: 600; }
                        .button-secondary { display: inline-block; background-color: #6c757d; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; margin-left: 10px; }
                        .alert-box { background-color: #f8d7da; border: 1px solid #f5c6cb; border-radius: 8px; padding: 15px; margin: 20px 0; }
                        .alert-box p { margin: 0; color: #721c24; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #999; background-color: #f9f9f9; }
                        .footer a { color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="icon">⚠️</div>
                            <h1>Login from New Location</h1>
                        </div>
                        <div class="content">
                            <p class="greeting">Hi %s,</p>
                            <p class="message">Your %s account was accessed from a new geographic location.</p>

                            <table class="details-table">
                                <tr>
                                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee;"><strong>New Location</strong></td>
                                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee; color: #fd7e14; font-weight: bold;">%s</td>
                                </tr>
                                %s
                                <tr>
                                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee;"><strong>Device</strong></td>
                                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee;"><strong>Time</strong></td>
                                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee;">%s</td>
                                </tr>
                                %s
                            </table>

                            <div class="alert-box">
                                <p><strong>⚠️ If this wasn't you</strong>, please change your password immediately and review your account activity.</p>
                            </div>

                            <div class="button-container">
                                <a href="%s" class="button">Change Password</a>
                                <a href="%s" class="button-secondary">View Activity</a>
                            </div>
                        </div>
                        <div class="footer">
                            <p>This is an automated security notification from %s.</p>
                            <p>&copy; %d %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        appearance.getWarningColor(),
                        alert.userName() != null ? alert.userName() : "there",
                        appName,
                        alert.getLocationDescription(),
                        previousLocationHtml,
                        alert.getDeviceDescription(),
                        alert.getFormattedLoginTime(),
                        ipHtml,
                        links.getFullUrl(links.getChangePasswordUrl()),
                        links.getFullUrl(links.getLoginHistoryUrl()),
                        appName,
                        year,
                        appearance.getCompanyName());
    }

    private String buildSuspiciousActivityAlertBody(SecurityAlertDto alert) {
        var links = securityAlertNotificationProperties.getLinks();
        var appearance = securityAlertNotificationProperties.getAppearance();
        int year = java.time.Year.now().getValue();

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5; }
                        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                        .header { background-color: %s; color: white; padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 24px; }
                        .icon { font-size: 48px; margin-bottom: 10px; }
                        .content { padding: 30px; }
                        .greeting { font-size: 18px; margin-bottom: 20px; }
                        .message { margin-bottom: 25px; color: #555; }
                        .details-table { width: 100%%; border-collapse: collapse; background-color: #f9f9f9; border-radius: 8px; overflow: hidden; margin: 20px 0; }
                        .button-container { text-align: center; margin: 30px 0; }
                        .button { display: inline-block; background-color: %s; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; font-weight: 600; }
                        .danger-box { background-color: #f8d7da; border: 2px solid %s; border-radius: 8px; padding: 20px; margin: 20px 0; }
                        .danger-box p { margin: 0; color: #721c24; font-weight: 500; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #999; background-color: #f9f9f9; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="icon">🚨</div>
                            <h1>Suspicious Activity Detected</h1>
                        </div>
                        <div class="content">
                            <p class="greeting">Hi %s,</p>
                            <p class="message">We detected unusual activity on your %s account that may indicate unauthorized access.</p>

                            <table class="details-table">
                                <tr>
                                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee;"><strong>Activity Type</strong></td>
                                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee; color: %s; font-weight: bold;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee;"><strong>Location</strong></td>
                                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee;"><strong>Device</strong></td>
                                    <td style="padding: 8px 15px; border-bottom: 1px solid #eee;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px;"><strong>Time</strong></td>
                                    <td style="padding: 8px 15px;">%s</td>
                                </tr>
                            </table>

                            <div class="danger-box">
                                <p>🔒 <strong>We recommend you take immediate action:</strong></p>
                                <p style="margin-top: 10px;">1. Change your password immediately</p>
                                <p>2. Review your recent account activity</p>
                                <p>3. Enable two-factor authentication if not already enabled</p>
                            </div>

                            <div class="button-container">
                                <a href="%s" class="button">Secure My Account Now</a>
                            </div>

                            <p style="color: #666; font-size: 14px; text-align: center;">
                                If this was you, you can ignore this email. Your account is secure.
                            </p>
                        </div>
                        <div class="footer">
                            <p>This is an automated security notification from %s.</p>
                            <p>&copy; %d %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        appearance.getWarningColor(),
                        appearance.getWarningColor(),
                        appearance.getWarningColor(),
                        alert.userName() != null ? alert.userName() : "there",
                        appName,
                        appearance.getWarningColor(),
                        alert.alertType() != null ? alert.alertType().getDisplayName() : "Suspicious Login",
                        alert.getLocationDescription(),
                        alert.getDeviceDescription(),
                        alert.getFormattedLoginTime(),
                        links.getFullUrl(links.getSecureAccountUrl()),
                        appName,
                        year,
                        appearance.getCompanyName());
    }

    private String buildGenericSecurityAlertBody(SecurityAlertDto alert) {
        var links = securityAlertNotificationProperties.getLinks();
        var appearance = securityAlertNotificationProperties.getAppearance();
        int year = java.time.Year.now().getValue();

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: %s; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background-color: #f9f9f9; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                        .button { display: inline-block; background-color: %s; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                        </div>
                        <div class="content">
                            <h2>Security Alert</h2>
                            <p>Hi %s,</p>
                            <p>%s</p>
                            <p><strong>Details:</strong></p>
                            <ul>
                                <li>Device: %s</li>
                                <li>Location: %s</li>
                                <li>Time: %s</li>
                            </ul>
                            <p style="text-align: center; margin-top: 30px;">
                                <a href="%s" class="button">Review Activity</a>
                            </p>
                        </div>
                        <div class="footer">
                            <p>&copy; %d %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        appearance.getPrimaryColor(),
                        appearance.getPrimaryColor(),
                        alert.alertType() != null ? alert.alertType().getDisplayName() : "Security Alert",
                        alert.userName() != null ? alert.userName() : "there",
                        alert.additionalInfo() != null ? alert.additionalInfo()
                                : "A security event was detected on your account.",
                        alert.getDeviceDescription(),
                        alert.getLocationDescription(),
                        alert.getFormattedLoginTime(),
                        links.getFullUrl(links.getSecureAccountUrl()),
                        year,
                        appearance.getCompanyName());
    }
}
