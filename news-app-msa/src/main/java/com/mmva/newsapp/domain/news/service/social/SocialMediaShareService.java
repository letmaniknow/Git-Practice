package com.mmva.newsapp.domain.news.service.social;

import com.mmva.newsapp.domain.news.dto.social.SocialMediaShareMarkPlatformSharedRequestDto;
import com.mmva.newsapp.domain.news.dto.social.SocialMediaShareDashboardResponseDto;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for social media sharing operations with per-platform
 * tracking.
 * <p>
 * This interface defines operations for managing social media sharing
 * of news articles, including dashboard management, per-platform share
 * tracking,
 * and content generation for different platforms.
 * </p>
 *
 * @author MMVA Team
 * @version 2.0
 * @since 2026-02-06
 */
public interface SocialMediaShareService {

    /**
     * Get social sharing dashboard data with per-platform tracking
     * Returns news items that need social sharing, organized by priority
     *
     * @return dashboard response with categorized news items and platform statuses
     */
    SocialMediaShareDashboardResponseDto getSocialMediaSharingDashboard();

    /**
     * Mark a specific platform as shared for a news article
     *
     * @param request details of the platform sharing completion
     */
    void markPlatformShared(SocialMediaShareMarkPlatformSharedRequestDto request);

    /**
     * Mark multiple platforms as shared for a news article
     *
     * @param newsId    the news article ID
     * @param platforms list of platforms to mark as completed
     * @param sharedBy  ID of the admin/editor performing the action
     */
    void markPlatformsShared(UUID newsId, List<String> platforms, UUID sharedBy);

    /**
     * Create or ensure sharing record exists for a news article
     * Called automatically when news is published
     *
     * @param newsId the news article ID
     */
    void ensureSharingRecordExists(UUID newsId);

    /**
     * Generate share texts for a news article
     *
     * @param news the news article entity
     * @return map of platform names to formatted share texts
     */
    Map<String, String> generateShareTexts(NewsMasterEntity news);

    /**
     * Get sharing statistics for dashboard
     *
     * @return map with sharing statistics
     */
    Map<String, Object> getSharingStatistics();
}