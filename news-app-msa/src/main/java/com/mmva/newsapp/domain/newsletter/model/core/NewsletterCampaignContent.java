package com.mmva.newsapp.domain.newsletter.model.core;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Newsletter campaign content entity.
 *
 * <p>
 * Stores multi-language content for newsletter campaigns,
 * enabling bilingual newsletter delivery.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Multi-language support (English/Spanish)</li>
 * <li>HTML and plain text versions</li>
 * <li>Campaign-specific subject lines</li>
 * <li>Version control through audit fields</li>
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
@Table(name = "newsletter_campaign_content", indexes = {
        @Index(name = "idx_newsletter_campaign_content_campaign", columnList = "newsletter_campaign_id"),
        @Index(name = "idx_newsletter_campaign_content_language", columnList = "newsletter_campaign_content_language")
})
public class NewsletterCampaignContent extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "newsletter_campaign_content_id", nullable = false)
    private Long newsletterCampaignContentId;

    // ========================================
    // Relationships
    // ========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "newsletter_campaign_id", nullable = false)
    private NewsletterCampaign newsletterCampaign;

    // ========================================
    // Content Definition
    // ========================================

    @Column(name = "newsletter_campaign_content_language", nullable = false, length = 10)
    private String newsletterCampaignContentLanguage;

    @Column(name = "newsletter_campaign_content_subject", nullable = false, length = 255)
    private String newsletterCampaignContentSubject;

    @Column(name = "newsletter_campaign_content_html", nullable = false, columnDefinition = "TEXT")
    private String newsletterCampaignContentHtml;

    @Column(name = "newsletter_campaign_content_text", columnDefinition = "TEXT")
    private String newsletterCampaignContentText;
}