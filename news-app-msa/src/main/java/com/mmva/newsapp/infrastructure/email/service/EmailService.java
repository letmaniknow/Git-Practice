package com.mmva.newsapp.infrastructure.email.service;

import com.mmva.newsapp.infrastructure.security.dto.SecurityAlertDto;

/**
 * Interface for email sending operations.
 * 
 * <p>
 * Supports various email types:
 * </p>
 * <ul>
 * <li>Verification codes</li>
 * <li>Password reset</li>
 * <li>Welcome emails</li>
 * <li>Security alerts (new device, new location)</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface EmailService {

    /**
     * Sends an email verification code to the specified email address.
     *
     * @param to               Recipient email address
     * @param verificationCode The verification code to send
     */
    void sendVerificationCode(String to, String verificationCode);

    /**
     * Sends a password reset email to the specified email address.
     *
     * @param to        Recipient email address
     * @param resetLink The password reset link or token
     */
    void sendPasswordResetEmail(String to, String resetLink);

    /**
     * Sends a generic email.
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param body    Email body (can be HTML)
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Sends a welcome email to a newly registered admindashboard.
     *
     * @param to       Recipient email address
     * @param username The admindashboard's username
     */
    void sendWelcomeEmail(String to, String username);

    // ========================================
    // Security Alert Emails
    // ========================================

    /**
     * Sends a new device login alert to the user.
     * 
     * <p>
     * Notifies the user when their account is accessed from an
     * unrecognized device for the first time.
     * </p>
     *
     * @param alert Security alert details including device and location info
     */
    void sendNewDeviceLoginAlert(SecurityAlertDto alert);

    /**
     * Sends a new location login alert to the user.
     * 
     * <p>
     * Notifies the user when their account is accessed from a
     * new geographic location.
     * </p>
     *
     * @param alert Security alert details including new and previous location
     */
    void sendNewLocationLoginAlert(SecurityAlertDto alert);

    /**
     * Sends a suspicious activity alert to the user.
     * 
     * <p>
     * Notifies the user when suspicious login activity is detected,
     * such as high risk score, multiple failed attempts, or VPN usage.
     * </p>
     *
     * @param alert Security alert details with risk information
     */
    void sendSuspiciousActivityAlert(SecurityAlertDto alert);

    /**
     * Sends a generic security alert email.
     * 
     * <p>
     * Can be used for custom security notifications.
     * </p>
     *
     * @param alert Security alert details
     */
    void sendSecurityAlert(SecurityAlertDto alert);
}
