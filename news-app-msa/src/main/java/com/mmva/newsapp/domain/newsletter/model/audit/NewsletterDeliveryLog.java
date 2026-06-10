package com.mmva.newsapp.domain.newsletter.model.audit;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterDeliveryStatus;
import com.mmva.newsapp.domain.newsletter.model.core.NewsletterCampaign;
import com.mmva.newsapp.domain.newsletter.model.core.NewsletterSubscriber;
import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Newsletter delivery log entity.
 *
 * <p>
 * Tracks the delivery status and engagement metrics for individual
 * newsletter sends to subscribers.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Complete delivery lifecycle tracking</li>
 * <li>Engagement metrics (opens, clicks)</li>
 * <li>Error tracking and bounce handling</li>
 * <li>Provider message ID correlation</li>
 * <li>Analytics foundation for campaign performance</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@SuperBuilder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "newsletter_delivery_log", indexes = {
        @Index(name = "idx_newsletter_delivery_log_campaign", columnList = "newsletter_campaign_id"),
        @Index(name = "idx_newsletter_delivery_log_subscriber", columnList = "newsletter_subscriber_id"),
        @Index(name = "idx_newsletter_delivery_log_status", columnList = "newsletter_delivery_log_status"),
        @Index(name = "idx_newsletter_delivery_log_sent_at", columnList = "newsletter_delivery_log_sent_at")
})
public class NewsletterDeliveryLog extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "newsletter_delivery_log_id", nullable = false)
    private Long newsletterDeliveryLogId;

    // ========================================
    // Relationships
    // ========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "newsletter_campaign_id", nullable = false)
    private NewsletterCampaign newsletterCampaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "newsletter_subscriber_id", nullable = false)
    private NewsletterSubscriber newsletterSubscriber;

    // ========================================
    // Delivery Status
    // ========================================

    @Enumerated(EnumType.STRING)
    @Column(name = "newsletter_delivery_log_status", nullable = false, length = 20)
    private NewsletterDeliveryStatus newsletterDeliveryLogStatus;

    // ========================================
    // Delivery Timestamps
    // ========================================

    @Column(name = "newsletter_delivery_log_sent_at")
    private Instant newsletterDeliveryLogSentAt;

    @Column(name = "newsletter_delivery_log_delivered_at")
    private Instant newsletterDeliveryLogDeliveredAt;

    @Column(name = "newsletter_delivery_log_opened_at")
    private Instant newsletterDeliveryLogOpenedAt;

    @Column(name = "newsletter_delivery_log_clicked_at")
    private Instant newsletterDeliveryLogClickedAt;

    @Column(name = "newsletter_delivery_log_bounced_at")
    private Instant newsletterDeliveryLogBouncedAt;

    // ========================================
    // Error Handling
    // ========================================

    @Column(name = "newsletter_delivery_log_error_message", columnDefinition = "TEXT")
    private String newsletterDeliveryLogErrorMessage;

    @Column(name = "newsletter_delivery_log_provider_message_id", length = 255)
    private String newsletterDeliveryLogProviderMessageId;

    // ========================================
    // Tracking Information
    // ========================================

    @Column(name = "newsletter_delivery_log_user_agent", length = 500)
    private String newsletterDeliveryLogUserAgent;

    @Column(name = "newsletter_delivery_log_ip_address", length = 45)
    private String newsletterDeliveryLogIpAddress;

    @Column(name = "newsletter_delivery_log_clicked_url", length = 2000)
    private String newsletterDeliveryLogClickedUrl;

    // ========================================
    // Bounce & Failure Information
    // ========================================

    @Column(name = "newsletter_delivery_log_bounce_reason", columnDefinition = "TEXT")
    private String newsletterDeliveryLogBounceReason;

    @Column(name = "newsletter_delivery_log_failed_at")
    private Instant newsletterDeliveryLogFailedAt;

    @Column(name = "newsletter_delivery_log_failure_reason", columnDefinition = "TEXT")
    private String newsletterDeliveryLogFailureReason;
}