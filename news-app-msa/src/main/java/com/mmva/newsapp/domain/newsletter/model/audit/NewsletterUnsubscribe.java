package com.mmva.newsapp.domain.newsletter.model.audit;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterUnsubscribeReason;
import com.mmva.newsapp.domain.newsletter.model.core.NewsletterCampaign;
import com.mmva.newsapp.domain.newsletter.model.core.NewsletterSubscriber;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Newsletter unsubscribe entity.
 *
 * <p>
 * Records unsubscribe actions with reasons and feedback for
 * GDPR compliance and analytics.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Unsubscribe reason tracking</li>
 * <li>User feedback collection</li>
 * <li>GDPR compliance with detailed audit trail</li>
 * <li>Source tracking for unsubscribe analysis</li>
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
@EqualsAndHashCode
@Table(name = "newsletter_unsubscribe", indexes = {
        @Index(name = "idx_newsletter_unsubscribe_subscriber", columnList = "newsletter_subscriber_id"),
        @Index(name = "idx_newsletter_unsubscribe_created_at", columnList = "newsletter_unsubscribe_created_at")
})
public class NewsletterUnsubscribe {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "newsletter_unsubscribe_id", nullable = false)
    private Long newsletterUnsubscribeId;

    // ========================================
    // Relationships
    // ========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "newsletter_subscriber_id", nullable = false)
    private NewsletterSubscriber newsletterSubscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "newsletter_campaign_id")
    private NewsletterCampaign newsletterCampaign;

    // ========================================
    // Unsubscribe Details
    // ========================================

    @Enumerated(EnumType.STRING)
    @Column(name = "newsletter_unsubscribe_reason", length = 50)
    private NewsletterUnsubscribeReason newsletterUnsubscribeReason;

    @Column(name = "newsletter_unsubscribe_feedback", columnDefinition = "TEXT")
    private String newsletterUnsubscribeFeedback;

    // ========================================
    // Tracking Information
    // ========================================

    @Column(name = "newsletter_unsubscribe_ip_address", length = 45)
    private String newsletterUnsubscribeIpAddress;

    @Column(name = "newsletter_unsubscribe_user_agent", length = 500)
    private String newsletterUnsubscribeUserAgent;

    @Column(name = "newsletter_unsubscribe_created_at", nullable = false)
    private Instant newsletterUnsubscribeCreatedAt;
}