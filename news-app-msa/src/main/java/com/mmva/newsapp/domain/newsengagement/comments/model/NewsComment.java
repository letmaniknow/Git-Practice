package com.mmva.newsapp.domain.newsengagement.comments.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.newsengagement.comments.enums.NewsCommentStatus;
import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;

/**
 * News comment entity for user comments on articles.
 * 
 * <p>
 * Supports threaded comments via parentId, moderation workflow, soft delete,
 * and comprehensive client context for analytics and security.
 * </p>
 * 
 * <p>
 * Soft-delete filtering is handled via {@code SoftDeleteSpec} in repository
 * queries.
 * </p>
 * 
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "news_comments", indexes = {
        @Index(name = "idx_news_comments_deleted_at", columnList = "deleted_at"),
        @Index(name = "idx_news_comments_news_id", columnList = "news_comments_news_id"),
        @Index(name = "idx_news_comments_user_id", columnList = "news_comments_user_id"),
        @Index(name = "idx_news_comments_country_code", columnList = "news_comments_country_code")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NewsComment extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "news_comments_id", updatable = false, nullable = false)
    private UUID newsCommentsId;

    // ========================================
    // Relationships
    // ========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_comments_news_id", referencedColumnName = "news_news_id", nullable = false, insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private NewsMasterEntity news;

    @Column(name = "news_comments_news_id", nullable = false)
    private UUID newsCommentsNewsId;

    @Column(name = "news_comments_user_id")
    private UUID newsCommentsUserId;

    @Column(name = "news_comments_parent_id")
    private UUID newsCommentsParentId;

    // ========================================
    // Content
    // ========================================

    @Column(name = "news_comments_comment", nullable = false, columnDefinition = "TEXT")
    private String newsCommentsComment;

    // ========================================
    // Network Information
    // ========================================

    @Column(name = "news_comments_ip_address", length = 100)
    private String newsCommentsIpAddress;

    // ========================================
    // Device Information
    // ========================================

    /** Device type: MOBILE, TABLET, DESKTOP, TV, WEARABLE, BOT, UNKNOWN */
    @Column(name = "news_comments_device_type", length = 20)
    private String newsCommentsDeviceType;

    /** Device fingerprint for unique device tracking */
    @Column(name = "news_comments_device_fingerprint", length = 128)
    private String newsCommentsDeviceFingerprint;

    /** Browser name: Chrome, Firefox, Safari, Edge, Opera */
    @Column(name = "news_comments_browser_name", length = 50)
    private String newsCommentsBrowserName;

    /** Operating system name: Windows, macOS, iOS, Android, Linux */
    @Column(name = "news_comments_os_name", length = 50)
    private String newsCommentsOsName;

    // ========================================
    // Location Information
    // ========================================

    /** ISO 3166-1 alpha-2 country code (e.g., US, GB, IN) */
    @Column(name = "news_comments_country_code", length = 5)
    private String newsCommentsCountryCode;

    /** City name from GeoIP */
    @Column(name = "news_comments_city", length = 100)
    private String newsCommentsCity;

    // ========================================
    // Security Context
    // ========================================

    /** Whether the request appears to be from a bot */
    @Builder.Default
    @Column(name = "news_comments_is_bot")
    private Boolean newsCommentsIsBot = false;

    /** Whether using VPN/Proxy/TOR (useful for spam detection) */
    @Builder.Default
    @Column(name = "news_comments_is_anonymized")
    private Boolean newsCommentsIsAnonymized = false;

    /** Risk score 0-100 (for spam/abuse prevention) */
    @Column(name = "news_comments_risk_score")
    private Integer newsCommentsRiskScore;

    // ========================================
    // Request Context
    // ========================================

    /** Access channel: WEB, MOBILE_WEB, IOS_APP, ANDROID_APP, API */
    @Column(name = "news_comments_channel", length = 20)
    private String newsCommentsChannel;

    /** User's preferred language (from Accept-Language) */
    @Column(name = "news_comments_language", length = 10)
    private String newsCommentsLanguage;

    // ========================================
    // Timestamps
    // ========================================

    @Column(name = "news_comments_commented_at")
    private Instant newsCommentsCommentedAt;

    // ========================================
    // Status
    // ========================================

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "news_comments_status", length = 20, nullable = false)
    private NewsCommentStatus newsCommentsStatus = NewsCommentStatus.APPROVED;
}
