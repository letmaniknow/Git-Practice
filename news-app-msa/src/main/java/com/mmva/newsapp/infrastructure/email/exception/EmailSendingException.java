package com.mmva.newsapp.infrastructure.email.exception;

/**
 * Exception thrown when email sending fails.
 * 
 * <p>
 * This exception is thrown when the email service encounters
 * an error while sending emails (SMTP failures, connection issues, etc.).
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class EmailSendingException extends RuntimeException {

    public EmailSendingException(String message) {
        super(message);
    }

    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
