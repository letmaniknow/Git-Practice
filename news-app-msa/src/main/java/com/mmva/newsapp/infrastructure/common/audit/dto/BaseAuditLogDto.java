package com.mmva.newsapp.infrastructure.common.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Base DTO for all audit log responses across all domains (RBAC, News, Push,
 * Admin).
 * 
 * <p>
 * Represents a complete audit log entry with all 24 industry-standard fields.
 * This DTO is reused by all feature-specific audits.
 * </p>
 * 
 * <h2>24 Fields Mapping (WHO, WHAT, WHICH, WHEN, WHERE, WHY, HOW):</h2>
 * 
 * <h3>WHO (Identity - 2 fields)</h3>
 * <ul>
 * <li>{@code actorId} - UUID of user/system that performed the action</li>
 * <li>{@code sessionId} - User session ID for forensics</li>
 * </ul>
 * 
 * <h3>WHAT (Action - 3 fields)</h3>
 * <ul>
 * <li>{@code action} - Action type (ROLE_CREATED, NEWS_PUBLISHED, etc.)</li>
 * <li>{@code domain} - Feature domain (RBAC, NEWS, PUSH, ADMIN)</li>
 * <li>{@code source} - Source of action (REST_API, BATCH, SCHEDULER,
 * SYSTEM)</li>
 * </ul>
 * 
 * <h3>WHICH (Resources - 4 fields)</h3>
 * <ul>
 * <li>{@code resourceId} - Primary resource ID (roleId, newsId, etc.)</li>
 * <li>{@code resourceName} - Human-readable name of resource</li>
 * <li>{@code roleId} - (Domain-specific) Role ID for RBAC audits</li>
 * <li>{@code permissionId} - (Domain-specific) Permission ID for RBAC
 * audits</li>
 * </ul>
 * 
 * <h3>WHEN (Timestamp - 1 field)</h3>
 * <ul>
 * <li>{@code createdAt} - Timestamp of action (auto-set to now)</li>
 * </ul>
 * 
 * <h3>WHERE (Location - 3 fields)</h3>
 * <ul>
 * <li>{@code ipAddress} - Client IP address (IPv4/IPv6)</li>
 * <li>{@code userAgent} - Browser/client user agent string</li>
 * <li>{@code requestUri} - Request path (/api/v1/admin/roles)</li>
 * </ul>
 * 
 * <h3>WHY (Context - 2 fields)</h3>
 * <ul>
 * <li>{@code reason} - Why was this action performed</li>
 * <li>{@code details} - Additional context as JSON</li>
 * </ul>
 * 
 * <h3>HOW (Status & Metrics - 5 fields)</h3>
 * <ul>
 * <li>{@code isSuccess} - Did operation succeed or fail</li>
 * <li>{@code errorMessage} - Error details if failed</li>
 * <li>{@code httpStatus} - HTTP response code</li>
 * <li>{@code requestMethod} - HTTP method (GET, POST, etc.)</li>
 * <li>{@code affectedRows} - For bulk operations: rows affected</li>
 * </ul>
 * 
 * <h3>CORRELATE & RISK (3 fields)</h3>
 * <ul>
 * <li>{@code transactionId} - Multi-step operation correlation ID</li>
 * <li>{@code severity} - Risk level (CRITICAL, HIGH, MEDIUM, LOW)</li>
 * <li>{@code responseTimeMs} - Response time in milliseconds</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseAuditLogDto {

    // ========================================
    // Identity (2 fields)
    // ========================================
    private UUID actorId;
    private String sessionId;

    // ========================================
    // Action (3 fields)
    // ========================================
    private String action;
    private String domain;
    private String source;

    // ========================================
    // Resources (4 fields)
    // ========================================
    private UUID resourceId;
    private String resourceName;
    private UUID roleId; // Domain-specific for RBAC
    private UUID permissionId; // Domain-specific for RBAC

    // ========================================
    // Timestamp (1 field)
    // ========================================
    private Instant createdAt;

    // ========================================
    // Location (3 fields)
    // ========================================
    private String ipAddress;
    private String userAgent;
    private String requestUri;

    // ========================================
    // Context (2 fields)
    // ========================================
    private String reason;
    private String details;

    // ========================================
    // Status & Metrics (5 fields)
    // ========================================
    private Boolean isSuccess;
    private String errorMessage;
    private Integer httpStatus;
    private String requestMethod;
    private Integer affectedRows;

    // ========================================
    // Correlation & Risk (3 fields)
    // ========================================
    private UUID transactionId;
    private String severity;
    private Long responseTimeMs;
}
