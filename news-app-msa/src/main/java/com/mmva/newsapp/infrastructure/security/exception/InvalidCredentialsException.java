package com.mmva.newsapp.infrastructure.security.exception;

/**
 * Exception thrown when login credentials are invalid.
 * 
 * <p>
 * Thrown during authentication when:
 * </p>
 * <ul>
 * <li>Username/email does not exist</li>
 * <li>Password does not match</li>
 * <li>Account is locked or disabled</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid username/email or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
