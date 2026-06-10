package com.mmva.newsapp.domain.newsletter.repository.core;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterCampaignStatus;
import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterCampaignType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mmva.newsapp.domain.newsletter.model.core.NewsletterCampaign;

import java.time.Instant;
import java.util.List;

/**
 * Repository for NewsletterCampaign entity operations.
 *
 * <p>
 * Provides data access methods for newsletter campaign management
 * with support for scheduling, status tracking, and analytics.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsletterCampaignRepository
                extends JpaRepository<NewsletterCampaign, Long>, JpaSpecificationExecutor<NewsletterCampaign> {

        /**
         * Finds campaigns by status.
         *
         * @param status   the campaign status
         * @param pageable pagination information
         * @return page of campaigns
         */
        Page<NewsletterCampaign> findByNewsletterCampaignStatus(NewsletterCampaignStatus status, Pageable pageable);

        /**
         * Finds campaigns by type.
         *
         * @param type     the campaign type
         * @param pageable pagination information
         * @return page of campaigns
         */
        Page<NewsletterCampaign> findByNewsletterCampaignType(NewsletterCampaignType type, Pageable pageable);

        /**
         * Finds campaigns scheduled for sending within a time range.
         *
         * @param startTime the start of the time range
         * @param endTime   the end of the time range
         * @return list of scheduled campaigns
         */
        List<NewsletterCampaign> findByNewsletterCampaignScheduledAtBetween(Instant startTime, Instant endTime);

        /**
         * Finds campaigns that are scheduled but not yet sent.
         *
         * @param currentTime the current timestamp
         * @param pageable    pagination information
         * @return page of campaigns ready for sending
         */
        @Query("SELECT c FROM NewsletterCampaign c WHERE c.deletedAt IS NULL AND " +
                        "c.newsletterCampaignStatus = 'SCHEDULED' AND " +
                        "c.newsletterCampaignScheduledAt <= :currentTime")
        Page<NewsletterCampaign> findScheduledCampaignsReadyForSending(@Param("currentTime") Instant currentTime,
                        Pageable pageable);

        /**
         * Finds campaigns by name containing a keyword (case-insensitive).
         *
         * @param keyword  the keyword to search in campaign names
         * @param pageable pagination information
         * @return page of campaigns
         */
        Page<NewsletterCampaign> findByNewsletterCampaignNameContainingIgnoreCase(String keyword, Pageable pageable);

        /**
         * Counts campaigns by status.
         *
         * @param status the campaign status
         * @return count of campaigns with the status
         */
        long countByNewsletterCampaignStatus(NewsletterCampaignStatus status);

        /**
         * Finds campaigns sent within a date range.
         *
         * @param startDate the start date
         * @param endDate   the end date
         * @param pageable  pagination information
         * @return page of sent campaigns
         */
        Page<NewsletterCampaign> findByNewsletterCampaignSentAtBetween(Instant startDate, Instant endDate,
                        Pageable pageable);

        /**
         * Calculates total recipients across all campaigns with a specific status.
         *
         * @param status the campaign status
         * @return total number of recipients
         */
        @Query("SELECT COALESCE(SUM(c.newsletterCampaignTotalRecipients), 0) FROM NewsletterCampaign c " +
                        "WHERE c.deletedAt IS NULL AND c.newsletterCampaignStatus = :status")
        Long sumTotalRecipientsByStatus(@Param("status") NewsletterCampaignStatus status);

        /**
         * Calculates average open rate for sent campaigns.
         *
         * @return average open rate percentage
         */
        @Query("SELECT COALESCE(AVG(c.newsletterCampaignOpenRate), 0) FROM NewsletterCampaign c " +
                        "WHERE c.deletedAt IS NULL AND c.newsletterCampaignStatus = 'SENT' AND c.newsletterCampaignOpenRate IS NOT NULL")
        Double calculateAverageOpenRate();

        /**
         * Calculates average click rate for sent campaigns.
         *
         * @return average click rate percentage
         */
        @Query("SELECT COALESCE(AVG(c.newsletterCampaignClickRate), 0) FROM NewsletterCampaign c " +
                        "WHERE c.deletedAt IS NULL AND c.newsletterCampaignStatus = 'SENT' AND c.newsletterCampaignClickRate IS NOT NULL")
        Double calculateAverageClickRate();
}