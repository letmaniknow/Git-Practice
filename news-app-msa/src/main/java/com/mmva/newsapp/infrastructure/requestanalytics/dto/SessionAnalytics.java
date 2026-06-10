package com.mmva.newsapp.infrastructure.requestanalytics.dto;

/**
 * Session analytics and user engagement.
 */
public record SessionAnalytics(
        long totalSessions,
        long newSessions,
        long returningSessions,
        Double newSessionRate) {
}