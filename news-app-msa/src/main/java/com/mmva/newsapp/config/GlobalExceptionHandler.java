package com.mmva.newsapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.apache.catalina.connector.ClientAbortException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mmva.newsapp.domain.adminuser.exception.core.AdminNotFoundException;
import com.mmva.newsapp.domain.appuser.exception.core.AppUserNotFoundException;
import com.mmva.newsapp.domain.news.exception.core.NewsNotFoundException;
import com.mmva.newsapp.domain.newsengagement.comments.exception.NewsCommentNotFoundException;
import com.mmva.newsapp.domain.newsengagement.likes.exception.NewsLikeNotFoundException;
import com.mmva.newsapp.domain.newsengagement.shares.exception.NewsShareNotFoundException;
import com.mmva.newsapp.domain.newsengagement.views.exception.NewsViewNotFoundException;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.common.exception.DuplicateResourceException;
import com.mmva.newsapp.infrastructure.common.exception.ErrorCode;
import com.mmva.newsapp.infrastructure.common.exception.ErrorResponse;
import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;
import com.mmva.newsapp.infrastructure.common.exception.ConstraintViolationMapper;
import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import com.mmva.newsapp.infrastructure.common.api.dto.SubscriptionConflictData;
import com.mmva.newsapp.infrastructure.common.exception.ActiveSubscriptionExistsException;
import com.mmva.newsapp.infrastructure.common.exception.UnauthorizedAccessException;
import com.mmva.newsapp.infrastructure.rbac.exception.PermissionNotFoundException;
import com.mmva.newsapp.infrastructure.rbac.exception.RoleNotFoundException;
import com.mmva.newsapp.infrastructure.requestanalytics.exception.RateLimitExceededException;
import com.mmva.newsapp.infrastructure.security.exception.InvalidCredentialsException;

import jakarta.servlet.http.HttpServletRequest;

