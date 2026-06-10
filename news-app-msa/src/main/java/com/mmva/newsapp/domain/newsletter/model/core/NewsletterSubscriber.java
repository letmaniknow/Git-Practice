package com.mmva.newsapp.domain.newsletter.model.core;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterSubscriptionStatus;
import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Newsletter subscriber entity.
 *
 * <p>
 * Represents a subscriber to the newsletter system with preferences,
 * subscription status, and engagement tracking.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Links to existing users or anonymous subscribers</li>
 * <li>Multi-language support (English/Spanish)</li>
 * <li>Subscription status management</li>
 * <li>Interest-based segmentation</li>
 * <li>GDPR compliance with unsubscribe tracking</li>
 * <li>Source attribution for subscriber acquisition</li>
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
@Table(name = "newsletter_subscriber", indexes = {
        @Index(name = "idx_newsletter_subscriber_email", columnList = "newsletter_subscriber_email"),
        @Index(name = "idx_newsletter_subscriber_user_id", columnList = "user_id"),
        @Index(name = "idx_newsletter_subscriber_status", columnList = "newsletter_subscriber_subscription_status"),
        @Index(name = "idx_newsletter_subscriber_language", columnList = "newsletter_subscriber_preferred_language"),
        @Index(name = "idx_newsletter_subscriber_created_at", columnList = "created_at")
})
public class NewsletterSubscriber extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "newsletter_subscriber_id", nullable = false)
    private Long newsletterSubscriberId;

    // ========================================
    // User Association
    // ========================================

    @Column(name = "user_id")
    private UUID userId;

    // ========================================
    // Subscriber Information
    // ========================================

    @Column(name = "newsletter_subscriber_email", nullable = false, unique = true, length = 255)
    private String newsletterSubscriberEmail;

    @Column(name = "newsletter_subscriber_first_name", length = 100)
    private String newsletterSubscriberFirstName;

    @Column(name = "newsletter_subscriber_last_name", length = 100)
    private String newsletterSubscriberLastName;

    @Column(name = "newsletter_subscriber_preferred_language", nullable = false, length = 10)
    @Builder.Default
    private String newsletterSubscriberPreferredLanguage = "en";

    @Enumerated(EnumType.STRING)
    @Column(name = "newsletter_subscriber_subscription_status", nullable = false, length = 20)
    @Builder.Default
    private NewsletterSubscriptionStatus newsletterSubscriberSubscriptionStatus = NewsletterSubscriptionStatus.ACTIVE;

    // ========================================
    // Confirmation & Verification
    // ========================================

    @Column(name = "newsletter_subscriber_confirmation_token")
    private UUID newsletterSubscriberConfirmationToken;

    // ========================================
    // Preferences & Segmentation
    // ========================================

    @Column(name = "newsletter_subscriber_interests", columnDefinition = "TEXT")
    private String newsletterSubscriberInterests; // JSON array of interests

    // ========================================
    // Acquisition Tracking
    // ========================================

    @Column(name = "newsletter_subscriber_source", length = 50)
    private String newsletterSubscriberSource;

    @Column(name = "newsletter_subscriber_ip_address", length = 45)
    private String newsletterSubscriberIpAddress;

    @Column(name = "newsletter_subscriber_user_agent", length = 500)
    private String newsletterSubscriberUserAgent;

    // ========================================
    // Status Timestamps
    // ========================================

    @Column(name = "newsletter_subscriber_confirmed_at")
    private Instant newsletterSubscriberConfirmedAt;

    @Column(name = "newsletter_subscriber_unsubscribed_at")
    private Instant newsletterSubscriberUnsubscribedAt;
}