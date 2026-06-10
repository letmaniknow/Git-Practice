package com.mmva.newsapp.infrastructure.monetization.ads.external.google.config;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.config.ProviderConfigBase;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for Google AdSense provider
 * 
 * Extends ProviderConfigBase to inherit common provider settings
 * Adds Google AdSense specific configuration
 * 
 * YAML Configuration:
 * app:
 * monetization:
 * adproviders:
 * google-adsense:
 * enabled: true
 * sync-interval-hours: 1
 * client-id: YOUR_CLIENT_ID
 * client-secret: YOUR_CLIENT_SECRET
 * publisher-id: pub-YOUR_PUBLISHER_ID
 * redirect-uri:
 * http://localhost:8080/api/v1/admin/ad-providers/google-adsense/oauth-callback
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "app.monetization.adproviders.google-adsense")
@Getter
@Setter
public class GoogleAdsenseConfig extends ProviderConfigBase {

    /**
     * Google OAuth 2.0 Client ID
     * Obtained from Google Cloud Console
     */
    private String adProviderClientId = "dummy-client-id-placeholder";

    /**
     * Google OAuth 2.0 Client Secret
     * Obtained from Google Cloud Console
     */
    private String adProviderClientSecret = "dummy-client-secret-placeholder";

    /**
     * Google AdSense Publisher ID
     * Format: pub-XXXXXXXXXXXXXXXX
     */
    private String adProviderPublisherId = "pub-dummy-publisher-id";

    /**
     * OAuth 2.0 Redirect URI
     * Must match exactly with Google Cloud Console configuration
     */
    private String adProviderRedirectUri = "http://localhost:8080/api/v1/admin/ad-providers/google-adsense/oauth-callback";

    /**
     * API endpoint for AdSense (can vary by environment)
     */
    private String adProviderApiEndpoint = "https://adsense.googleapis.com/v2";

    /**
     * OAuth token storage directory path
     */
    private String adProviderTokenStoragePath = "tokens/google-adsense";

    /**
     * Maximum number of days to lookback for metrics
     */
    private int adProviderMaxMetricsLookbackDays = 90;

    /**
     * Whether to enable automatic token refresh
     */
    private boolean adProviderAutoRefreshToken = true;

    @Override
    public boolean validateAdProviderConfiguration() {
        // Check if enabled
        if (!isAdProviderEnabled()) {
            return true;
        }

        // Validate required fields
        boolean isClientIdValid = adProviderClientId != null &&
                !adProviderClientId.isEmpty() &&
                !adProviderClientId.startsWith("dummy");

        boolean isClientSecretValid = adProviderClientSecret != null &&
                !adProviderClientSecret.isEmpty() &&
                !adProviderClientSecret.startsWith("dummy");

        boolean isPublisherIdValid = adProviderPublisherId != null &&
                !adProviderPublisherId.isEmpty() &&
                adProviderPublisherId.startsWith("pub-") &&
                !adProviderPublisherId.startsWith("pub-dummy");

        return isClientIdValid && isClientSecretValid && isPublisherIdValid;
    }

    @Override
    public String getAdProviderConfigurationStatus() {
        if (!isAdProviderEnabled()) {
            return "Google AdSense: DISABLED";
        }

        boolean configValid = validateAdProviderConfiguration();
        if (configValid) {
            return "Google AdSense: ✅ CONFIGURED (ready for production)";
        }

        StringBuilder status = new StringBuilder("Google AdSense: ⚠️  INCOMPLETE CONFIG - ");

        if (adProviderClientId == null || adProviderClientId.isEmpty() || adProviderClientId.startsWith("dummy")) {
            status.append("Missing CLIENT_ID; ");
        }

        if (adProviderClientSecret == null || adProviderClientSecret.isEmpty()
                || adProviderClientSecret.startsWith("dummy")) {
            status.append("Missing CLIENT_SECRET; ");
        }

        if (adProviderPublisherId == null || !adProviderPublisherId.startsWith("pub-")
                || adProviderPublisherId.startsWith("pub-dummy")) {
            status.append("Invalid PUBLISHER_ID; ");
        }

        return status.toString();
    }
}
