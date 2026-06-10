package com.mmva.newsapp.infrastructure.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response structure for all API errors.
 * 
 * <p>
 * Provides consistent error format across all endpoints with standardized error
 * codes. Follows RFC 7807 (Problem Details for HTTP APIs) specification.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response (RFC 7807 compliant)")
public class ErrorResponse {

    @Schema(description = "Timestamp when error occurred", example = "2025-12-30T22:50:00Z")
    private String timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type/category", example = "Validation Error")
    private String error;

    @Schema(description = "Machine-readable error code for client handling", example = "VALIDATION_001")
    private String code;

    @Schema(description = "Human-readable error message", example = "Validation failed. See fieldErrors for details")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/v1/admin/news")
    private String path;

    @Schema(description = "Field-level validation errors (optional)", example = "{\"newsContent\": \"Content must be at least 100 characters\", \"newsTitle\": \"Title is required\"}")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> fieldErrors;

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.error = error;
        this.code = ErrorCode.UNKNOWN_ERROR.getCode();
        this.message = message;
        this.path = path;
        this.fieldErrors = null;
    }

    public ErrorResponse(int status, String error, ErrorCode errorCode, String path) {
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.error = error;
        this.code = errorCode.getCode();
        this.message = errorCode.getUserMessage();
        this.path = path;
        this.fieldErrors = null;
    }

    public ErrorResponse(int status, String error, ErrorCode errorCode, String customMessage, String path) {
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.error = error;
        this.code = errorCode.getCode();
        this.message = customMessage != null ? customMessage : errorCode.getUserMessage();
        this.path = path;
        this.fieldErrors = null;
    }

    /**
     * Create ErrorResponse with field-level validation errors (RFC 7807 compliant)
     */
    public static ErrorResponse withFieldErrors(int status, String error, ErrorCode errorCode,
            String message, String path, Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .status(status)
                .error(error)
                .code(errorCode.getCode())
                .message(message)
                .path(path)
                .fieldErrors(fieldErrors)
                .build();
    }

    /**
     * @deprecated Use ErrorCode enum constructors or builder pattern instead
     */
    @Deprecated
    private String toErrorCode(String error) {
        if (error == null)
            return "UNKNOWN_ERROR";
        return error.toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");
    }
}