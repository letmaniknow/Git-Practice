package com.mmva.newsapp.domain.newsletter.repository.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mmva.newsapp.domain.newsletter.model.core.NewsletterCampaign;
import com.mmva.newsapp.domain.newsletter.model.core.NewsletterCampaignContent;

import java.util.List;
import java.util.Optional;

/**
 * Repository for NewsletterCampaignContent entity operations.
 *
 * <p>
 * Provides data access methods for newsletter campaign content management
 * with support for multi-language content and campaign relationships.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsletterCampaignContentRepository
                extends JpaRepository<NewsletterCampaignContent, Long>,
                JpaSpecificationExecutor<NewsletterCampaignContent> {

        /**
         * Finds all content for a specific campaign.
         *
         * @param campaign the campaign entity
         * @return list of campaign content
         */
        List<NewsletterCampaignContent> findByNewsletterCampaign(NewsletterCampaign campaign);

        /**
         * Finds all content for a specific campaign by campaign ID.
         *
         * @param campaignId the campaign ID
         * @return list of campaign content
         */
        List<NewsletterCampaignContent> findByNewsletterCampaign_NewsletterCampaignId(Long campaignId);

        /**
         * Finds content for a specific campaign and language.
         *
         * @param campaign the campaign entity
         * @param language the language code
         * @return optional campaign content
         */
        Optional<NewsletterCampaignContent> findByNewsletterCampaignAndNewsletterCampaignContentLanguage(
                        NewsletterCampaign campaign, String language);

        /**
         * Finds content for a specific campaign ID and language.
         *
         * @param campaignId the campaign ID
         * @param language   the language code
         * @return optional campaign content
         */
        Optional<NewsletterCampaignContent> findByNewsletterCampaign_NewsletterCampaignIdAndNewsletterCampaignContentLanguage(
                        Long campaignId, String language);

        /**
         * Finds all content by language.
         *
         * @param language the language code
         * @param pageable pagination information
         * @return page of campaign content
         */
        Page<NewsletterCampaignContent> findByNewsletterCampaignContentLanguage(String language, Pageable pageable);

        /**
         * Counts content entries for a specific campaign.
         *
         * @param campaign the campaign entity
         * @return count of content entries
         */
        long countByNewsletterCampaign(NewsletterCampaign campaign);

        /**
         * Counts content entries for a specific campaign by campaign ID.
         *
         * @param campaignId the campaign ID
         * @return count of content entries
         */
        long countByNewsletterCampaign_NewsletterCampaignId(Long campaignId);

        /**
         * Finds campaigns that have content in multiple languages.
         *
         * @return list of campaign IDs with multi-language content
         */
        @Query("SELECT DISTINCT c.newsletterCampaign.newsletterCampaignId FROM NewsletterCampaignContent c " +
                        "WHERE c.deletedAt IS NULL " +
                        "GROUP BY c.newsletterCampaign.newsletterCampaignId " +
                        "HAVING COUNT(DISTINCT c.newsletterCampaignContentLanguage) > 1")
        List<Long> findCampaignsWithMultipleLanguages();

        /**
         * Finds the most recently updated content for a campaign.
         *
         * @param campaignId the campaign ID
         * @return optional most recent content
         */
        @Query("SELECT c FROM NewsletterCampaignContent c WHERE c.deletedAt IS NULL AND " +
                        "c.newsletterCampaign.newsletterCampaignId = :campaignId " +
                        "ORDER BY c.updatedAt DESC")
        Optional<NewsletterCampaignContent> findMostRecentContentByCampaignId(@Param("campaignId") Long campaignId);

        /**
         * Checks if a campaign has content in a specific language.
         *
         * @param campaignId the campaign ID
         * @param language   the language code
         * @return true if content exists for the language
         */
        boolean existsByNewsletterCampaign_NewsletterCampaignIdAndNewsletterCampaignContentLanguage(
                        Long campaignId, String language);
}