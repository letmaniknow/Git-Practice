package com.mmva.newsapp.infrastructure.common.audit.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Base mapped superclass for all audit log entities (append-only).
 * 
 * <p>
 * Provides common audit fields to avoid duplication across feature-specific
 * audit tables (rbac_audit_log, news_audit_log, push_audit_log,
 * admin_audit_log).
 * 
 * <strong>This is a @MappedSuperclass - NOT an @Entity.</strong> Child classes
 * (RbacAuditLog, NewsAuditLog, etc.) must be @Entity with @Table annotations.
 * 
 * Audit log records are immutable (append-only) - never updated or deleted
 * after creation.
 * All logic is in Java - no database triggers or stored procedures.
 * JPA/Hibernate handles table creation and index management automatically.
 * </p>
 * 
 * <h2>24 Industry-Standard Fields (WHO, WHAT, WHICH, WHEN, WHERE, WHY,
 * HOW)</h2>
 * 
 * <h3>WHO (Identity - 3 fields)</h3>
 * <ul>
 * <li>{@code actorId} - UUID of user/system that performed the action</li>
 * <li>{@code actorDisplayName} - Human-readable name (email, full name, or
 * system name)</li>
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
 * <h3>WHICH (Resources - 2 fields)</h3>
 * <ul>
 * <li>{@code resourceId} - Primary resource ID being acted upon</li>
 * <li>{@code resourceName} - Human-readable name of resource</li>
 * </ul>
 * <p>
 * <strong>Note:</strong> Feature-specific fields (roleId, newsId, permissionId,
 * etc.)
 * are added in concrete entity classes implementing {@code DomainAuditLog}
 * interface.
 * </p>
 * 
 * <h3>WHEN (Timestamp - 1 field)</h3>
 * <ul>
 * <li>{@code createdAt} - Auto-set to now via Spring Data Auditing</li>
 * </ul>
 * 
 * <h3>WHERE (Location - 3 fields)</h3>
 * <ul>
 * <li>{@code ipAddress} - Client IP address (IPv4/IPv6)</li>
 * <li>{@code userAgent} - Browser/client user agent</li>
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
 * <li>{@code affectedRows} - For bulk operations</li>
 * </ul>
 * 
 * <h3>CORRELATE & RISK (3 fields)</h3>
 * <ul>
 * <li>{@code transactionId} - Multi-step operation correlation</li>
 * <li>{@code severity} - Risk level (CRITICAL, HIGH, MEDIUM, LOW)</li>
 * <li>{@code responseTimeMs} - Response time in milliseconds</li>
 * </ul>
 * 
 * <h2>Child Implementations (Feature-Specific):</h2>
 * 
 * <pre>
 * // RBAC Audit Table
 * &#64;Entity
 * &#64;Table(name = "rbac_audit_log", indexes = {
 *         &#64;Index(name = "idx_rbac_audit_created_at", columnList = "created_at"),
 *         &#64;Index(name = "idx_rbac_audit_actor_id", columnList = "actor_id"),
 *         &#64;Index(name = "idx_rbac_audit_action", columnList = "action"),
 *         &#64;Index(name = "idx_rbac_audit_domain", columnList = "domain")
 * })
 * public class RbacAuditLog extends BaseAuditLogEntity {
 *     &#64;Id
 *     &#64;GeneratedValue(strategy = GenerationType.IDENTITY)
 *     private Long id;
 * }
 * 
 * // News Audit Table
 * &#64;Entity
 * &#64;Table(name = "news_audit_log")
 * public class NewsAuditLog extends BaseAuditLogEntity {
 *     &#64;Id
 *     &#64;GeneratedValue(strategy = GenerationType.IDENTITY)
 *     private Long id;
 * }
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditLogEntity {

    // ========================================
    // WHO (Identity - 3 fields)
    // ========================================

    @Column(name = "actor_id")
    private UUID actorId; // User/system that performed the action

    @Column(name = "actor_display_name", length = 255)
    private String actorDisplayName; // Human-readable name (email, full name, or system name)

    @Column(name = "session_id", length = 128)
    private String sessionId; // User session ID for forensics

    // ========================================
    // WHAT (Action - 3 fields)
    // ========================================

    @Column(name = "action", nullable = false, length = 100)
    private String action; // Action type (ROLE_CREATED, NEWS_PUBLISHED, PUSH_SENT, etc.)

    @Column(name = "domain", length = 100)
    private String domain; // Feature domain (RBAC, NEWS, PUSH, ADMIN)

    @Column(name = "source", length = 100)
    private String source; // Source of action (REST_API, BATCH, SCHEDULER, SYSTEM)

    // ========================================
    // WHICH (Resources - 4 fields)
    // ========================================

    @Column(name = "resource_id")
    private UUID resourceId; // Primary resource ID (roleId, newsId, campaignId, etc.)

    @Column(name = "resource_name", length = 255)
    private String resourceName; // Human-readable name (role name, article title, etc.)

    // ========================================
    // WHEN (Timestamp - 1 field)
    // ========================================

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt; // Auto-set to now via Spring Data Auditing

    // ========================================
    // WHERE (Location - 3 fields)
    // ========================================

    @Column(name = "ip_address", length = 45)
    private String ipAddress; // Client IP address (IPv4: 15 chars, IPv6: 45 chars)

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent; // Browser/client user agent string

    @Column(name = "request_uri", columnDefinition = "TEXT")
    private String requestUri; // Request path (/api/v1/admin/roles, etc.)

    // ========================================
    // WHY (Context - 2 fields)
    // ========================================

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason; // Why was this action performed (optional)

    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // Additional context as JSON

    // ========================================
    // HOW (Status & Metrics - 5 fields)
    // ========================================

    @Column(name = "is_success")
    private Boolean isSuccess; // Did operation succeed or fail

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // Error details if isSuccess=false

    @Column(name = "http_status")
    private Integer httpStatus; // HTTP response code (200, 201, 400, 403, 500, etc.)

    @Column(name = "request_method", length = 10)
    private String requestMethod; // HTTP method (GET, POST, PUT, DELETE, PATCH)

    @Column(name = "affected_rows")
    private Integer affectedRows; // For bulk operations: how many records affected

    // ========================================
    // CORRELATE & RISK (3 fields)
    // ========================================

    @Column(name = "transaction_id")
    private UUID transactionId; // Multi-step operation correlation ID

    @Builder.Default
    @Column(name = "severity", length = 10)
    private String severity = "MEDIUM"; // Risk level (CRITICAL, HIGH, MEDIUM, LOW)

    @Column(name = "response_time_ms")
    private Long responseTimeMs; // Response time in milliseconds
}
