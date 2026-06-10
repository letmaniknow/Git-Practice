package com.mmva.newsapp.infrastructure.rbac.exception;

/**
 * Exception thrown when a role is not found.
 */
public class RoleNotFoundException extends RuntimeException {

    private final String field;
    private final String value;

    public RoleNotFoundException(String field, String value) {
        super(String.format("Role not found with %s: %s", field, value));
        this.field = field;
        this.value = value;
    }

    public RoleNotFoundException(String message) {
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
