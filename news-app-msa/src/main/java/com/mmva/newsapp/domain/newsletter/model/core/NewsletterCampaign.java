package com.mmva.newsapp.domain.newsletter.model.core;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterCampaignStatus;
import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterCampaignType;
import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Newsletter campaign entity.
 *
 * <p>
 * Represents a newsletter campaign with scheduling, content, targeting,
 * and delivery analytics.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Campaign lifecycle management (draft → scheduled → sent)</li>
 * <li>Multiple campaign types (regular, welcome, re-engagement,
 * promotional)</li>
 * <li>Audience targeting with JSON-based criteria</li>
 * <li>Delivery analytics and engagement metrics</li>
 * <li>Automated scheduling support</li>
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
@Table(name = "newsletter_campaign", indexes = {
        @Index(name = "idx_newsletter_campaign_status", columnList = "newsletter_campaign_status"),
        @Index(name = "idx_newsletter_campaign_type", columnList = "newsletter_campaign_type"),
        @Index(name = "idx_newsletter_campaign_scheduled_at", columnList = "newsletter_campaign_scheduled_at"),
        @Index(name = "idx_newsletter_campaign_sent_at", columnList = "newsletter_campaign_sent_at")
})
public class NewsletterCampaign extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "newsletter_campaign_id", nullable = false)
    private Long newsletterCampaignId;

    // ========================================
    // Campaign Definition
    // ========================================

    @Column(name = "newsletter_campaign_name", nullable = false, length = 255)
    private String newsletterCampaignName;

    @Column(name = "newsletter_campaign_subject", nullable = false, length = 255)
    private String newsletterCampaignSubject;

    @Column(name = "newsletter_campaign_description", columnDefinition = "TEXT")
    private String newsletterCampaignDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "newsletter_campaign_status", nullable = false, length = 20)
    @Builder.Default
    private NewsletterCampaignStatus newsletterCampaignStatus = NewsletterCampaignStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "newsletter_campaign_type", nullable = false, length = 20)
    @Builder.Default
    private NewsletterCampaignType newsletterCampaignType = NewsletterCampaignType.REGULAR;

    // ========================================
    // Scheduling
    // ========================================

    @Column(name = "newsletter_campaign_scheduled_at")
    private Instant newsletterCampaignScheduledAt;

    @Column(name = "newsletter_campaign_sent_at")
    private Instant newsletterCampaignSentAt;

    // ========================================
    // Targeting & Analytics
    // ========================================

    @Column(name = "newsletter_campaign_target_audience", columnDefinition = "TEXT")
    private String newsletterCampaignTargetAudience; // JSON criteria for targeting

    @Column(name = "newsletter_campaign_total_recipients")
    private Integer newsletterCampaignTotalRecipients;

    @Column(name = "newsletter_campaign_successful_deliveries")
    private Integer newsletterCampaignSuccessfulDeliveries;

    @Column(name = "newsletter_campaign_failed_deliveries")
    private Integer newsletterCampaignFailedDeliveries;

    @Column(name = "newsletter_campaign_open_rate", precision = 5, scale = 2)
    private BigDecimal newsletterCampaignOpenRate;

    @Column(name = "newsletter_campaign_click_rate", precision = 5, scale = 2)
    private BigDecimal newsletterCampaignClickRate;
}