package com.mmva.newsapp.domain.newsletter.repository.audit;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterUnsubscribeReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mmva.newsapp.domain.newsletter.model.core.NewsletterCampaign;
import com.mmva.newsapp.domain.newsletter.model.core.NewsletterSubscriber;
import com.mmva.newsapp.domain.newsletter.model.audit.NewsletterUnsubscribe;

import java.time.Instant;
import java.util.List;

/**
 * Repository for NewsletterUnsubscribe entity operations.
 *
 * <p>
 * Provides data access methods for unsubscribe tracking and analytics
 * with support for GDPR compliance and user engagement insights.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsletterUnsubscribeRepository
                extends JpaRepository<NewsletterUnsubscribe, Long>, JpaSpecificationExecutor<NewsletterUnsubscribe> {

        /**
         * Finds unsubscribes for a specific subscriber.
         *
         * @param subscriber the subscriber entity
         * @param pageable   pagination information
         * @return page of unsubscribes
         */
        Page<NewsletterUnsubscribe> findByNewsletterSubscriber(NewsletterSubscriber subscriber, Pageable pageable);

        /**
         * Finds unsubscribes for a specific subscriber by subscriber ID.
         *
         * @param subscriberId the subscriber ID
         * @param pageable     pagination information
         * @return page of unsubscribes
         */
        Page<NewsletterUnsubscribe> findByNewsletterSubscriber_NewsletterSubscriberId(Long subscriberId,
                        Pageable pageable);

        /**
         * Finds unsubscribes by reason.
         *
         * @param reason   the unsubscribe reason
         * @param pageable pagination information
         * @return page of unsubscribes
         */
        Page<NewsletterUnsubscribe> findByNewsletterUnsubscribeReason(NewsletterUnsubscribeReason reason,
                        Pageable pageable);

        /**
         * Counts unsubscribes by campaign.
         *
         * @param campaignId the campaign ID
         * @return count of unsubscribes
         */
        long countByNewsletterCampaign_NewsletterCampaignId(Long campaignId);

        /**
         * Counts unsubscribes by reason for a campaign.
         *
         * @param campaignId the campaign ID
         * @param reason     the unsubscribe reason
         * @return count of unsubscribes
         */
        long countByNewsletterCampaign_NewsletterCampaignIdAndNewsletterUnsubscribeReason(
                        Long campaignId, NewsletterUnsubscribeReason reason);

        /**
         * Finds unsubscribes within a time range.
         *
         * @param startTime the start timestamp
         * @param endTime   the end timestamp
         * @param pageable  pagination information
         * @return page of unsubscribes
         */
        Page<NewsletterUnsubscribe> findByNewsletterUnsubscribeCreatedAtBetween(Instant startTime, Instant endTime,
                        Pageable pageable);

        /**
         * Checks if a subscriber has unsubscribed from a specific campaign.
         *
         * @param subscriberId the subscriber ID
         * @param campaignId   the campaign ID
         * @return true if unsubscribed, false otherwise
         */
        boolean existsByNewsletterSubscriber_NewsletterSubscriberIdAndNewsletterCampaign_NewsletterCampaignId(
                        Long subscriberId, Long campaignId);

        /**
         * Finds recent unsubscribes for analytics.
         *
         * @param since timestamp to look back from
         * @return list of recent unsubscribes
         */
        List<NewsletterUnsubscribe> findByNewsletterUnsubscribeCreatedAtAfter(Instant since);

        /**
         * Calculates unsubscribe rate for a campaign.
         *
         * @param campaignId the campaign ID
         * @return unsubscribe rate as percentage (0-100)
         */
        @Query("SELECT CASE WHEN (SELECT COUNT(d) FROM NewsletterDeliveryLog d WHERE d.newsletterCampaign.newsletterCampaignId = :campaignId) > 0 THEN "
                        +
                        "(CAST(COUNT(u) AS DOUBLE) / (SELECT COUNT(d) FROM NewsletterDeliveryLog d WHERE d.newsletterCampaign.newsletterCampaignId = :campaignId)) * 100 "
                        +
                        "ELSE 0 END " +
                        "FROM NewsletterUnsubscribe u WHERE u.newsletterCampaign.newsletterCampaignId = :campaignId")
        Double calculateUnsubscribeRateForCampaign(@Param("campaignId") Long campaignId);

        /**
         * Gets unsubscribe statistics by reason for a campaign.
         *
         * @param campaignId the campaign ID
         * @return array with counts by reason: [total, irrelevant_content,
         *         too_frequent, spam, other]
         */
        @Query("SELECT " +
                        "COUNT(u), " +
                        "COUNT(CASE WHEN u.newsletterUnsubscribeReason = 'IRRELEVANT_CONTENT' THEN 1 END), " +
                        "COUNT(CASE WHEN u.newsletterUnsubscribeReason = 'TOO_FREQUENT' THEN 1 END), " +
                        "COUNT(CASE WHEN u.newsletterUnsubscribeReason = 'SPAM' THEN 1 END), " +
                        "COUNT(CASE WHEN u.newsletterUnsubscribeReason = 'OTHER' THEN 1 END) " +
                        "FROM NewsletterUnsubscribe u WHERE u.newsletterCampaign.newsletterCampaignId = :campaignId")
        Object[] getUnsubscribeStatisticsByCampaignId(@Param("campaignId") Long campaignId);

        /**
         * Finds subscribers who unsubscribed after opening a campaign.
         *
         * @param campaignId the campaign ID
         * @return list of unsubscribes after opening
         */
        @Query("SELECT u FROM NewsletterUnsubscribe u " +
                        "WHERE u.newsletterCampaign.newsletterCampaignId = :campaignId " +
                        "AND EXISTS (SELECT d FROM NewsletterDeliveryLog d WHERE d.newsletterSubscriber = u.newsletterSubscriber "
                        +
                        "AND d.newsletterCampaign = u.newsletterCampaign AND d.newsletterDeliveryLogStatus IN ('OPENED', 'CLICKED'))")
        List<NewsletterUnsubscribe> findUnsubscribesAfterOpeningByCampaignId(@Param("campaignId") Long campaignId);

        /**
         * Gets overall unsubscribe rate across all campaigns within a time period.
         *
         * @param startTime the start timestamp
         * @param endTime   the end timestamp
         * @return overall unsubscribe rate as percentage (0-100)
         */
        @Query("SELECT CASE WHEN (SELECT COUNT(d) FROM NewsletterDeliveryLog d WHERE d.createdAt BETWEEN :startTime AND :endTime) > 0 THEN "
                        +
                        "(CAST(COUNT(u) AS DOUBLE) / (SELECT COUNT(d) FROM NewsletterDeliveryLog d WHERE d.createdAt BETWEEN :startTime AND :endTime)) * 100 "
                        +
                        "ELSE 0 END " +
                        "FROM NewsletterUnsubscribe u WHERE u.newsletterUnsubscribeCreatedAt BETWEEN :startTime AND :endTime")
        Double calculateOverallUnsubscribeRate(@Param("startTime") Instant startTime,
                        @Param("endTime") Instant endTime);
}