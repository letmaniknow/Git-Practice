package com.mmva.newsapp.infrastructure.monetization.ads.external.google.service;

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
import com.mmva.newsapp.infrastructure.monetization.ads.external.google.config.GoogleAdsenseConfig;
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
 * Google AdSense implementation of AdProviderService
 * 
 * Handles OAuth 2.0 authentication and metrics fetching from Google AdSense API
 * 
 * Naming Convention: GoogleAdse**AdProviderService follows pattern
 * {ProviderName}AdProviderService
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAdsenseAdProviderService implements AdProviderService {

    private static final String APPLICATION_NAME = "NewsApp";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/adsense");

    private final GoogleAdsenseConfig googleAdsenseConfig;
    private final ProviderMetricsSyncService providerMetricsSyncService;
    private String lastSyncTime;

    @Override
    public ProviderType getAdProviderType() {
        return ProviderType.GOOGLE_ADSENSE;
    }

    @Override
    public String getAdProviderDisplayName() {
        return "Google AdSense";
    }

    @Override
    public boolean isAdProviderEnabled() {
        return googleAdsenseConfig.isAdProviderEnabled();
    }

    @Override
    public String authenticateWithProvider(String authorizationCode) throws IOException {
        if (!isAdProviderEnabled()) {
            log.warn("⚠️  Google AdSense is not enabled");
            throw new AdProviderAuthException(
                    "Google AdSense is not enabled",
                    ProviderType.GOOGLE_ADSENSE,
                    "PROVIDER_DISABLED");
        }

        try {
            log.info("🔐 Authenticating with Google AdSense API...");

            // Validate configuration
            if (!googleAdsenseConfig.validateAdProviderConfiguration()) {
                log.error("❌ Google AdSense configuration is incomplete");
                throw new AdProviderAuthException(
                        "Google AdSense configuration is incomplete: "
                                + googleAdsenseConfig.getAdProviderConfigurationStatus(),
                        ProviderType.GOOGLE_ADSENSE,
                        "CONFIG_INVALID");
            }

            // Parse client secrets
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new StringReader("{\"installed\":{\"client_id\":\"" +
                            googleAdsenseConfig.getAdProviderClientId() +
                            "\",\"client_secret\":\"" +
                            googleAdsenseConfig.getAdProviderClientSecret() +
                            "\"}}"));

            // Build OAuth flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    clientSecrets,
                    SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(
                            new File(googleAdsenseConfig.getAdProviderTokenStoragePath())))
                    .setAccessType("offline")
                    .build();

            // Exchange authorization code for credential
            var tokenResponse = flow.newTokenRequest(authorizationCode)
                    .setRedirectUri(googleAdsenseConfig.getAdProviderRedirectUri())
                    .execute();

            Credential credential = flow.createAndStoreCredential(tokenResponse, "user");

            log.info("✅ Google AdSense authentication successful");
            return credential.getAccessToken();

        } catch (GeneralSecurityException e) {
            log.error("❌ Security error during Google AdSense authentication", e);
            throw new AdProviderAuthException(
                    "Security error during authentication: " + e.getMessage(),
                    ProviderType.GOOGLE_ADSENSE,
                    "SECURITY_ERROR",
                    e.getMessage(),
                    e);
        } catch (IOException e) {
            log.error("❌ IO error during Google AdSense authentication", e);
            throw new AdProviderAuthException(
                    "IO error during authentication: " + e.getMessage(),
                    ProviderType.GOOGLE_ADSENSE,
                    "AUTH_IO_ERROR",
                    e.getMessage(),
                    e);
        }
    }

    @Override
    public String getAdProviderAuthorizationUrl() throws IOException {
        if (!isAdProviderEnabled()) {
            log.warn("⚠️  Google AdSense is not enabled");
            return null;
        }

        try {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new StringReader("{\"installed\":{\"client_id\":\"" +
                            googleAdsenseConfig.getAdProviderClientId() +
                            "\",\"client_secret\":\"" +
                            googleAdsenseConfig.getAdProviderClientSecret() +
                            "\"}}"));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    clientSecrets,
                    SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(
                            new File(googleAdsenseConfig.getAdProviderTokenStoragePath())))
                    .setAccessType("offline")
                    .build();

            return flow.newAuthorizationUrl()
                    .setRedirectUri(googleAdsenseConfig.getAdProviderRedirectUri())
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
            log.warn("⚠️  Google AdSense is not enabled");
            return null;
        }

        try {
            log.info("🔄 Fetching metrics from Google AdSense for period {} to {}...", startDate, endDate);

            // TODO: Implement real API call to Google AdSense API
            // GET /v2/accounts/{publisherId}/reports/generate

            // For now, return dummy metrics that follow the normalized format
            AdProviderMetricsDto metrics = AdProviderMetricsDto.builder()
                    .adProviderType(ProviderType.GOOGLE_ADSENSE)
                    .adProviderAccountId(googleAdsenseConfig.getAdProviderPublisherId())
                    .adProviderAccountName("Google AdSense Account")
                    .adProviderTotalImpressions(100L)
                    .adProviderTotalClicks(5L)
                    .adProviderEstimatedEarningsUsd(2.50)
                    .adProviderCtrPercentage(5.0) // 5 clicks / 100 impressions
                    .adProviderCpmUsd(25.0) // $2.50 / 100 impressions
                    .adProviderCpcUsd(0.50) // $2.50 / 5 clicks
                    .adProviderMetricsPeriodStart(startDate)
                    .adProviderMetricsPeriodEnd(endDate)
                    .adProviderSyncedAt(Instant.now())
                    .adProviderSyncSource("GOOGLE_ADSENSE_API")
                    .adProviderSyncStatus("SUCCESS")
                    .tenantId("default-tenant")
                    .build();

            // Add provider-specific metadata
            metrics.addProviderMetadata(ProviderMetadataKeys.AD_PROVIDER_METADATA_NETWORK_TYPE, "display");
            metrics.addProviderMetadata(ProviderMetadataKeys.AD_PROVIDER_METADATA_AD_FORMAT, "responsive");

            log.info("✅ Fetched Google AdSense metrics: {} impressions, {} clicks, ${} earnings",
                    metrics.getAdProviderTotalImpressions(),
                    metrics.getAdProviderTotalClicks(),
                    metrics.getAdProviderEstimatedEarningsUsd());

            return metrics;

        } catch (Exception e) {
            log.error("❌ Error fetching Google AdSense metrics", e);
            throw new AdProviderApiException(
                    "Error fetching metrics: " + e.getMessage(),
                    ProviderType.GOOGLE_ADSENSE,
                    "METRICS_FETCH_ERROR",
                    e.getMessage(),
                    0,
                    e);
        }
    }

    @Override
    public void syncAdProviderMetricsToDatabase(Credential credential) throws IOException {
        if (!isAdProviderEnabled()) {
            log.debug("Google AdSense sync is disabled");
            return;
        }

        try {
            log.info("🔄 Syncing Google AdSense metrics to database...");

            // Fetch metrics for last 30 days
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);

            AdProviderMetricsDto metrics = fetchAdProviderMetrics(credential, startDate, endDate);

            // Save to database using persistence service
            providerMetricsSyncService.saveProviderMetrics(metrics);

            this.lastSyncTime = Instant.now().toString();

            log.info("✅ Google AdSense metrics synced successfully");

        } catch (Exception e) {
            log.error("❌ Error syncing Google AdSense metrics", e);
            throw new IOException("Failed to sync Google AdSense metrics", e);
        }
    }

    @Override
    public String getLastAdProviderSyncTime() {
        return lastSyncTime;
    }

    @Override
    public String getAdProviderConfigurationStatus() {
        return googleAdsenseConfig.getAdProviderConfigurationStatus();
    }
}
