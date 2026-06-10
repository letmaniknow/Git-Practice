package com.mmva.newsapp.domain.appuser.model.core;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity for tracking user sessions.
 * 
 * <p>
 * Table: app_users_sessions
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_users_sessions", indexes = {
        @Index(name = "idx_app_users_sessions_user_id", columnList = "app_users_sessions_user_id"),
        @Index(name = "idx_app_users_sessions_token", columnList = "app_users_sessions_token"),
        @Index(name = "idx_app_users_sessions_is_active", columnList = "app_users_sessions_is_active"),
        @Index(name = "idx_app_users_sessions_expires_at", columnList = "app_users_sessions_expires_at")
})
public class AppUserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "app_users_sessions_id", nullable = false)
    private UUID appUsersSessionsId;

    @Column(name = "app_users_sessions_user_id", nullable = false)
    private UUID appUsersSessionsUserId;

    @Column(name = "app_users_sessions_device_info", length = 500)
    private String appUsersSessionsDeviceInfo;

    @Column(name = "app_users_sessions_ip_address", length = 50)
    private String appUsersSessionsIpAddress;

    @Column(name = "app_users_sessions_user_agent", length = 500)
    private String appUsersSessionsUserAgent;

    @Column(name = "app_users_sessions_location", length = 255)
    private String appUsersSessionsLocation;

    @Column(name = "app_users_sessions_created_at")
    private Instant appUsersSessionsCreatedAt;

    @Column(name = "app_users_sessions_last_activity_at")
    private Instant appUsersSessionsLastActivityAt;

    @Column(name = "app_users_sessions_expires_at")
    private Instant appUsersSessionsExpiresAt;

    @Column(name = "app_users_sessions_is_active")
    @Builder.Default
    private Boolean appUsersSessionsIsActive = true;

    @Column(name = "app_users_sessions_token", length = 500)
    private String appUsersSessionsToken;
}
