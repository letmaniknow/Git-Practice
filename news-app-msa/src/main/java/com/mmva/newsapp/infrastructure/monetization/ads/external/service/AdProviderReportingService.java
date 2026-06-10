package com.mmva.newsapp.infrastructure.monetization.ads.external.service;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.AdProviderDashboardOverviewResponse;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.AdProviderTenantUsageStats;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Advanced reporting service for ad providers dashboard
 *
 * Provides comprehensive reporting capabilities including PDF and Excel exports
 * for dashboard data, analytics, and usage statistics
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface AdProviderReportingService {

        /**
         * Generate PDF report for dashboard overview
         *
         * Creates a comprehensive PDF report with charts, tables, and analytics
         * for the dashboard overview data
         *
         * @param overview Dashboard overview data
         * @param tenantId Tenant identifier for report context
         * @return PDF report as byte array
         */
        ByteArrayOutputStream generateDashboardPdfReport(
                        AdProviderDashboardOverviewResponse overview,
                        String tenantId);

        /**
         * Generate Excel report for dashboard data
         *
         * Creates a detailed Excel spreadsheet with multiple sheets containing
         * dashboard metrics, provider data, and analytics
         *
         * @param overview Dashboard overview data
         * @param tenantId Tenant identifier for report context
         * @return Excel report as byte array
         */
        ByteArrayOutputStream generateDashboardExcelReport(
                        AdProviderDashboardOverviewResponse overview,
                        String tenantId);

        /**
         * Generate usage statistics PDF report
         *
         * Creates a PDF report focused on usage statistics and analytics
         *
         * @param usageStats Usage statistics data
         * @param tenantId   Tenant identifier
         * @return PDF report as byte array
         */
        ByteArrayOutputStream generateUsageStatsPdfReport(
                        AdProviderTenantUsageStats usageStats,
                        String tenantId);

        /**
         * Generate usage statistics Excel report
         *
         * Creates an Excel report with usage metrics and trends
         *
         * @param usageStats Usage statistics data
         * @param tenantId   Tenant identifier
         * @return Excel report as byte array
         */
        ByteArrayOutputStream generateUsageStatsExcelReport(
                        AdProviderTenantUsageStats usageStats,
                        String tenantId);

        /**
         * Generate system-wide analytics PDF report
         *
         * Creates a comprehensive PDF report for system-wide analytics
         * across all tenants
         *
         * @param allTenantStats List of all tenant usage statistics
         * @param systemStats    System-wide aggregated statistics
         * @return PDF report as byte array
         */
        ByteArrayOutputStream generateSystemAnalyticsPdfReport(
                        List<AdProviderTenantUsageStats> allTenantStats,
                        AdProviderTenantUsageStats systemStats);

        /**
         * Generate system-wide analytics Excel report
         *
         * Creates an Excel report with multi-tenant analytics and comparisons
         *
         * @param allTenantStats List of all tenant usage statistics
         * @param systemStats    System-wide aggregated statistics
         * @return Excel report as byte array
         */
        ByteArrayOutputStream generateSystemAnalyticsExcelReport(
                        List<AdProviderTenantUsageStats> allTenantStats,
                        AdProviderTenantUsageStats systemStats);

        /**
         * Generate date-range specific analytics report
         *
         * Creates a report for a specific date range with trend analysis
         *
         * @param tenantId  Tenant identifier
         * @param startDate Start date for the report
         * @param endDate   End date for the report
         * @param format    Report format (PDF or EXCEL)
         * @return Report as byte array
         */
        ByteArrayOutputStream generateDateRangeAnalyticsReport(
                        String tenantId,
                        LocalDate startDate,
                        LocalDate endDate,
                        String format);
}