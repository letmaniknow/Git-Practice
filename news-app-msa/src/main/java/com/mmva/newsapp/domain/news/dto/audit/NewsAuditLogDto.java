package com.mmva.newsapp.domain.news.dto.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for News Audit Log responses.
 * 
 * <h3>Usage:</h3>
 * Returned when querying audit logs. Includes all 24 industry-standard audit
 * fields
 * from BaseAuditLogEntity plus domain-specific newsId field.
 * 
 * <h3>Field Categories:</h3>
 * <ul>
 * <li><strong>WHO:</strong> actorId, actorDisplayName, sessionId - Identity of
 * actor</li>
 * <li><strong>WHAT:</strong> action, domain, source - What action was
 * performed</li>
 * <li><strong>WHICH:</strong> resourceId, resourceName, newsId - What was
 * affected</li>
 * <li><strong>WHEN:</strong> createdAt - When it happened</li>
 * <li><strong>WHERE:</strong> ipAddress, userAgent, requestUri - Where
 * from</li>
 * <li><strong>WHY:</strong> reason, details - Why and context</li>
 * <li><strong>HOW:</strong> isSuccess, errorMessage, httpStatus, requestMethod,
 * affectedRows - Result</li>
 * <li><strong>RISK:</strong> transactionId, severity, responseTimeMs -
 * Correlation and performance</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "News Audit Log - Complete audit trail entry with 24 industry-standard fields")
public class NewsAuditLogDto {

    // ========================================
    // IDENTIFICATION (1 field)
    // ========================================

    @Schema(description = "Unique audit log entry ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    // ========================================
    // WHO (Identity - 3 fields)
    // ========================================

    @Schema(description = "UUID of user/system that performed the action", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID actorId;

    @Schema(description = "Human-readable name of actor for UI display (email, full name, or system name)", example = "maria.garcia@news.com")
    private String actorDisplayName;

    @Schema(description = "User session ID for forensics", example = "sess_abc123xyz789")
    private String sessionId;

    // ========================================
    // WHAT (Action - 3 fields)
    // ========================================

    @Schema(description = "Action type performed", example = "NEWS_PUBLISHED", allowableValues = { "CREATED",
            "PUBLISHED", "UPDATED", "DELETED", "ARCHIVED", "RESTORED", "SCHEDULED_PUBLISHED" })
    private String action;

    @Schema(description = "Feature domain where action occurred", example = "NEWS", allowableValues = { "RBAC", "NEWS",
            "PUSH", "ADMIN" })
    private String domain;

    @Schema(description = "Source of action", example = "REST_API", allowableValues = { "REST_API", "BATCH",
            "SCHEDULER", "SYSTEM" })
    private String source;

    // ========================================
    // WHICH (Resources - 3 fields)
    // ========================================

    @Schema(description = "Generic resource ID being acted upon", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID resourceId;

    @Schema(description = "Human-readable name of the resource", example = "Breaking News Article")
    private String resourceName;

    @Schema(description = "Domain-specific: The news article UUID", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID newsId;

    // ========================================
    // WHEN (Timestamp - 1 field)
    // ========================================

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Timestamp when action was performed (auto-set by system)", example = "2026-05-10T10:30:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant createdAt;

    // ========================================
    // WHERE (Location - 3 fields)
    // ========================================

    @Schema(description = "Client IP address (IPv4 or IPv6)", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "Browser/client user agent", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    private String userAgent;

    @Schema(description = "Request path", example = "/api/v1/admin/news/publish")
    private String requestUri;

    // ========================================
    // WHY (Context - 2 fields)
    // ========================================

    @Schema(description = "Why was this action performed", example = "Manual publication by editor")
    private String reason;

    @Schema(description = "Additional context as JSON", example = "{\"previousStatus\": \"DRAFT\", \"publishedChannels\": [\"WEB\", \"MOBILE\"]}")
    private String details;

    // ========================================
    // HOW (Status & Metrics - 5 fields)
    // ========================================

    @Schema(description = "Whether operation succeeded or failed", example = "true")
    private Boolean isSuccess;

    @Schema(description = "Error message if operation failed", example = "Article validation failed: title is required")
    private String errorMessage;

    @Schema(description = "HTTP response status code", example = "200")
    private Integer httpStatus;

    @Schema(description = "HTTP request method", example = "POST", allowableValues = { "GET", "POST", "PUT", "DELETE",
            "PATCH" })
    private String requestMethod;

    @Schema(description = "Number of rows affected (for bulk operations)", example = "1")
    private Integer affectedRows;

    // ========================================
    // CORRELATE & RISK (3 fields)
    // ========================================

    @Schema(description = "Transaction ID for multi-step operation correlation", example = "txn_550e8400-e29b-41d4-a716-446655440003")
    private UUID transactionId;

    @Schema(description = "Risk severity level", example = "MEDIUM", allowableValues = { "CRITICAL", "HIGH", "MEDIUM",
            "LOW" })
    private String severity;

    @Schema(description = "Response time in milliseconds", example = "245")
    private Long responseTimeMs;
}
