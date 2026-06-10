package com.mmva.newsapp.infrastructure.monetization.ads.external.facebook.config;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.config.ProviderConfigBase;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for Facebook Audience Network provider
 * 
 * Extends ProviderConfigBase to inherit common provider settings
 * Adds Facebook Audience Network specific configuration
 * 
 * YAML Configuration:
 * app:
 * monetization:
 * adproviders:
 * facebook-audience-network:
 * enabled: true
 * sync-interval-hours: 1
 * app-id: YOUR_APP_ID
 * app-secret: YOUR_APP_SECRET
 * access-token: YOUR_ACCESS_TOKEN
 * publisher-id: YOUR_PUBLISHER_ID
 * api-version: v18.0
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "app.monetization.adproviders.facebook-audience-network")
@Getter
@Setter
public class FacebookAudienceNetworkConfig extends ProviderConfigBase {

    /**
     * Facebook App ID
     * Obtained from Facebook App Dashboard
     */
    private String adProviderAppId = "dummy-app-id-placeholder";

    /**
     * Facebook App Secret
     * Obtained from Facebook App Dashboard
     */
    private String adProviderAppSecret = "dummy-app-secret-placeholder";

    /**
     * Facebook User Access Token for API calls
     * Generated via Facebook Graph API
     */
    private String adProviderAccessToken = "dummy-access-token-placeholder";

    /**
     * Facebook Publisher ID
     * Format: pub_xxxxxxxxxxxxxxxx
     */
    private String adProviderPublisherId = "pub-dummy-publisher-id";

    /**
     * Facebook Graph API version
     * Example: v18.0, v17.0
     */
    private String adProviderApiVersion = "v18.0";

    /**
     * API endpoint for Facebook Graph API
     */
    private String adProviderApiEndpoint = "https://graph.facebook.com";

    /**
     * Maximum number of days to lookback for metrics
     */
    private int adProviderMaxMetricsLookbackDays = 90;

    /**
     * Whether to include breakdown by placement (app name, platform, etc.)
     */
    private boolean adProviderIncludeBreakdown = true;

    /**
     * Whether to include breakdown by country
     */
    private boolean adProviderIncludeCountryBreakdown = false;

    @Override
    public boolean validateAdProviderConfiguration() {
        // Check if enabled
        if (!isAdProviderEnabled()) {
            return true;
        }

        // Validate required fields
        boolean isAppIdValid = adProviderAppId != null &&
                !adProviderAppId.isEmpty() &&
                !adProviderAppId.startsWith("dummy");

        boolean isAppSecretValid = adProviderAppSecret != null &&
                !adProviderAppSecret.isEmpty() &&
                !adProviderAppSecret.startsWith("dummy");

        boolean isAccessTokenValid = adProviderAccessToken != null &&
                !adProviderAccessToken.isEmpty() &&
                !adProviderAccessToken.startsWith("dummy");

        boolean isPublisherIdValid = adProviderPublisherId != null &&
                !adProviderPublisherId.isEmpty() &&
                adProviderPublisherId.startsWith("pub_") &&
                !adProviderPublisherId.startsWith("pub_dummy");

        return isAppIdValid && isAppSecretValid && isAccessTokenValid && isPublisherIdValid;
    }

    @Override
    public String getAdProviderConfigurationStatus() {
        if (!isAdProviderEnabled()) {
            return "Facebook Audience Network: DISABLED";
        }

        boolean configValid = validateAdProviderConfiguration();
        if (configValid) {
            return "Facebook Audience Network: ✅ CONFIGURED (ready for production)";
        }

        StringBuilder status = new StringBuilder("Facebook Audience Network: ⚠️  INCOMPLETE CONFIG - ");

        if (adProviderAppId == null || adProviderAppId.isEmpty() || adProviderAppId.startsWith("dummy")) {
            status.append("Missing APP_ID; ");
        }

        if (adProviderAppSecret == null || adProviderAppSecret.isEmpty() || adProviderAppSecret.startsWith("dummy")) {
            status.append("Missing APP_SECRET; ");
        }

        if (adProviderAccessToken == null || adProviderAccessToken.isEmpty()
                || adProviderAccessToken.startsWith("dummy")) {
            status.append("Missing ACCESS_TOKEN; ");
        }

        if (adProviderPublisherId == null || !adProviderPublisherId.startsWith("pub_")
                || adProviderPublisherId.startsWith("pub_dummy")) {
            status.append("Invalid PUBLISHER_ID; ");
        }

        return status.toString();
    }
}
