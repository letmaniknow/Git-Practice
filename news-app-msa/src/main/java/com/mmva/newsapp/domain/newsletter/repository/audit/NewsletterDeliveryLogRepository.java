package com.mmva.newsapp.domain.newsletter.repository.audit;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterDeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mmva.newsapp.domain.newsletter.model.core.NewsletterCampaign;
import com.mmva.newsapp.domain.newsletter.model.audit.NewsletterDeliveryLog;
import com.mmva.newsapp.domain.newsletter.model.core.NewsletterSubscriber;

import java.time.Instant;
import java.util.List;

/**
 * Repository for NewsletterDeliveryLog entity operations.
 *
 * <p>
 * Provides data access methods for newsletter delivery tracking
 * with support for analytics, engagement metrics, and reporting.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsletterDeliveryLogRepository
                extends JpaRepository<NewsletterDeliveryLog, Long>, JpaSpecificationExecutor<NewsletterDeliveryLog> {

        /**
         * Finds delivery logs for a specific campaign.
         *
         * @param campaign the campaign entity
         * @param pageable pagination information
         * @return page of delivery logs
         */
        Page<NewsletterDeliveryLog> findByNewsletterCampaign(NewsletterCampaign campaign, Pageable pageable);

        /**
         * Finds delivery logs for a specific campaign by campaign ID.
         *
         * @param campaignId the campaign ID
         * @param pageable   pagination information
         * @return page of delivery logs
         */
        Page<NewsletterDeliveryLog> findByNewsletterCampaign_NewsletterCampaignId(Long campaignId, Pageable pageable);

        /**
         * Finds delivery logs for a specific subscriber.
         *
         * @param subscriber the subscriber entity
         * @param pageable   pagination information
         * @return page of delivery logs
         */
        Page<NewsletterDeliveryLog> findByNewsletterSubscriber(NewsletterSubscriber subscriber, Pageable pageable);

        /**
         * Finds delivery logs for a specific subscriber by subscriber ID.
         *
         * @param subscriberId the subscriber ID
         * @param pageable     pagination information
         * @return page of delivery logs
         */
        Page<NewsletterDeliveryLog> findByNewsletterSubscriber_NewsletterSubscriberId(Long subscriberId,
                        Pageable pageable);

        /**
         * Finds delivery logs by campaign ID and subscriber ID.
         *
         * @param campaignId   the campaign ID
         * @param subscriberId the subscriber ID
         * @return list of delivery logs
         */
        List<NewsletterDeliveryLog> findByNewsletterCampaign_NewsletterCampaignIdAndNewsletterSubscriber_NewsletterSubscriberId(
                        Long campaignId, Long subscriberId);

        /**
         * Finds delivery logs by status.
         *
         * @param status   the delivery status
         * @param pageable pagination information
         * @return page of delivery logs
         */
        Page<NewsletterDeliveryLog> findByNewsletterDeliveryLogStatus(NewsletterDeliveryStatus status,
                        Pageable pageable);

        /**
         * Counts delivery logs by campaign and status.
         *
         * @param campaignId the campaign ID
         * @param status     the delivery status
         * @return count of delivery logs
         */
        long countByNewsletterCampaign_NewsletterCampaignIdAndNewsletterDeliveryLogStatus(
                        Long campaignId, NewsletterDeliveryStatus status);

        /**
         * Finds delivery logs within a time range.
         *
         * @param startTime the start timestamp
         * @param endTime   the end timestamp
         * @param pageable  pagination information
         * @return page of delivery logs
         */
        Page<NewsletterDeliveryLog> findByCreatedAtBetween(Instant startTime, Instant endTime, Pageable pageable);

        /**
         * Calculates open rate for a campaign.
         *
         * @param campaignId the campaign ID
         * @return open rate as percentage (0-100)
         */
        @Query("SELECT CASE WHEN COUNT(d) > 0 THEN " +
                        "(CAST(COUNT(CASE WHEN d.newsletterDeliveryLogStatus IN ('OPENED', 'CLICKED') THEN 1 END) AS DOUBLE) / COUNT(d)) * 100 "
                        +
                        "ELSE 0 END " +
                        "FROM NewsletterDeliveryLog d WHERE d.newsletterCampaign.newsletterCampaignId = :campaignId")
        Double calculateOpenRateForCampaign(@Param("campaignId") Long campaignId);

        /**
         * Calculates click rate for a campaign.
         *
         * @param campaignId the campaign ID
         * @return click rate as percentage (0-100)
         */
        @Query("SELECT CASE WHEN COUNT(d) > 0 THEN " +
                        "(CAST(COUNT(CASE WHEN d.newsletterDeliveryLogStatus = 'CLICKED' THEN 1 END) AS DOUBLE) / COUNT(d)) * 100 "
                        +
                        "ELSE 0 END " +
                        "FROM NewsletterDeliveryLog d WHERE d.newsletterCampaign.newsletterCampaignId = :campaignId")
        Double calculateClickRateForCampaign(@Param("campaignId") Long campaignId);

        /**
         * Finds bounced deliveries for a campaign.
         *
         * @param campaignId the campaign ID
         * @return list of bounced delivery logs
         */
        @Query("SELECT d FROM NewsletterDeliveryLog d WHERE d.newsletterCampaign.newsletterCampaignId = :campaignId " +
                        "AND d.newsletterDeliveryLogStatus = 'BOUNCED'")
        List<NewsletterDeliveryLog> findBouncedDeliveriesByCampaignId(@Param("campaignId") Long campaignId);

        /**
         * Finds failed deliveries within a time range.
         *
         * @param startTime the start timestamp
         * @param endTime   the end timestamp
         * @return list of failed delivery logs
         */
        List<NewsletterDeliveryLog> findByNewsletterDeliveryLogStatusAndCreatedAtBetween(
                        NewsletterDeliveryStatus status, Instant startTime, Instant endTime);

        /**
         * Finds deliveries that were opened but not clicked.
         *
         * @param campaignId the campaign ID
         * @return list of opened but not clicked delivery logs
         */
        @Query("SELECT d FROM NewsletterDeliveryLog d WHERE d.newsletterCampaign.newsletterCampaignId = :campaignId " +
                        "AND d.newsletterDeliveryLogStatus = 'OPENED' AND d.newsletterDeliveryLogClickedAt IS NULL")
        List<NewsletterDeliveryLog> findOpenedNotClickedByCampaignId(@Param("campaignId") Long campaignId);

        /**
         * Gets delivery statistics for a campaign.
         *
         * @param campaignId the campaign ID
         * @return array with counts: [total, sent, delivered, opened, clicked, bounced,
         *         failed]
         */
        @Query("SELECT " +
                        "COUNT(d), " +
                        "COUNT(CASE WHEN d.newsletterDeliveryLogStatus IN ('SENT', 'DELIVERED', 'OPENED', 'CLICKED') THEN 1 END), "
                        +
                        "COUNT(CASE WHEN d.newsletterDeliveryLogStatus IN ('DELIVERED', 'OPENED', 'CLICKED') THEN 1 END), "
                        +
                        "COUNT(CASE WHEN d.newsletterDeliveryLogStatus IN ('OPENED', 'CLICKED') THEN 1 END), " +
                        "COUNT(CASE WHEN d.newsletterDeliveryLogStatus = 'CLICKED' THEN 1 END), " +
                        "COUNT(CASE WHEN d.newsletterDeliveryLogStatus = 'BOUNCED' THEN 1 END), " +
                        "COUNT(CASE WHEN d.newsletterDeliveryLogStatus = 'FAILED' THEN 1 END) " +
                        "FROM NewsletterDeliveryLog d WHERE d.newsletterCampaign.newsletterCampaignId = :campaignId")
        Object[] getDeliveryStatisticsByCampaignId(@Param("campaignId") Long campaignId);
}