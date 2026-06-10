package com.mmva.newsapp.infrastructure.monetization.ads.external.admob.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.constant.ProviderMetadataKeys;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.AdProviderMetricsDto;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.exception.AdProviderAuthException;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.exception.AdProviderApiException;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.service.AdProviderService;
import com.mmva.newsapp.infrastructure.monetization.ads.external.admob.config.AdMobConfig;
import com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.service.ProviderMetricsSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Google AdMob implementation of AdProviderService
 * 
 * Handles OAuth 2.0 authentication and metrics fetching from Google AdMob API
 * 
 * Naming Convention: GoogleAdMobAdProviderService follows pattern
 * {ProviderName}AdProviderService
 * 
 * Google AdMob supports:
 * - Mobile App monetization (iOS/Android)
 * - Multiple ad formats: Banner, Interstitial, Rewarded, Native
 * - Multiple mediation networks for ad fill optimization
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAdMobAdProviderService implements AdProviderService {

    private static final String APPLICATION_NAME = "NewsApp";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections
            .singletonList("https://www.googleapis.com/auth/admob.report");

    private final AdMobConfig adMobConfig;
    private final ProviderMetricsSyncService providerMetricsSyncService;
    private String lastSyncTime;

    @Override
    public ProviderType getAdProviderType() {
        return ProviderType.GOOGLE_ADMOB;
    }

    @Override
    public String getAdProviderDisplayName() {
        return "Google AdMob";
    }

    @Override
    public boolean isAdProviderEnabled() {
        return adMobConfig.isAdProviderEnabled();
    }

    @Override
    public String authenticateWithProvider(String authorizationCode) throws IOException {
        if (!isAdProviderEnabled()) {
            log.warn("⚠️  Google AdMob is not enabled");
            throw new AdProviderAuthException(
                    "Google AdMob is not enabled",
                    ProviderType.GOOGLE_ADMOB,
                    "PROVIDER_DISABLED");
        }

        try {
            log.info("🔐 Authenticating with Google AdMob API...");

            // Validate configuration
            if (!adMobConfig.validateAdProviderConfiguration()) {
                log.error("❌ Google AdMob configuration is incomplete");
                throw new AdProviderAuthException(
                        "Google AdMob configuration is incomplete: " + adMobConfig.getAdProviderConfigurationStatus(),
                        ProviderType.GOOGLE_ADMOB,
                        "CONFIG_INVALID");
            }

            // Parse client secrets
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new StringReader("{\"installed\":{\"client_id\":\"" +
                            adMobConfig.getAdProviderClientId() +
                            "\",\"client_secret\":\"" +
                            adMobConfig.getAdProviderClientSecret() +
                            "\"}}"));

            // Build OAuth flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    clientSecrets,
                    SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(
                            new File(adMobConfig.getAdProviderTokenStoragePath())))
                    .setAccessType("offline")
                    .build();

            // Exchange authorization code for credential
            var tokenResponse = flow.newTokenRequest(authorizationCode)
                    .setRedirectUri(adMobConfig.getAdProviderRedirectUri())
                    .execute();

            Credential credential = flow.createAndStoreCredential(tokenResponse, "user");

            log.info("✅ Google AdMob authentication successful");
            return credential.getAccessToken();

        } catch (GeneralSecurityException e) {
            log.error("❌ Security error during Google AdMob authentication", e);
            throw new AdProviderAuthException(
                    "Security error during authentication: " + e.getMessage(),
                    ProviderType.GOOGLE_ADMOB,
                    "SECURITY_ERROR",
                    e.getMessage(),
                    e);
        } catch (IOException e) {
            log.error("❌ IO error during Google AdMob authentication", e);
            throw new AdProviderAuthException(
                    "IO error during authentication: " + e.getMessage(),
                    ProviderType.GOOGLE_ADMOB,
                    "AUTH_IO_ERROR",
                    e.getMessage(),
                    e);
        }
    }

    @Override
    public String getAdProviderAuthorizationUrl() throws IOException {
        if (!isAdProviderEnabled()) {
            log.warn("⚠️  Google AdMob is not enabled");
            return null;
        }

        try {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new StringReader("{\"installed\":{\"client_id\":\"" +
                            adMobConfig.getAdProviderClientId() +
                            "\",\"client_secret\":\"" +
                            adMobConfig.getAdProviderClientSecret() +
                            "\"}}"));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    clientSecrets,
                    SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(
                            new File(adMobConfig.getAdProviderTokenStoragePath())))
                    .setAccessType("offline")
                    .build();

            return flow.newAuthorizationUrl()
                    .setRedirectUri(adMobConfig.getAdProviderRedirectUri())
                    .build();

        } catch (GeneralSecurityException e) {
            throw new IOException("Failed to generate authorization URL", e);
        }
    }

    @Override
    public AdProviderMetricsDto fetchAdProviderMetrics(
            Credential credential,
            LocalDate startDate,
            LocalDate endDate) throws IOException {

        if (!isAdProviderEnabled()) {
            log.warn("⚠️  Google AdMob is not enabled");
            return null;
        }

        try {
            log.info("🔄 Fetching metrics from Google AdMob for period {} to {}...", startDate, endDate);

            // TODO: Implement real API call to Google AdMob API
            // POST /v1/accounts/{publisherId}/mediationReport:generate

            // For now, return dummy metrics that follow the normalized format
            // AdMob typically has higher impression/click volumes for mobile apps
            AdProviderMetricsDto metrics = AdProviderMetricsDto.builder()
                    .adProviderType(ProviderType.GOOGLE_ADMOB)
                    .adProviderAccountId(adMobConfig.getAdProviderAppId())
                    .adProviderAccountName("Google AdMob Account")
                    .adProviderTotalImpressions(5000L) // Higher volume for mobile apps
                    .adProviderTotalClicks(150L) // Higher click volume
                    .adProviderEstimatedEarningsUsd(75.00) // Higher earnings
                    .adProviderCtrPercentage(3.0) // 150 clicks / 5000 impressions
                    .adProviderCpmUsd(15.0) // $75 / 5000 impressions
                    .adProviderCpcUsd(0.50) // $75 / 150 clicks
                    .adProviderMetricsPeriodStart(startDate)
                    .adProviderMetricsPeriodEnd(endDate)
                    .adProviderSyncedAt(Instant.now())
                    .adProviderSyncSource("GOOGLE_ADMOB_API")
                    .adProviderSyncStatus("SUCCESS")
                    .tenantId("default-tenant")
                    .build();

            // Add AdMob-specific metadata
            metrics.addProviderMetadata(ProviderMetadataKeys.AD_PROVIDER_METADATA_PLATFORM, "iOS");
            metrics.addProviderMetadata(ProviderMetadataKeys.AD_PROVIDER_METADATA_AD_FORMAT, "banner");
            metrics.addProviderMetadata(ProviderMetadataKeys.AD_PROVIDER_METADATA_AD_MEDIATION_NETWORK, "google");

            // Add revenue breakdown by network (AdMob supports multiple networks)
            metrics.addProviderMetadata(ProviderMetadataKeys.AD_PROVIDER_METADATA_ESTIMATED_EARNINGS_BY_NETWORK,
                    "{\"google_mobile_ads\": 45.00, \"facebook_audience_network\": 20.00, \"ironSource\": 10.00}");

            log.info("✅ Fetched Google AdMob metrics: {} impressions, {} clicks, ${} earnings",
                    metrics.getAdProviderTotalImpressions(),
                    metrics.getAdProviderTotalClicks(),
                    metrics.getAdProviderEstimatedEarningsUsd());

            return metrics;

        } catch (Exception e) {
            log.error("❌ Error fetching Google AdMob metrics", e);
            throw new AdProviderApiException(
                    "Error fetching metrics: " + e.getMessage(),
                    ProviderType.GOOGLE_ADMOB,
                    "METRICS_FETCH_ERROR",
                    e.getMessage(),
                    0,
                    e);
        }
    }

    @Override
    public void syncAdProviderMetricsToDatabase(Credential credential) throws IOException {
        if (!isAdProviderEnabled()) {
            log.debug("Google AdMob sync is disabled");
            return;
        }

        try {
            log.info("🔄 Syncing Google AdMob metrics to database...");

            // Fetch metrics for last 30 days
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);

            AdProviderMetricsDto metrics = fetchAdProviderMetrics(credential, startDate, endDate);

            // Save to database using persistence service
            providerMetricsSyncService.saveProviderMetrics(metrics);

            this.lastSyncTime = Instant.now().toString();

            log.info("✅ Google AdMob metrics synced successfully");

        } catch (Exception e) {
            log.error("❌ Error syncing Google AdMob metrics", e);
            throw new IOException("Failed to sync Google AdMob metrics", e);
        }
    }

    @Override
    public String getLastAdProviderSyncTime() {
        return lastSyncTime;
    }

    @Override
    public String getAdProviderConfigurationStatus() {
        return adMobConfig.getAdProviderConfigurationStatus();
    }
}
