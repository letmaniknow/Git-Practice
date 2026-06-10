package com.mmva.newsapp.domain.news.service.recommendation;

import java.util.List;

import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;

/**
 * Service interface for generating news recommendations.
 * Provides personalized news suggestions based on user activity and
 * preferences.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsRecommendationService {

    /**
     * Generates personalized news recommendations based on user activity.
     *
     * @param userActivity the user's activity data (views, likes, bookmarks, etc.)
     * @return list of recommended news articles
     */
    List<NewsMasterEntity> generateRecommendations(Object userActivity);
}
