package com.mmva.newsapp.infrastructure.storage;

/**
 * Exception thrown when storage operations fail.
 * 
 * <p>Used by all StorageProvider implementations for consistent error handling.
 * 
 * @author TheNews Team
 * @since 1.0.0
 */
public class StorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String operation;
    private final String path;
    private final String providerType;

    /**
     * Create a new StorageException.
     *
     * @param message the error message
     */
    public StorageException(String message) {
        super(message);
        this.operation = null;
        this.path = null;
        this.providerType = null;
    }

    /**
     * Create a new StorageException with cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
        this.operation = null;
        this.path = null;
        this.providerType = null;
    }

    /**
     * Create a new StorageException with full context.
     *
     * @param operation the operation that failed (store, retrieve, delete, etc.)
     * @param path the path involved
     * @param providerType the storage provider type
     * @param message the error message
     * @param cause the underlying cause
     */
    public StorageException(String operation, String path, String providerType, 
                           String message, Throwable cause) {
        super(formatMessage(operation, path, providerType, message), cause);
        this.operation = operation;
        this.path = path;
        this.providerType = providerType;
    }

    /**
     * Create a new StorageException with context but no cause.
     *
     * @param operation the operation that failed
     * @param path the path involved
     * @param providerType the storage provider type
     * @param message the error message
     */
    public StorageException(String operation, String path, String providerType, String message) {
        super(formatMessage(operation, path, providerType, message));
        this.operation = operation;
        this.path = path;
        this.providerType = providerType;
    }

    private static String formatMessage(String operation, String path, 
                                        String providerType, String message) {
        return String.format("[%s] Storage operation '%s' failed for path '%s': %s",
                providerType, operation, path, message);
    }

    public String getOperation() {
        return operation;
    }

    public String getPath() {
        return path;
    }

    public String getProviderType() {
        return providerType;
    }
}
