package com.mmva.newsapp.domain.newsengagement.comments.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * News comment report entity for abuse reporting.
 * 
 * <p>
 * Tracks user reports on comments with client context for moderation.
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
@Table(name = "news_comment_reports", indexes = {
        @Index(name = "idx_news_comment_reports_comment_id", columnList = "news_comment_reports_comment_id"),
        @Index(name = "idx_news_comment_reports_user_id", columnList = "news_comment_reports_user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsCommentReport {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "news_comment_reports_id", updatable = false, nullable = false)
    private UUID newsCommentReportsId;

    // ========================================
    // Relationships
    // ========================================

    @Column(name = "news_comment_reports_comment_id", nullable = false)
    private UUID newsCommentReportsCommentId;

    @Column(name = "news_comment_reports_user_id", nullable = false)
    private UUID newsCommentReportsUserId;

    // ========================================
    // Report Content
    // ========================================

    @Column(name = "news_comment_reports_reason", length = 255)
    private String newsCommentReportsReason;

    // ========================================
    // Client Context (for abuse pattern detection)
    // ========================================

    /** IP address of reporter */
    @Column(name = "news_comment_reports_ip_address", length = 100)
    private String newsCommentReportsIpAddress;

    /** Device type: MOBILE, TABLET, DESKTOP */
    @Column(name = "news_comment_reports_device_type", length = 20)
    private String newsCommentReportsDeviceType;

    /** ISO 3166-1 alpha-2 country code */
    @Column(name = "news_comment_reports_country_code", length = 5)
    private String newsCommentReportsCountryCode;

    /** Access channel: WEB, MOBILE_WEB, IOS_APP, ANDROID_APP */
    @Column(name = "news_comment_reports_channel", length = 20)
    private String newsCommentReportsChannel;

    // ========================================
    // Timestamp
    // ========================================

    @Column(name = "news_comment_reports_reported_at")
    private Instant newsCommentReportsReportedAt;
}
