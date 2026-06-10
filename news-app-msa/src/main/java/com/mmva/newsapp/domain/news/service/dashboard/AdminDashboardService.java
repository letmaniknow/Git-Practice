package com.mmva.newsapp.domain.news.service.dashboard;

import com.mmva.newsapp.domain.news.dto.dashboard.AdminDashboardStatsDto;
import com.mmva.newsapp.domain.news.dto.audit.NewsAuditLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Admin Dashboard functionality
 * Provides statistics aggregation and recent activity retrieval
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 2026-05-10
 */
public interface AdminDashboardService {

    /**
     * Get aggregated dashboard statistics
     * 
     * @return dashboard statistics DTO with aggregated metrics
     */
    AdminDashboardStatsDto getDashboardStats();

    /**
     * Get recent news audit activity with pagination
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return paginated page of recent activities from audit logs
     */
    Page<NewsAuditLogDto> getRecentActivity(Pageable pageable);
}
