package com.mmva.newsapp.infrastructure.rbac.exception;

/**
 * Exception thrown when a permission is not found.
 */
public class PermissionNotFoundException extends RuntimeException {

    private final String field;
    private final String value;

    public PermissionNotFoundException(String field, String value) {
        super(String.format("Permission not found with %s: %s", field, value));
        this.field = field;
        this.value = value;
    }

    public PermissionNotFoundException(String message) {
        super(message);
        this.field = null;
        this.value = null;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
