package com.mmva.newsapp.controller.admin.newsletter;

import com.mmva.newsapp.domain.newsletter.service.analytics.NewsletterAnalyticsService;
import static com.mmva.newsapp.domain.newsletter.service.analytics.NewsletterAnalyticsService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST controller for newsletter analytics operations.
 *
 * <p>
 * Provides endpoints for comprehensive newsletter analytics
 * including campaign performance, subscriber insights, and reporting.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/newsletter/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Newsletter Analytics API", description = "Newsletter analytics and reporting operations")
public class NewsletterAnalyticsController {

    private final NewsletterAnalyticsService analyticsService;

    // =========================
    // Campaign Analytics
    // =========================

    /**
     * Gets comprehensive analytics for a specific campaign.
     *
     * @param campaignId the campaign ID
     * @return comprehensive campaign analytics
     */
    @GetMapping("/campaigns/{campaignId}")
    @Operation(summary = "Get campaign analytics", description = "Retrieves comprehensive analytics for a specific campaign")
    public ResponseEntity<ComprehensiveCampaignAnalytics> getCampaignAnalytics(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId) {

        log.debug("Get campaign analytics request for campaignId: {}", campaignId);

        ComprehensiveCampaignAnalytics analytics = analyticsService.getComprehensiveCampaignAnalytics(campaignId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Compares multiple campaigns.
     *
     * @param campaignIds list of campaign IDs to compare
     * @return campaign comparison analytics
     */
    @GetMapping("/campaigns/compare")
    @Operation(summary = "Compare campaigns", description = "Compares performance metrics across multiple campaigns")
    public ResponseEntity<CampaignComparisonAnalytics> compareCampaigns(
            @Parameter(description = "Campaign IDs to compare", example = "[1,2,3]") @RequestParam List<Long> campaignIds) {

        log.debug("Compare campaigns request for campaignIds: {}", campaignIds);

        CampaignComparisonAnalytics comparison = analyticsService.compareCampaigns(campaignIds);
        return ResponseEntity.ok(comparison);
    }

    /**
     * Gets campaign performance over time.
     *
     * @param campaignId the campaign ID
     * @param days       number of days to analyze
     * @return time-series performance data
     */
    @GetMapping("/campaigns/{campaignId}/performance")
    @Operation(summary = "Get campaign performance over time", description = "Retrieves time-series performance data for a campaign")
    public ResponseEntity<List<TimeSeriesPerformanceData>> getCampaignPerformanceOverTime(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "Number of days to analyze", example = "30") @RequestParam(defaultValue = "30") int days) {

        log.debug("Get campaign performance over time request - campaignId: {}, days: {}", campaignId, days);

        List<TimeSeriesPerformanceData> performance = analyticsService.getCampaignPerformanceOverTime(campaignId, days);
        return ResponseEntity.ok(performance);
    }

    // =========================
    // Subscriber Analytics
    // =========================

    /**
     * Gets subscriber engagement analytics.
     *
     * @param subscriberId the subscriber ID
     * @return subscriber engagement analytics
     */
    @GetMapping("/subscribers/{subscriberId}/engagement")
    @Operation(summary = "Get subscriber engagement", description = "Retrieves engagement analytics for a specific subscriber")
    public ResponseEntity<SubscriberEngagementAnalytics> getSubscriberEngagementAnalytics(
            @Parameter(description = "Subscriber ID", example = "1") @PathVariable Long subscriberId) {

        log.debug("Get subscriber engagement analytics request for subscriberId: {}", subscriberId);

        SubscriberEngagementAnalytics engagement = analyticsService.getSubscriberEngagementAnalytics(subscriberId);
        return ResponseEntity.ok(engagement);
    }

    /**
     * Gets subscriber segment analytics.
     *
     * @param segmentCriteria criteria for segmentation
     * @return segment analytics
     */
    @PostMapping("/subscribers/segment")
    @Operation(summary = "Get subscriber segment analytics", description = "Analyzes subscriber segments based on specified criteria")
    public ResponseEntity<SubscriberSegmentAnalytics> getSubscriberSegmentAnalytics(
            @RequestBody Map<String, Object> segmentCriteria) {

        log.debug("Get subscriber segment analytics request for criteria: {}", segmentCriteria);

        SubscriberSegmentAnalytics analytics = analyticsService.getSubscriberSegmentAnalytics(segmentCriteria);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Gets subscriber lifecycle analytics.
     *
     * @param days number of days to analyze
     * @return lifecycle analytics
     */
    @GetMapping("/subscribers/lifecycle")
    @Operation(summary = "Get subscriber lifecycle analytics", description = "Retrieves subscriber lifecycle metrics over specified days")
    public ResponseEntity<SubscriberLifecycleAnalytics> getSubscriberLifecycleAnalytics(
            @Parameter(description = "Number of days to analyze", example = "30") @RequestParam(defaultValue = "30") int days) {

        log.debug("Get subscriber lifecycle analytics request for days: {}", days);

        SubscriberLifecycleAnalytics analytics = analyticsService.getSubscriberLifecycleAnalytics(days);
        return ResponseEntity.ok(analytics);
    }

    // =========================
    // Delivery Analytics
    // =========================

    /**
     * Gets delivery performance analytics.
     *
     * @param startDate start date for the period
     * @param endDate   end date for the period
     * @return delivery performance analytics
     */
    @GetMapping("/delivery")
    @Operation(summary = "Get delivery analytics", description = "Retrieves delivery performance analytics for a date range")
    public ResponseEntity<DeliveryPerformanceAnalytics> getDeliveryPerformanceAnalytics(
            @Parameter(description = "Start date", example = "2024-01-01T00:00:00Z") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @Parameter(description = "End date", example = "2024-12-31T23:59:59Z") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        log.debug("Get delivery performance analytics request - startDate: {}, endDate: {}", startDate, endDate);

        DeliveryPerformanceAnalytics analytics = analyticsService.getDeliveryPerformanceAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Gets bounce analysis for a campaign.
     *
     * @param campaignId the campaign ID
     * @return bounce analysis
     */
    @GetMapping("/campaigns/{campaignId}/bounce-analysis")
    @Operation(summary = "Get bounce analysis", description = "Analyzes bounce patterns and reasons for a specific campaign")
    public ResponseEntity<BounceAnalysis> getBounceAnalysis(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId) {

        log.debug("Get bounce analysis request for campaignId: {}", campaignId);

        BounceAnalysis analysis = analyticsService.getBounceAnalysis(campaignId);
        return ResponseEntity.ok(analysis);
    }

    /**
     * Gets geographic analytics for a campaign.
     *
     * @param campaignId the campaign ID
     * @return geographic analytics
     */
    @GetMapping("/campaigns/{campaignId}/geographic")
    @Operation(summary = "Get geographic analytics", description = "Retrieves geographic performance data for a campaign")
    public ResponseEntity<GeographicAnalytics> getGeographicAnalytics(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId) {

        log.debug("Get geographic analytics request for campaignId: {}", campaignId);

        GeographicAnalytics analytics = analyticsService.getGeographicAnalytics(campaignId);
        return ResponseEntity.ok(analytics);
    }

    // =========================
    // Content Analytics
    // =========================

    /**
     * Gets content performance analytics for a campaign.
     *
     * @param campaignId the campaign ID
     * @return content performance analytics
     */
    @GetMapping("/campaigns/{campaignId}/content-performance")
    @Operation(summary = "Get content performance analytics", description = "Analyzes performance of different content elements in a campaign")
    public ResponseEntity<ContentPerformanceAnalytics> getContentPerformanceAnalytics(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId) {

        log.debug("Get content performance analytics request for campaignId: {}", campaignId);

        ContentPerformanceAnalytics analytics = analyticsService.getContentPerformanceAnalytics(campaignId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Gets A/B test results.
     *
     * @param testId the A/B test ID
     * @return A/B test results
     */
    @GetMapping("/ab-tests/{testId}/results")
    @Operation(summary = "Get A/B test results", description = "Retrieves results and analysis for a specific A/B test")
    public ResponseEntity<ABTestResults> getABTestResults(
            @Parameter(description = "A/B test ID", example = "test_123") @PathVariable String testId) {

        log.debug("Get A/B test results request for testId: {}", testId);

        ABTestResults results = analyticsService.getABTestResults(testId);
        return ResponseEntity.ok(results);
    }

    /**
     * Gets subject line performance for multiple campaigns.
     *
     * @param campaignIds list of campaign IDs to analyze
     * @return subject line performance data
     */
    @PostMapping("/subject-lines/performance")
    @Operation(summary = "Get subject line performance", description = "Analyzes performance of subject lines across multiple campaigns")
    public ResponseEntity<List<SubjectLinePerformance>> getSubjectLinePerformance(
            @RequestBody List<Long> campaignIds) {

        log.debug("Get subject line performance request for campaignIds: {}", campaignIds);

        List<SubjectLinePerformance> performance = analyticsService.getSubjectLinePerformance(campaignIds);
        return ResponseEntity.ok(performance);
    }

    // =========================
    // Business Intelligence
    // =========================

    /**
     * Gets revenue attribution for a campaign.
     *
     * @param campaignId            the campaign ID
     * @param attributionWindowDays attribution window in days
     * @return revenue attribution data
     */
    @GetMapping("/campaigns/{campaignId}/revenue-attribution")
    @Operation(summary = "Get revenue attribution", description = "Calculates revenue attribution from a campaign within specified window")
    public ResponseEntity<RevenueAttribution> getRevenueAttribution(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "Attribution window in days", example = "30") @RequestParam(defaultValue = "30") int attributionWindowDays) {

        log.debug("Get revenue attribution request - campaignId: {}, window: {} days", campaignId,
                attributionWindowDays);

        RevenueAttribution attribution = analyticsService.getRevenueAttribution(campaignId, attributionWindowDays);
        return ResponseEntity.ok(attribution);
    }

    /**
     * Gets customer lifetime value by acquisition source.
     *
     * @param startDate start date for analysis
     * @param endDate   end date for analysis
     * @return CLV by acquisition source
     */
    @GetMapping("/customer-lifetime-value")
    @Operation(summary = "Get customer lifetime value by source", description = "Calculates CLV metrics grouped by acquisition source")
    public ResponseEntity<Map<String, Double>> getCustomerLifetimeValueBySource(
            @Parameter(description = "Start date", example = "2024-01-01T00:00:00Z") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @Parameter(description = "End date", example = "2024-12-31T23:59:59Z") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        log.debug("Get CLV by source request - startDate: {}, endDate: {}", startDate, endDate);

        Map<String, Double> clvBySource = analyticsService.getCustomerLifetimeValueBySource(startDate, endDate);
        return ResponseEntity.ok(clvBySource);
    }

    /**
     * Gets churn prediction analytics.
     *
     * @param days number of days to look ahead
     * @return churn prediction data
     */
    @GetMapping("/churn-prediction")
    @Operation(summary = "Get churn prediction analytics", description = "Predicts churn rates and identifies at-risk subscribers")
    public ResponseEntity<ChurnPredictionAnalytics> getChurnPredictionAnalytics(
            @Parameter(description = "Days to look ahead", example = "30") @RequestParam(defaultValue = "30") int days) {

        log.debug("Get churn prediction analytics request for days: {}", days);

        ChurnPredictionAnalytics analytics = analyticsService.getChurnPredictionAnalytics(days);
        return ResponseEntity.ok(analytics);
    }

    // =========================
    // Reporting
    // =========================

    /**
     * Generates a comprehensive campaign report.
     *
     * @param campaignId the campaign ID
     * @param reportType the type of report
     * @return comprehensive campaign report
     */
    @GetMapping("/campaigns/{campaignId}/report")
    @Operation(summary = "Generate campaign report", description = "Generates a comprehensive report for a specific campaign")
    public ResponseEntity<CampaignReport> generateCampaignReport(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "Report type", example = "DETAILED") @RequestParam(defaultValue = "DETAILED") ReportType reportType) {

        log.debug("Generate campaign report request for campaignId: {}, type: {}", campaignId, reportType);

        CampaignReport report = analyticsService.generateCampaignReport(campaignId, reportType);
        return ResponseEntity.ok(report);
    }

    /**
     * Generates a subscriber report.
     *
     * @param startDate start date for report
     * @param endDate   end date for report
     * @return subscriber report
     */
    @GetMapping("/subscribers/report")
    @Operation(summary = "Generate subscriber report", description = "Generates a subscriber growth and engagement report")
    public ResponseEntity<SubscriberReport> generateSubscriberReport(
            @Parameter(description = "Start date", example = "2024-01-01T00:00:00Z") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @Parameter(description = "End date", example = "2024-12-31T23:59:59Z") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        log.debug("Generate subscriber report request - startDate: {}, endDate: {}", startDate, endDate);

        SubscriberReport report = analyticsService.generateSubscriberReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Generates an executive summary.
     *
     * @param startDate start date for summary
     * @param endDate   end date for summary
     * @return executive summary
     */
    @GetMapping("/executive-summary")
    @Operation(summary = "Generate executive summary", description = "Generates a high-level executive summary of newsletter performance")
    public ResponseEntity<ExecutiveSummary> generateExecutiveSummary(
            @Parameter(description = "Start date", example = "2024-01-01T00:00:00Z") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @Parameter(description = "End date", example = "2024-12-31T23:59:59Z") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        log.debug("Generate executive summary request - startDate: {}, endDate: {}", startDate, endDate);

        ExecutiveSummary summary = analyticsService.generateExecutiveSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
}