import java.util.stream.Collectors;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Global exception handler for the entire application.
 * 
 * This is app-level wiring that coordinates error handling across ALL modules.
 * Located in config/ package per PROJECT_PRINCIPLES.md:
 * "config/ (root) = ONLY app-level wiring (WebConfig, OpenApiConfig)"
 * 
 * This handler is ALLOWED to import domain-specific exceptions because
 * app-level configuration needs domain knowledge to wire everything together.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

        private final ConstraintViolationMapper constraintViolationMapper;

        public GlobalExceptionHandler(ConstraintViolationMapper constraintViolationMapper) {
                this.constraintViolationMapper = constraintViolationMapper;
        }

        // =========================
        // Custom Exceptions
        // =========================

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleResourceNotFoundException(
                        ResourceNotFoundException ex, HttpServletRequest request) {
                log.warn("Resource not found: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Resource Not Found",
                                ErrorCode.RESOURCE_NOT_FOUND,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildErrorResponse("error", "Resource not found", error));
        }

        @ExceptionHandler(NewsNotFoundException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleNewsNotFoundException(
                        NewsNotFoundException ex, HttpServletRequest request) {
                log.warn("News not found: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "News Not Found",
                                ErrorCode.NEWS_NOT_FOUND,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildErrorResponse("error", "News article not found", error));
        }

        @ExceptionHandler(AdminNotFoundException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleAdminNotFoundException(
                        AdminNotFoundException ex, HttpServletRequest request) {
                log.warn("Admin not found: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Admin Not Found",
                                ErrorCode.ADMIN_NOT_FOUND,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildErrorResponse("error", "Administrator not found", error));
        }

        @ExceptionHandler(AppUserNotFoundException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleUserNotFoundException(
                        AppUserNotFoundException ex, HttpServletRequest request) {
                log.warn("User not found: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "User Not Found",
                                ErrorCode.USER_NOT_FOUND,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildErrorResponse("error", "User not found", error));
        }

        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleInvalidCredentialsException(
                        InvalidCredentialsException ex, HttpServletRequest request) {
                log.warn("Invalid credentials: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Authentication Error",
                                ErrorCode.AUTH_INVALID_CREDENTIALS,
                                request.getRequestURI());
                // Use the actual exception message (could be "Account is deactivated", "Wrong
                // password", etc.)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(buildErrorResponse("error", ex.getMessage(), error));
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleDuplicateResourceException(
                        DuplicateResourceException ex, HttpServletRequest request) {
                log.warn("Duplicate resource: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                "Duplicate Resource",
                                ErrorCode.VALIDATION_DUPLICATE,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(buildErrorResponse("error", "Resource already exists", error));
        }

        @ExceptionHandler(UnauthorizedAccessException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleUnauthorizedAccessException(
                        UnauthorizedAccessException ex, HttpServletRequest request) {
                log.warn("Unauthorized access: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                "Authorization Error",
                                ErrorCode.AUTH_FORBIDDEN,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(buildErrorResponse("error", "Access denied", error));
        }

        @ExceptionHandler(InvalidRequestException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleInvalidRequestException(
                        InvalidRequestException ex, HttpServletRequest request) {
                log.warn("Invalid request: {}", ex.getMessage());

                // Extract field-level errors if present (RFC 7807 compliance)
                if (ex.getFieldErrors() != null && !ex.getFieldErrors().isEmpty()) {
                        ErrorResponse error = ErrorResponse.withFieldErrors(
                                        HttpStatus.BAD_REQUEST.value(),
                                        "Validation Error",
                                        ErrorCode.VALIDATION_ERROR,
                                        ex.getMessage(),
                                        request.getRequestURI(),
                                        ex.getFieldErrors());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(buildErrorResponse("error", ex.getMessage(), error));
                } else {
                        // Fallback to generic error response if no field errors
                        ErrorResponse error = new ErrorResponse(
                                        HttpStatus.BAD_REQUEST.value(),
                                        "Invalid Request",
                                        ErrorCode.VALIDATION_ERROR,
                                        ex.getMessage(),
                                        request.getRequestURI());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(buildErrorResponse("error", ex.getMessage(), error));
                }
        }

        @ExceptionHandler(RateLimitExceededException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleRateLimitExceededException(
                        RateLimitExceededException ex, HttpServletRequest request) {
                log.warn("Rate limit exceeded: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.TOO_MANY_REQUESTS.value(),
                                "Rate Limit Exceeded",
                                ErrorCode.RATE_LIMIT_EXCEEDED,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                                .body(buildErrorResponse("error", "Rate limit exceeded", error));
        }

        @ExceptionHandler(java.util.NoSuchElementException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleNoSuchElementException(
                        java.util.NoSuchElementException ex, HttpServletRequest request) {
                log.warn("Element not found: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                ErrorCode.RESOURCE_NOT_FOUND,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildErrorResponse("error", "Item not found", error));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleIllegalArgumentException(
                        IllegalArgumentException ex, HttpServletRequest request) {
                log.warn("Invalid argument: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid Argument",
                                ErrorCode.VALIDATION_INVALID_FORMAT,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildErrorResponse("error", "Invalid input provided", error));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleIllegalStateException(
                        IllegalStateException ex, HttpServletRequest request) {
                log.warn("Illegal state: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                "Operation Not Allowed",
                                ErrorCode.BUSINESS_INVALID_OPERATION,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(buildErrorResponse("error", "Operation not allowed", error));
        }

        @ExceptionHandler(ActiveSubscriptionExistsException.class)
        public ResponseEntity<ApiResponseDto<SubscriptionConflictData>> handleActiveSubscriptionExistsException(
                        ActiveSubscriptionExistsException ex, HttpServletRequest request) {
                log.warn("Subscription conflict: {}", ex.getMessage());

                SubscriptionConflictData conflictData = SubscriptionConflictData.builder()
                                .existingSubscription(ex.getExistingSubscription())
                                .availableActions(ex.getAvailableActions())
                                .suggestion(ex.getMessage())
                                .build();

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponseDto.<SubscriptionConflictData>builder()
                                                .status("conflict")
                                                .message("Subscription conflict detected")
                                                .timestamp(java.time.Instant.now().toString())
                                                .data(conflictData)
                                                .build());
        }

        @ExceptionHandler(NewsCommentNotFoundException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleNewsCommentNotFoundException(
                        NewsCommentNotFoundException ex, HttpServletRequest request) {
                log.warn("News comment not found: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "News Comment Not Found",
                                ErrorCode.COMMENT_NOT_FOUND,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildErrorResponse("error", "Comment not found", error));
        }

        @ExceptionHandler(NewsLikeNotFoundException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleNewsLikeNotFoundException(
                        NewsLikeNotFoundException ex, HttpServletRequest request) {
                log.warn("News like not found: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "News Like Not Found",
                                ErrorCode.LIKE_NOT_FOUND,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildErrorResponse("error", "Like not found", error));
        }

        @ExceptionHandler(NewsShareNotFoundException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleNewsShareNotFoundException(
                        NewsShareNotFoundException ex, HttpServletRequest request) {
                log.warn("News share not found: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "News Share Not Found",
                                ErrorCode.SHARE_NOT_FOUND,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildErrorResponse("error", "Share not found", error));
        }

        @ExceptionHandler(NewsViewNotFoundException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleNewsViewNotFoundException(
                        NewsViewNotFoundException ex, HttpServletRequest request) {
                log.warn("News view not found: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "News View Not Found",
                                ErrorCode.VIEW_NOT_FOUND,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildErrorResponse("error", "View record not found", error));
        }

        // =========================
        // Spring/Framework Exceptions
        // =========================

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleNoResourceFoundException(
                        NoResourceFoundException ex, HttpServletRequest request) {
                // Silently ignore favicon.ico - just return 404 without logging
                if (request.getRequestURI().contains("favicon.ico")) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
                log.debug("Static resource not found: {}", request.getRequestURI());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Resource Not Found",
                                ErrorCode.RESOURCE_NOT_FOUND,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildErrorResponse("error", "Resource not found", error));
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleDataIntegrityViolation(
                        DataIntegrityViolationException ex, HttpServletRequest request) {
                String message = ex.getMessage();
                Map<String, String> fieldErrors = new LinkedHashMap<>();

                // DEBUG LOGGING
                log.error("🔴 DataIntegrityViolationException caught!");
                log.error("   Raw message: {}", message);
                log.error("   Root cause: {}", ex.getCause());

                // INDUSTRY STANDARD: Use generalized mapper to extract field from ANY
                // constraint
                // This works for all entities, not just news
                String field = constraintViolationMapper.extractFieldFromError(message);
                log.error("   Extracted field: {}", field);

                String userMessage = constraintViolationMapper.generateUserMessage(field, message);
                log.error("   Generated message: {}", userMessage);

                // Map constraint to field for inline error display
                fieldErrors.put(field, userMessage);

                log.warn("Data integrity violation on field '{}': {}", field, ex.getMessage());

                // Send as field-level error (like validation errors) so it displays inline
                ErrorResponse error = ErrorResponse.withFieldErrors(
                                HttpStatus.CONFLICT.value(),
                                "Data Integrity Violation",
                                ErrorCode.DATA_INTEGRITY_VIOLATION,
                                userMessage,
                                request.getRequestURI(),
                                fieldErrors);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(buildErrorResponse("error", userMessage, error));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleValidationException(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {
                // Extract field-level errors as Map<fieldName, errorMessage>
                Map<String, String> fieldErrors = new LinkedHashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

                ErrorResponse error = ErrorResponse.withFieldErrors(
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Error",
                                ErrorCode.VALIDATION_ERROR,
                                "Validation failed. See fieldErrors for details.",
                                request.getRequestURI(),
                                fieldErrors);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildErrorResponse("error", "Validation failed", error));
        }

        @ExceptionHandler(RoleNotFoundException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleRoleNotFoundException(
                        RoleNotFoundException ex, HttpServletRequest request) {
                log.warn("Role not found: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Role Not Found",
                                ErrorCode.ROLE_NOT_FOUND,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildErrorResponse("error", "Role not found", error));
        }

        @ExceptionHandler(PermissionNotFoundException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handlePermissionNotFoundException(
                        PermissionNotFoundException ex, HttpServletRequest request) {
                log.warn("Permission not found: {}", ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Permission Not Found",
                                ErrorCode.PERMISSION_NOT_FOUND,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildErrorResponse("error", "Permission not found", error));
        }

        /**
         * Handler for HttpMessageNotWritableException (serialization failures).
         * 
         * <p>
         * This occurs when an exception happens on a file/stream endpoint
         * (Content-Type: video/mp4)
         * and the exception handler tries to return JSON. Spring cannot write JSON to a
         * response
         * already marked as video/mp4.
         * </p>
         * 
         * <p>
         * <strong>Root Cause:</strong>
         * Request to file endpoint → Exception occurs → Headers already set to
         * video/mp4
         * → Exception handler tries to return ApiResponseDto (JSON) → Converter
         * conflict
         * </p>
         * 
         * <p>
         * <strong>Solution:</strong> Return error response with explicit Content-Type
         * reset.
         * </p>
         */
        @ExceptionHandler(HttpMessageNotWritableException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleHttpMessageNotWritableException(
                        HttpMessageNotWritableException ex, HttpServletRequest request) {

                log.warn("Message serialization failed (likely due to content-type mismatch): {} - "
                                + "Request may have already set incompatible Content-Type (e.g., video/mp4). "
                                + "Exception details: {}",
                                ex.getMessage(), ex.getCause() != null ? ex.getCause().getMessage() : "unknown");

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Serialization Error",
                                ErrorCode.SYSTEM_INTERNAL_ERROR,
                                request.getRequestURI());

                // Explicitly set content-type to override any previously set content-types
                // This ensures the error response can be properly serialized as JSON
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .header("Content-Type", "application/json")
                                .body(buildErrorResponse("error", "Unable to process response format", error));
        }

        @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleOptimisticLockingFailure(
                        org.springframework.orm.ObjectOptimisticLockingFailureException ex,
                        HttpServletRequest request) {
                log.warn("Optimistic locking failure: {}", ex.getMessage());
                String userMessage = "This news item was updated by another user. Please reload and try again.";
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                "Update Conflict",
                                ErrorCode.BUSINESS_CONFLICT,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(buildErrorResponse("error", userMessage, error));
        }

        /**
         * Handler for ClientAbortException (client disconnection at Tomcat level).
         * 
         * <p>
         * This is the root cause exception that wraps the IOException when the client
         * disconnects. Occurs at the Tomcat/servlet engine level before Spring can
         * even process the response.
         * </p>
         * 
         * <p>
         * <strong>Scenario:</strong>
         * User requests file → Download is slow → User gets impatient and closes
         * browser
         * → Tomcat tries to write more data → Connection already closed →
         * ClientAbortException
         * </p>
         * 
         * <p>
         * <strong>Solution:</strong> Just log at DEBUG, don't treat as error.
         * </p>
         */
        @ExceptionHandler(ClientAbortException.class)
        public ResponseEntity<Void> handleClientAbortException(ClientAbortException ex) {

                log.debug("Client aborted connection (normal behavior). "
                                + "Likely causes: timeout, user closed browser, network interrupted. "
                                + "Details: {}", ex.getMessage());

                // Return empty response - connection is already closed
                return ResponseEntity.ok().build();
        }

        /**
         * Handler for AsyncRequestNotUsableException (client disconnection).
         * 
         * <p>
         * This occurs when a client disconnects (timeout, user cancels, network issue)
         * while the server is still trying to write the response. The server initially
         * starts writing but realizes midway that the client is gone.
         * </p>
         * 
         * <p>
         * <strong>Root Cause:</strong>
         * 1. Request to file/video endpoint
         * 2. Processing takes too long
         * 3. Client disconnects (timeout, user cancels)
         * 4. Server realizes connection is closed
         * 5. ServletOutputStream fails to write
         * 6. AsyncRequestNotUsableException thrown
         * </p>
         * 
         * <p>
         * <strong>Why Not Log as ERROR:</strong>
         * This is NOT actually an error - it's expected client behavior (impatient
         * users,
         * network timeouts, etc). Logging as ERROR would create false-positive alerts
         * and noise in production logs.
         * </p>
         * 
         * <p>
         * <strong>Solution:</strong> Log at DEBUG level only, don't propagate error.
         * Return ResponseEntity.ok() to satisfy Spring (won't write to closed
         * connection anyway).
         * </p>
         */
        @ExceptionHandler(AsyncRequestNotUsableException.class)
        public ResponseEntity<Void> handleAsyncRequestNotUsableException(
                        AsyncRequestNotUsableException ex) {

                // Only log at DEBUG level - this is expected client behavior, not an error
                log.debug("Client disconnected before response could be written (normal behavior). "
                                + "Likely causes: request timeout, user cancellation, network issue. "
                                + "Details: {}", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());

                // Return empty response - the client is already gone anyway
                // This satisfies Spring's requirement to return ResponseEntity
                return ResponseEntity.ok().build();
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleException(
                        Exception ex, HttpServletRequest request) {

                // Let specific handler deal with optimistic locking
                if (ex instanceof org.springframework.orm.ObjectOptimisticLockingFailureException) {
                        throw (org.springframework.orm.ObjectOptimisticLockingFailureException) ex;
                }

                log.error("Unhandled exception occurred", ex);

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                ErrorCode.SYSTEM_INTERNAL_ERROR,
                                request.getRequestURI());

                // Build response with explicit JSON content type to override any previously set
                // content type
                // This prevents "No converter for ApiResponseDto with preset Content-Type
                // 'video/mp4'" errors
                // when exceptions occur on file/stream endpoints
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .header("Content-Type", "application/json")
                                .body(buildErrorResponse("error", "An unexpected error occurred", error));
        }

        @ExceptionHandler(JsonProcessingException.class)
        public ResponseEntity<ApiResponseDto<ErrorResponse>> handleJsonProcessingException(
                        JsonProcessingException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "JSON Parse Error",
                                ErrorCode.SYSTEM_JSON_PARSE_ERROR,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildErrorResponse("error", "Invalid data format received", error));
        }

        private ApiResponseDto<ErrorResponse> buildErrorResponse(String status, String message, ErrorResponse error) {
                return ApiResponseDto.<ErrorResponse>builder()
                                .status(status)
                                .message(message)
                                .timestamp(java.time.Instant.now().toString())
                                .data(error)
                                .build();
        }
}
