package com.mmva.newsapp.domain.newsletter.service.analytics;

import com.mmva.newsapp.domain.newsletter.dto.analytics.NewsletterDeliveryAnalyticsDto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Service interface for newsletter analytics and reporting.
 *
 * <p>
 * Provides comprehensive analytics for campaigns, subscribers,
 * delivery performance, and business intelligence.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsletterAnalyticsService {

    // =========================
    // Campaign Analytics
    // =========================

    /**
     * Gets comprehensive analytics for a campaign.
     *
     * @param campaignId the campaign ID
     * @return comprehensive campaign analytics
     */
    ComprehensiveCampaignAnalytics getComprehensiveCampaignAnalytics(Long campaignId);

    /**
     * Gets campaign performance comparison.
     *
     * @param campaignIds list of campaign IDs to compare
     * @return comparison analytics
     */
    CampaignComparisonAnalytics compareCampaigns(List<Long> campaignIds);

    /**
     * Gets campaign performance over time.
     *
     * @param campaignId the campaign ID
     * @param days       number of days to analyze
     * @return time-series performance data
     */
    List<TimeSeriesPerformanceData> getCampaignPerformanceOverTime(Long campaignId, int days);

    // =========================
    // Subscriber Analytics
    // =========================

    /**
     * Gets subscriber engagement analytics.
     *
     * @param subscriberId the subscriber ID
     * @return subscriber engagement data
     */
    SubscriberEngagementAnalytics getSubscriberEngagementAnalytics(Long subscriberId);

    /**
     * Gets subscriber segment analytics.
     *
     * @param segmentCriteria criteria for segmentation
     * @return segment analytics
     */
    SubscriberSegmentAnalytics getSubscriberSegmentAnalytics(Map<String, Object> segmentCriteria);

    /**
     * Gets subscriber lifecycle analytics.
     *
     * @param days number of days to analyze
     * @return lifecycle analytics
     */
    SubscriberLifecycleAnalytics getSubscriberLifecycleAnalytics(int days);

    // =========================
    // Delivery Analytics
    // =========================

    /**
     * Gets delivery performance analytics.
     *
     * @param startDate start date for analysis
     * @param endDate   end date for analysis
     * @return delivery performance data
     */
    DeliveryPerformanceAnalytics getDeliveryPerformanceAnalytics(Instant startDate, Instant endDate);

    /**
     * Gets bounce analysis.
     *
     * @param campaignId the campaign ID (null for all campaigns)
     * @return bounce analysis data
     */
    BounceAnalysis getBounceAnalysis(Long campaignId);

    /**
     * Gets geographic delivery analytics.
     *
     * @param campaignId the campaign ID
     * @return geographic analytics
     */
    GeographicAnalytics getGeographicAnalytics(Long campaignId);

    // =========================
    // Content Analytics
    // =========================

    /**
     * Gets content performance analytics.
     *
     * @param campaignId the campaign ID
     * @return content performance data
     */
    ContentPerformanceAnalytics getContentPerformanceAnalytics(Long campaignId);

    /**
     * Gets A/B test results.
     *
     * @param testId the A/B test ID
     * @return A/B test results
     */
    ABTestResults getABTestResults(String testId);

    /**
     * Gets subject line performance.
     *
     * @param campaignIds list of campaign IDs to analyze
     * @return subject line performance data
     */
    List<SubjectLinePerformance> getSubjectLinePerformance(List<Long> campaignIds);

    // =========================
    // Business Intelligence
    // =========================

    /**
     * Gets revenue attribution from campaigns.
     *
     * @param campaignId            the campaign ID
     * @param attributionWindowDays attribution window in days
     * @return revenue attribution data
     */
    RevenueAttribution getRevenueAttribution(Long campaignId, int attributionWindowDays);

    /**
     * Gets customer lifetime value by acquisition source.
     *
     * @param startDate start date for analysis
     * @param endDate   end date for analysis
     * @return CLV by acquisition source
     */
    Map<String, Double> getCustomerLifetimeValueBySource(Instant startDate, Instant endDate);

    /**
     * Gets churn prediction analytics.
     *
     * @param days number of days to look ahead
     * @return churn prediction data
     */
    ChurnPredictionAnalytics getChurnPredictionAnalytics(int days);

    // =========================
    // Reporting
    // =========================

    /**
     * Generates campaign report.
     *
     * @param campaignId the campaign ID
     * @param reportType the type of report
     * @return campaign report data
     */
    CampaignReport generateCampaignReport(Long campaignId, ReportType reportType);

    /**
     * Generates subscriber report.
     *
     * @param startDate start date for report
     * @param endDate   end date for report
     * @return subscriber report data
     */
    SubscriberReport generateSubscriberReport(Instant startDate, Instant endDate);

    /**
     * Generates executive summary.
     *
     * @param startDate start date for summary
     * @param endDate   end date for summary
     * @return executive summary data
     */
    ExecutiveSummary generateExecutiveSummary(Instant startDate, Instant endDate);

    // =========================
    // Data Transfer Objects
    // =========================

    /**
     * Comprehensive campaign analytics.
     */
    interface ComprehensiveCampaignAnalytics {
        CampaignMetrics getMetrics();

        List<TimeSeriesDataPoint> getTimeSeriesData();

        GeographicData getGeographicData();

        DeviceData getDeviceData();

        ContentPerformance getContentPerformance();
    }

    /**
     * Campaign metrics.
     */
    interface CampaignMetrics {
        long getTotalRecipients();

        long getDelivered();

        long getOpened();

        long getClicked();

        long getBounced();

        long getUnsubscribed();

        double getOpenRate();

        double getClickRate();

        double getBounceRate();

        double getUnsubscribeRate();

        double getRevenue();

        double getConversionRate();
    }

    /**
     * Time series data point.
     */
    interface TimeSeriesDataPoint {
        Instant getTimestamp();

        long getOpens();

        long getClicks();

        long getUnsubscribes();
    }

    /**
     * Geographic data.
     */
    interface GeographicData {
        Map<String, Long> getOpensByCountry();

        Map<String, Long> getClicksByCountry();

        Map<String, Double> getOpenRatesByCountry();
    }

    /**
     * Device data.
     */
    interface DeviceData {
        Map<String, Long> getOpensByDevice();

        Map<String, Long> getClicksByDevice();

        Map<String, Double> getOpenRatesByDevice();
    }

    /**
     * Content performance.
     */
    interface ContentPerformance {
        List<UrlPerformance> getUrlPerformance();

        Map<String, Double> getContentTypePerformance();
    }

    /**
     * URL performance.
     */
    interface UrlPerformance {
        String getUrl();

        long getClicks();

        double getClickRate();
    }

    /**
     * Campaign comparison analytics.
     */
    interface CampaignComparisonAnalytics {
        List<CampaignMetrics> getCampaignMetrics();

        Map<String, Double> getPerformanceComparison();
    }

    /**
     * Time series performance data.
     */
    interface TimeSeriesPerformanceData {
        Instant getTimestamp();

        double getOpenRate();

        double getClickRate();

        long getNewOpens();

        long getNewClicks();
    }

    /**
     * Subscriber engagement analytics.
     */
    interface SubscriberEngagementAnalytics {
        long getTotalEmailsReceived();

        long getTotalOpens();

        long getTotalClicks();

        double getAverageOpenRate();

        double getAverageClickRate();

        List<EngagementHistory> getEngagementHistory();

        EngagementScore getEngagementScore();
    }

    /**
     * Engagement history.
     */
    interface EngagementHistory {
        Instant getDate();

        boolean isOpened();

        boolean isClicked();

        String getCampaignName();
    }

    /**
     * Engagement score.
     */
    interface EngagementScore {
        double getScore();

        String getGrade();

        List<String> getFactors();
    }

    /**
     * Subscriber segment analytics.
     */
    interface SubscriberSegmentAnalytics {
        long getSegmentSize();

        double getAverageOpenRate();

        double getAverageClickRate();

        Map<String, Double> getDemographicBreakdown();
    }

    /**
     * Subscriber lifecycle analytics.
     */
    interface SubscriberLifecycleAnalytics {
        long getNewSubscribers();

        long getConfirmedSubscribers();

        long getUnsubscribed();

        double getConfirmationRate();

        double getChurnRate();

        Map<String, Long> getLifecycleStageDistribution();
    }

    /**
     * Delivery performance analytics.
     */
    interface DeliveryPerformanceAnalytics {
        double getOverallDeliveryRate();

        double getOverallOpenRate();

        double getOverallClickRate();

        Map<String, Double> getDeliveryRateByTimeOfDay();

        Map<String, Double> getOpenRateByDayOfWeek();

        List<DeliveryIssue> getTopDeliveryIssues();
    }

    /**
     * Delivery issue.
     */
    interface DeliveryIssue {
        String getIssueType();

        long getCount();

        double getPercentage();
    }

    /**
     * Bounce analysis.
     */
    interface BounceAnalysis {
        long getTotalBounces();

        long getHardBounces();

        long getSoftBounces();

        Map<String, Long> getBounceReasons();

        List<String> getRecommendations();
    }

    /**
     * Geographic analytics.
     */
    interface GeographicAnalytics {
        Map<String, Long> getDeliveriesByCountry();

        Map<String, Double> getOpenRatesByCountry();

        Map<String, Double> getClickRatesByCountry();

        List<TopPerformingRegion> getTopPerformingRegions();
    }

    /**
     * Top performing region.
     */
    interface TopPerformingRegion {
        String getRegion();

        double getOpenRate();

        double getClickRate();

        long getTotalDeliveries();
    }

    /**
     * Content performance analytics.
     */
    interface ContentPerformanceAnalytics {
        Map<String, Double> getPerformanceByContentType();

        List<ContentElementPerformance> getElementPerformance();

        List<String> getContentRecommendations();
    }

    /**
     * Content element performance.
     */
    interface ContentElementPerformance {
        String getElementType();

        double getClickRate();

        long getTotalClicks();
    }

    /**
     * A/B test results.
     */
    interface ABTestResults {
        String getTestName();

        String getWinner();

        double getConfidenceLevel();

        Map<String, VariantResult> getVariantResults();

        List<String> getRecommendations();
    }

    /**
     * Variant result.
     */
    interface VariantResult {
        String getVariantName();

        long getSampleSize();

        double getOpenRate();

        double getClickRate();

        double getConversionRate();
    }

    /**
     * Subject line performance.
     */
    interface SubjectLinePerformance {
        String getSubjectLine();

        double getOpenRate();

        long getTotalOpens();

        long getTotalSends();

        String getPerformanceGrade();
    }

    /**
     * Revenue attribution.
     */
    interface RevenueAttribution {
        double getTotalRevenue();

        double getAttributedRevenue();

        double getAttributionRate();

        Map<String, Double> getRevenueByChannel();

        List<AttributionTouchpoint> getTouchpointAnalysis();
    }

    /**
     * Attribution touchpoint.
     */
    interface AttributionTouchpoint {
        String getTouchpoint();

        double getRevenue();

        long getConversions();

        double getAttributionWeight();
    }

    /**
     * Churn prediction analytics.
     */
    interface ChurnPredictionAnalytics {
        long getPredictedChurners();

        double getChurnRate();

        Map<String, Double> getChurnRiskBySegment();

        List<String> getRetentionRecommendations();
    }

    /**
     * Report types.
     */
    enum ReportType {
        SUMMARY,
        DETAILED,
        EXECUTIVE,
        TECHNICAL
    }

    /**
     * Campaign report.
     */
    interface CampaignReport {
        String getReportTitle();

        Instant getGeneratedAt();

        CampaignMetrics getMetrics();

        List<String> getKeyInsights();

        List<String> getRecommendations();

        byte[] getPdfContent();
    }

    /**
     * Subscriber report.
     */
    interface SubscriberReport {
        long getTotalSubscribers();

        long getActiveSubscribers();

        double getGrowthRate();

        Map<String, Long> getSubscribersByStatus();

        List<String> getKeyInsights();

        byte[] getPdfContent();
    }

    /**
     * Executive summary.
     */
    interface ExecutiveSummary {
        double getOverallOpenRate();

        double getOverallClickRate();

        double getRevenueGrowth();

        List<String> getTopPerformingCampaigns();

        List<String> getKeyOpportunities();

        List<String> getRisksAndChallenges();

        byte[] getPdfContent();
    }
}