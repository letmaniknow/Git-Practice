package com.mmva.newsapp.infrastructure.external.client;

/**
 * Custom exception for external API failures.
 *
 * <p>
 * Provides structured error handling for external API integrations:
 * </p>
 * <ul>
 * <li>Wraps RestClientException with business context</li>
 * <li>Supports error categorization</li>
 * <li>Enables proper error propagation</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public class ExternalApiException extends RuntimeException {

    private final String serviceName;
    private final String operation;
    private final int statusCode;

    /**
     * Creates a new external API exception.
     *
     * @param message error message
     * @param cause   underlying cause
     */
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
        this.serviceName = "External API";
        this.operation = "Unknown";
        this.statusCode = 0;
    }

    /**
     * Creates a new external API exception with full context.
     *
     * @param serviceName service that failed
     * @param operation   operation that failed
     * @param statusCode  HTTP status code
     * @param message     error message
     * @param cause       underlying cause
     */
    public ExternalApiException(String serviceName, String operation, int statusCode,
            String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.operation = operation;
        this.statusCode = statusCode;
    }

    /**
     * Creates a new external API exception with context.
     *
     * @param serviceName service that failed
     * @param operation   operation that failed
     * @param message     error message
     * @param cause       underlying cause
     */
    public ExternalApiException(String serviceName, String operation, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.operation = operation;
        this.statusCode = 0;
    }

    // Getters
    public String getServiceName() {
        return serviceName;
    }

    public String getOperation() {
        return operation;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return String.format("ExternalApiException{service='%s', operation='%s', statusCode=%d, message='%s'}",
                serviceName, operation, statusCode, getMessage());
    }
}