package com.mmva.newsapp.infrastructure.rbac.config.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * RbacConfigCache Entity - Stores runtime RBAC configuration
 * 
 * Purpose:
 * - Store RBAC configuration in database for runtime access
 * - Support multi-server synchronization without redeployment
 * - Enable feature flags and settings management
 * - Allow zero-downtime configuration updates
 * 
 * Examples:
 * - RBAC_CONFIG_VERSION: Tracks version for multi-server sync
 * - RBAC_CACHE_REFRESH_INTERVAL_MS: Customize cache refresh interval
 * - AUDIT_LOG_ENABLED: Enable/disable audit logging
 * - PERMISSION_STRICT_MODE: Fail on unknown permissions
 * - NEWS_SCHEDULER_ENABLED: Enable news scheduler
 * - NEWS_SCHEDULER_INTERVAL_MS: Scheduler run interval
 * 
 * Design:
 * - Key-value pairs for flexibility
 * - config_type helps with parsing (INT, STRING, BOOLEAN)
 * - config_is_active allows soft-disabling without deletion
 * - Audit fields track who made changes
 * 
 * Note:
 * - Not extending BaseAuditEntity to keep it simple
 * - Audit fields are explicit for clarity
 */
@Entity
@Table(name = "rbac_config_cache", indexes = {
        @Index(name = "idx_rbac_config_key", columnList = "config_key"),
        @Index(name = "idx_rbac_config_active", columnList = "config_is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "createdAt", "updatedAt" })
public class RbacConfigCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Configuration key - UPPERCASE_SNAKE_CASE
     * Examples: RBAC_CONFIG_VERSION, AUDIT_LOG_ENABLED, NEWS_SCHEDULER_INTERVAL_MS
     */
    @Column(unique = true, nullable = false, length = 100)
    private String configKey;

    /**
     * Configuration value - stored as string for flexibility
     * Use configType to determine how to parse
     */
    @Column(nullable = false, length = 500)
    private String configValue;

    /**
     * Configuration type for parsing
     * Values: INT, STRING, BOOLEAN
     */
    @Column(length = 20)
    private String configType = "STRING";

    /**
     * Documentation about this configuration
     */
    @Column(length = 500)
    private String configDescription;

    /**
     * Whether this configuration is active
     * Allows soft-disabling without deletion
     */
    @Column(nullable = false)
    private Boolean configIsActive = true;

    /**
     * Timestamp when created
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when last updated
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Admin user email who created this config
     */
    @Column(length = 255)
    private String createdBy;

    /**
     * Admin user email who last updated this config
     */
    @Column(length = 255)
    private String updatedBy;

    /**
     * Constructor with key and value
     */
    public RbacConfigCache(String configKey, String configValue, String configType) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.configType = configType;
        this.configIsActive = true;
    }

    @Override
    public String toString() {
        return "RbacConfigCache{" +
                "id=" + id +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                ", configType='" + configType + '\'' +
                ", configIsActive=" + configIsActive +
                '}';
    }
}
