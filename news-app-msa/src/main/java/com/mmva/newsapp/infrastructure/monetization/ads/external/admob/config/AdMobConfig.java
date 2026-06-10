package com.mmva.newsapp.infrastructure.monetization.ads.external.admob.config;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.config.ProviderConfigBase;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for Google AdMob provider
 * 
 * Extends ProviderConfigBase to inherit common provider settings
 * Adds Google AdMob specific configuration
 * 
 * YAML Configuration:
 * app:
 * monetization:
 * adproviders:
 * google-admob:
 * enabled: true
 * sync-interval-hours: 1
 * client-id: YOUR_CLIENT_ID
 * client-secret: YOUR_CLIENT_SECRET
 * app-id: ca-app-YOUR_APP_ID
 * redirect-uri:
 * http://localhost:8080/api/v1/admin/ad-providers/google-admob/oauth-callback
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "app.monetization.adproviders.google-admob")
@Getter
@Setter
public class AdMobConfig extends ProviderConfigBase {

    /**
     * Google OAuth 2.0 Client ID for AdMob
     * Obtained from Google Cloud Console
     */
    private String adProviderClientId = "dummy-client-id-placeholder";

    /**
     * Google OAuth 2.0 Client Secret for AdMob
     * Obtained from Google Cloud Console
     */
    private String adProviderClientSecret = "dummy-client-secret-placeholder";

    /**
     * Google AdMob App ID
     * Format: ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
     */
    private String adProviderAppId = "ca-app-pub-dummy-app-id";

    /**
     * OAuth 2.0 Redirect URI
     * Must match exactly with Google Cloud Console configuration
     */
    private String adProviderRedirectUri = "http://localhost:8080/api/v1/admin/ad-providers/google-admob/oauth-callback";

    /**
     * API endpoint for AdMob (can vary by environment)
     */
    private String adProviderApiEndpoint = "https://admob.googleapis.com/v1";

    /**
     * OAuth token storage directory path
     */
    private String adProviderTokenStoragePath = "tokens/google-admob";

    /**
     * Maximum number of days to lookback for metrics
     */
    private int adProviderMaxMetricsLookbackDays = 90;

    /**
     * Whether to enable automatic token refresh
     */
    private boolean adProviderAutoRefreshToken = true;

    /**
     * Array of App IDs to sync metrics for (for multi-app publishers)
     */
    private String[] adProviderAppIds = new String[] {};

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

        boolean isAppIdValid = adProviderAppId != null &&
                !adProviderAppId.isEmpty() &&
                adProviderAppId.startsWith("ca-app-pub-") &&
                !adProviderAppId.startsWith("ca-app-pub-dummy");

        return isClientIdValid && isClientSecretValid && isAppIdValid;
    }

    @Override
    public String getAdProviderConfigurationStatus() {
        if (!isAdProviderEnabled()) {
            return "Google AdMob: DISABLED";
        }

        boolean configValid = validateAdProviderConfiguration();
        if (configValid) {
            return "Google AdMob: ✅ CONFIGURED (ready for production)";
        }

        StringBuilder status = new StringBuilder("Google AdMob: ⚠️  INCOMPLETE CONFIG - ");

        if (adProviderClientId == null || adProviderClientId.isEmpty() || adProviderClientId.startsWith("dummy")) {
            status.append("Missing CLIENT_ID; ");
        }

        if (adProviderClientSecret == null || adProviderClientSecret.isEmpty()
                || adProviderClientSecret.startsWith("dummy")) {
            status.append("Missing CLIENT_SECRET; ");
        }

        if (adProviderAppId == null || !adProviderAppId.startsWith("ca-app-pub-")
                || adProviderAppId.startsWith("ca-app-pub-dummy")) {
            status.append("Invalid APP_ID; ");
        }

        return status.toString();
    }
}
