package com.mmva.newsapp.infrastructure.common.exception;

import lombok.Getter;

/**
 * Standardized error codes for consistent API error responses.
 *
 * <p>
 * Provides predefined error codes with user-friendly messages that don't expose
 * internal implementation details. Error codes follow the pattern:
 * CATEGORY_XXX where CATEGORY indicates the error domain and XXX is a
 * sequential number.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Getter
public enum ErrorCode {

    // Authentication & Authorization (AUTH_xxx)
    AUTH_INVALID_CREDENTIALS("AUTH_001", "Invalid email or password"),
    AUTH_UNAUTHORIZED("AUTH_002", "Authentication required to access this resource"),
    AUTH_FORBIDDEN("AUTH_003", "You don't have permission to access this resource"),
    AUTH_TOKEN_EXPIRED("AUTH_004", "Your session has expired. Please log in again"),
    AUTH_TOKEN_INVALID("AUTH_005", "Invalid authentication token"),

    // Resource Not Found (RESOURCE_xxx)
    RESOURCE_NOT_FOUND("RESOURCE_001", "The requested item was not found"),
    NEWS_NOT_FOUND("RESOURCE_002", "The news article you're looking for doesn't exist"),
    USER_NOT_FOUND("RESOURCE_003", "The user account was not found"),
    ADMIN_NOT_FOUND("RESOURCE_004", "The administrator account was not found"),
    ROLE_NOT_FOUND("RESOURCE_005", "The role was not found"),
    PERMISSION_NOT_FOUND("RESOURCE_006", "The permission was not found"),
    COMMENT_NOT_FOUND("RESOURCE_007", "The comment was not found"),
    LIKE_NOT_FOUND("RESOURCE_008", "The like was not found"),
    SHARE_NOT_FOUND("RESOURCE_009", "The share was not found"),
    VIEW_NOT_FOUND("RESOURCE_010", "The view record was not found"),

    // Validation Errors (VALIDATION_xxx)
    VALIDATION_ERROR("VALIDATION_001", "Please check your input and try again"),
    VALIDATION_REQUIRED_FIELD("VALIDATION_002", "This field is required"),
    VALIDATION_INVALID_FORMAT("VALIDATION_003", "The provided data format is invalid"),
    VALIDATION_DUPLICATE("VALIDATION_004", "This item already exists"),

    // Business Logic Errors (BUSINESS_xxx)
    BUSINESS_CONFLICT("BUSINESS_001", "This action cannot be completed due to a conflict"),
    BUSINESS_INVALID_OPERATION("BUSINESS_002", "This operation is not allowed"),
    BUSINESS_SUBSCRIPTION_CONFLICT("BUSINESS_003", "Subscription conflict detected"),

    // Rate Limiting (RATE_xxx)
    RATE_LIMIT_EXCEEDED("RATE_001", "Too many requests. Please try again later"),

    // Data Integrity (DATA_xxx)
    DATA_INTEGRITY_VIOLATION("DATA_001", "Data integrity constraint violated"),

    // External Service Errors (EXTERNAL_xxx)
    EXTERNAL_SERVICE_ERROR("EXTERNAL_001", "A service is temporarily unavailable. Please try again later"),

    // System Errors (SYSTEM_xxx)
    SYSTEM_INTERNAL_ERROR("SYSTEM_001", "An unexpected error occurred. Please try again later"),
    SYSTEM_JSON_PARSE_ERROR("SYSTEM_002", "Invalid data format received"),

    // Unknown/Default
    UNKNOWN_ERROR("UNKNOWN_001", "An unexpected error occurred");

    private final String code;
    private final String userMessage;

    ErrorCode(String code, String userMessage) {
        this.code = code;
        this.userMessage = userMessage;
    }

    /**
     * Get ErrorCode by code string.
     *
     * @param code the error code string
     * @return ErrorCode enum or UNKNOWN_ERROR if not found
     */
    public static ErrorCode fromCode(String code) {
        if (code == null) {
            return UNKNOWN_ERROR;
        }
        for (ErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        return UNKNOWN_ERROR;
    }
}