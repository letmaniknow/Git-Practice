package com.mmva.newsapp.domain.news.service.dashboard;

import com.mmva.newsapp.domain.news.dto.dashboard.AdminDashboardStatsDto;
import com.mmva.newsapp.domain.news.dto.audit.NewsAuditLogDto;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.news.audit.model.NewsAuditLog;
import com.mmva.newsapp.domain.news.repository.audit.NewsAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

/**
 * Implementation of {@link AdminDashboardService}
 * Aggregates dashboard statistics and retrieves recent activity
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 2026-05-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final EntityManager entityManager;
    private final NewsAuditLogRepository auditLogRepository;

    @Override
    public AdminDashboardStatsDto getDashboardStats() {
        log.info("📊 Fetching dashboard statistics");

        try {
            // Get total articles count
            Long totalArticles = getTotalArticlesCount();

            // Get published this month count
            Long publishedThisMonth = getPublishedThisMonthCount();

            // Get draft count
            Long draftCount = getDraftCount();

            // Get scheduled count
            Long scheduledCount = getScheduledCount();

            // Get archived/deleted count
            Long archivedCount = getArchivedCount();

            // Get total page views
            Long totalPageViews = getTotalPageViews();

            // Get total engagement
            Long totalEngagement = getTotalEngagement();

            // Determine system health based on metrics
            String systemHealth = determineSystemHealth(totalArticles, publishedThisMonth, totalEngagement);

            AdminDashboardStatsDto stats = AdminDashboardStatsDto.builder()
                    .totalArticles(totalArticles)
                    .publishedThisMonth(publishedThisMonth)
                    .draftCount(draftCount)
                    .scheduledCount(scheduledCount)
                    .archivedCount(archivedCount)
                    .systemHealth(systemHealth)
                    .totalPageViews(totalPageViews)
                    .totalEngagement(totalEngagement)
                    .build();

            log.info("✅ Dashboard stats retrieved: {} articles, {} published this month",
                    totalArticles, publishedThisMonth);
            return stats;

        } catch (Exception e) {
            log.error("❌ Error fetching dashboard statistics", e);
            throw new RuntimeException("Failed to fetch dashboard statistics", e);
        }
    }

    @Override
    public Page<NewsAuditLogDto> getRecentActivity(Pageable pageable) {
        log.info("📝 Fetching recent activity - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        try {
            // Get paginated audit logs with provided pagination parameters
            Page<NewsAuditLog> auditLogs = auditLogRepository.findAll(pageable);

            // Map entities to DTOs
            Page<NewsAuditLogDto> activities = auditLogs.map(this::convertToDto);

            log.info("✅ Retrieved {} recent activities (total: {})", activities.getNumberOfElements(),
                    activities.getTotalElements());
            return activities;

        } catch (Exception e) {
            log.error("❌ Error fetching recent activity", e);
            throw new RuntimeException("Failed to fetch recent activity", e);
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Get total articles count
     */
    private Long getTotalArticlesCount() {
        String jpql = "SELECT COUNT(n) FROM NewsMasterEntity n WHERE n.deletedAt IS NULL";
        Query query = entityManager.createQuery(jpql);
        return (Long) query.getSingleResult();
    }

    /**
     * Get articles published in current month
     */
    private Long getPublishedThisMonthCount() {
        // Get current time as Instant (matches createdAt field type)
        Instant now = Instant.now();

        // Calculate month start as Instant
        // Convert to LocalDateTime in system's default timezone, get month start, then
        // back to Instant
        LocalDateTime localNow = LocalDateTime.ofInstant(now, java.time.ZoneId.systemDefault());
        LocalDateTime monthStart = localNow.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        Instant monthStartInstant = monthStart.atZone(java.time.ZoneId.systemDefault()).toInstant();

        String jpql = "SELECT COUNT(n) FROM NewsMasterEntity n " +
                "WHERE n.newsWorkflowStatus = 'PUBLISHED' " +
                "AND n.createdAt >= :monthStart " +
                "AND n.deletedAt IS NULL";

        Query query = entityManager.createQuery(jpql);
        query.setParameter("monthStart", monthStartInstant);
        return (Long) query.getSingleResult();
    }

    /**
     * Get draft articles count
     */
    private Long getDraftCount() {
        String jpql = "SELECT COUNT(n) FROM NewsMasterEntity n " +
                "WHERE n.newsWorkflowStatus = 'DRAFT' " +
                "AND n.deletedAt IS NULL";
        Query query = entityManager.createQuery(jpql);
        return (Long) query.getSingleResult();
    }

    /**
     * Get scheduled articles count
     */
    private Long getScheduledCount() {
        String jpql = "SELECT COUNT(n) FROM NewsMasterEntity n " +
                "WHERE n.newsWorkflowStatus = 'SCHEDULED' " +
                "AND n.deletedAt IS NULL";
        Query query = entityManager.createQuery(jpql);
        return (Long) query.getSingleResult();
    }

    /**
     * Get archived/deleted articles count
     */
    private Long getArchivedCount() {
        String jpql = "SELECT COUNT(n) FROM NewsMasterEntity n WHERE n.deletedAt IS NOT NULL";
        Query query = entityManager.createQuery(jpql);
        return (Long) query.getSingleResult();
    }

    /**
     * Get total page views (sum of all article views)
     */
    private Long getTotalPageViews() {
        String jpql = "SELECT COALESCE(SUM(n.newsViewCount), 0L) FROM NewsMasterEntity n WHERE n.deletedAt IS NULL";
        Query query = entityManager.createQuery(jpql);
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    /**
     * Get total engagement (sum of likes, shares, comments, bookmarks)
     * Calculates: SUM(likes) + SUM(shares) + SUM(comments) + SUM(bookmarks)
     */
    private Long getTotalEngagement() {
        // Get sum of each engagement metric separately (JPQL limitation: no arithmetic
        // in SUM)
        Long totalLikes = getSumEngagementMetric("newsLikeCount");
        Long totalShares = getSumEngagementMetric("newsShareCount");
        Long totalComments = getSumEngagementMetric("newsCommentCount");
        Long totalBookmarks = getSumEngagementMetric("newsBookmarkCount");

        return totalLikes + totalShares + totalComments + totalBookmarks;
    }

    /**
     * Helper method to calculate sum of a single engagement metric
     */
    private Long getSumEngagementMetric(String metricField) {
        String jpql = String.format("SELECT COALESCE(SUM(n.%s), 0L) FROM NewsMasterEntity n WHERE n.deletedAt IS NULL",
                metricField);
        Query query = entityManager.createQuery(jpql);
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    /**
     * Determine system health based on metrics
     */
    private String determineSystemHealth(Long totalArticles, Long publishedThisMonth, Long totalEngagement) {
        if (totalArticles < 1) {
            return "warning";
        }

        if (publishedThisMonth == 0 && totalArticles > 10) {
            return "warning";
        }

        if (totalEngagement > 500 && publishedThisMonth > 5) {
            return "healthy";
        }

        return "healthy";
    }

    /**
     * Convert NewsAuditLog entity to NewsAuditLogDto
     * Maps all 24 industry-standard audit fields + domain-specific newsId
     */
    private NewsAuditLogDto convertToDto(NewsAuditLog auditLog) {
        return NewsAuditLogDto.builder()
                // IDENTIFICATION
                .id(auditLog.getId())

                // WHO (Identity)
                .actorId(auditLog.getActorId())
                .actorDisplayName(auditLog.getActorDisplayName())
                .sessionId(auditLog.getSessionId())

                // WHAT (Action)
                .action(auditLog.getAction())
                .domain(auditLog.getDomain())
                .source(auditLog.getSource())

                // WHICH (Resources)
                .resourceId(auditLog.getResourceId())
                .resourceName(auditLog.getResourceName())
                .newsId(auditLog.getNewsId())

                // WHEN (Timestamp)
                .createdAt(auditLog.getCreatedAt())

                // WHERE (Location)
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .requestUri(auditLog.getRequestUri())

                // WHY (Context)
                .reason(auditLog.getReason())
                .details(auditLog.getDetails())

                // HOW (Status & Metrics)
                .isSuccess(auditLog.getIsSuccess())
                .errorMessage(auditLog.getErrorMessage())
                .httpStatus(auditLog.getHttpStatus())
                .requestMethod(auditLog.getRequestMethod())
                .affectedRows(auditLog.getAffectedRows())

                // CORRELATE & RISK
                .transactionId(auditLog.getTransactionId())
                .severity(auditLog.getSeverity())
                .responseTimeMs(auditLog.getResponseTimeMs())

                .build();
    }
}
