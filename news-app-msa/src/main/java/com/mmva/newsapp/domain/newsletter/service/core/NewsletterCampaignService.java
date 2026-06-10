package com.mmva.newsapp.domain.newsletter.service.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mmva.newsapp.domain.newsletter.dto.core.NewsletterCampaignRequestDto;
import com.mmva.newsapp.domain.newsletter.dto.core.NewsletterCampaignResponseDto;
import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterCampaignStatus;
import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterCampaignType;
import com.mmva.newsapp.domain.newsletter.exception.core.NewsletterCampaignNotFoundException;

import java.time.Instant;
import java.util.List;

/**
 * Service interface for newsletter campaign management.
 *
 * <p>
 * Provides business logic for campaign operations including
 * creation, scheduling, execution, and analytics.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsletterCampaignService {

    // =========================
    // Campaign Management
    // =========================

    /**
     * Creates a new newsletter campaign.
     *
     * @param request the campaign creation request
     * @return the created campaign response
     */
    NewsletterCampaignResponseDto createCampaign(NewsletterCampaignRequestDto request);

    /**
     * Updates an existing campaign.
     *
     * @param campaignId the campaign ID
     * @param request    the campaign update request
     * @return the updated campaign response
     * @throws NewsletterCampaignNotFoundException if campaign not found
     */
    NewsletterCampaignResponseDto updateCampaign(Long campaignId, NewsletterCampaignRequestDto request);

    /**
     * Schedules a campaign for sending.
     *
     * @param campaignId    the campaign ID
     * @param scheduledTime the scheduled send time
     * @return the updated campaign response
     * @throws NewsletterCampaignNotFoundException if campaign not found
     */
    NewsletterCampaignResponseDto scheduleCampaign(Long campaignId, Instant scheduledTime);

    /**
     * Sends a campaign immediately.
     *
     * @param campaignId the campaign ID
     * @return the updated campaign response
     * @throws NewsletterCampaignNotFoundException if campaign not found
     */
    NewsletterCampaignResponseDto sendCampaign(Long campaignId);

    /**
     * Cancels a scheduled campaign.
     *
     * @param campaignId the campaign ID
     * @return the updated campaign response
     * @throws NewsletterCampaignNotFoundException if campaign not found
     */
    NewsletterCampaignResponseDto cancelCampaign(Long campaignId);

    /**
     * Pauses an active campaign.
     *
     * @param campaignId the campaign ID
     * @return the updated campaign response
     * @throws NewsletterCampaignNotFoundException if campaign not found
     */
    NewsletterCampaignResponseDto pauseCampaign(Long campaignId);

    /**
     * Resumes a paused campaign.
     *
     * @param campaignId the campaign ID
     * @return the updated campaign response
     * @throws NewsletterCampaignNotFoundException if campaign not found
     */
    NewsletterCampaignResponseDto resumeCampaign(Long campaignId);

    // =========================
    // Query Operations
    // =========================

    /**
     * Gets a campaign by ID.
     *
     * @param campaignId the campaign ID
     * @return the campaign response
     * @throws NewsletterCampaignNotFoundException if campaign not found
     */
    NewsletterCampaignResponseDto getCampaignById(Long campaignId);

    /**
     * Gets campaigns by status with pagination.
     *
     * @param status   the campaign status
     * @param pageable pagination information
     * @return page of campaign responses
     */
    Page<NewsletterCampaignResponseDto> getCampaignsByStatus(NewsletterCampaignStatus status, Pageable pageable);

    /**
     * Gets campaigns by type with pagination.
     *
     * @param type     the campaign type
     * @param pageable pagination information
     * @return page of campaign responses
     */
    Page<NewsletterCampaignResponseDto> getCampaignsByType(NewsletterCampaignType type, Pageable pageable);

    /**
     * Gets all campaigns with pagination.
     *
     * @param pageable pagination information
     * @return page of campaign responses
     */
    Page<NewsletterCampaignResponseDto> getAllCampaigns(Pageable pageable);

    /**
     * Gets scheduled campaigns ready for sending.
     *
     * @param pageable pagination information
     * @return page of scheduled campaign responses
     */
    Page<NewsletterCampaignResponseDto> getScheduledCampaignsReadyForSending(Pageable pageable);

    /**
     * Gets campaigns within a date range.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @param pageable  pagination information
     * @return page of campaign responses
     */
    Page<NewsletterCampaignResponseDto> getCampaignsByDateRange(Instant startDate, Instant endDate, Pageable pageable);

    // =========================
    // Analytics & Statistics
    // =========================

    /**
     * Gets campaign analytics.
     *
     * @param campaignId the campaign ID
     * @return campaign analytics
     * @throws NewsletterCampaignNotFoundException if campaign not found
     */
    CampaignAnalytics getCampaignAnalytics(Long campaignId);

    /**
     * Gets overall campaign statistics.
     *
     * @return overall campaign statistics
     */
    OverallCampaignStatistics getOverallCampaignStatistics();

    /**
     * Calculates average open rate across all campaigns.
     *
     * @return average open rate percentage
     */
    double getAverageOpenRate();

    /**
     * Calculates average click rate across all campaigns.
     *
     * @return average click rate percentage
     */
    double getAverageClickRate();

    /**
     * Gets campaign performance trends.
     *
     * @param days number of days to look back
     * @return list of performance data points
     */
    List<CampaignPerformanceData> getCampaignPerformanceTrends(int days);

    // =========================
    // Content Management
    // =========================

    /**
     * Adds content to a campaign.
     *
     * @param campaignId the campaign ID
     * @param contentId  the content ID
     * @return the updated campaign response
     * @throws NewsletterCampaignNotFoundException if campaign not found
     */
    NewsletterCampaignResponseDto addContentToCampaign(Long campaignId, Long contentId);

    /**
     * Removes content from a campaign.
     *
     * @param campaignId the campaign ID
     * @param contentId  the content ID
     * @return the updated campaign response
     * @throws NewsletterCampaignNotFoundException if campaign not found
     */
    NewsletterCampaignResponseDto removeContentFromCampaign(Long campaignId, Long contentId);

    /**
     * Gets campaigns with multiple languages.
     *
     * @param pageable pagination information
     * @return page of campaign responses
     */
    Page<NewsletterCampaignResponseDto> getCampaignsWithMultipleLanguages(Pageable pageable);

    // =========================
    // Administrative Operations
    // =========================

    /**
     * Deletes a campaign.
     *
     * @param campaignId the campaign ID
     * @throws NewsletterCampaignNotFoundException if campaign not found
     */
    void deleteCampaign(Long campaignId);

    /**
     * Duplicates an existing campaign.
     *
     * @param campaignId the campaign ID to duplicate
     * @param newName    the name for the duplicated campaign
     * @return the duplicated campaign response
     * @throws NewsletterCampaignNotFoundException if campaign not found
     */
    NewsletterCampaignResponseDto duplicateCampaign(Long campaignId, String newName);

    // =========================
    // Data Transfer Objects
    // =========================

    /**
     * Campaign analytics data.
     */
    interface CampaignAnalytics {
        long getTotalRecipients();

        long getDeliveredCount();

        long getOpenedCount();

        long getClickedCount();

        long getBouncedCount();

        long getUnsubscribedCount();

        double getOpenRate();

        double getClickRate();

        double getBounceRate();

        double getUnsubscribeRate();
    }

    /**
     * Overall campaign statistics.
     */
    interface OverallCampaignStatistics {
        long getTotalCampaigns();

        long getActiveCampaigns();

        long getCompletedCampaigns();

        long getScheduledCampaigns();

        long getTotalRecipients();

        long getTotalDelivered();

        double getAverageOpenRate();

        double getAverageClickRate();
    }

    /**
     * Campaign performance data point.
     */
    interface CampaignPerformanceData {
        String getDate();

        long getCampaignsSent();

        long getTotalOpens();

        long getTotalClicks();

        double getAverageOpenRate();

        double getAverageClickRate();
    }
}