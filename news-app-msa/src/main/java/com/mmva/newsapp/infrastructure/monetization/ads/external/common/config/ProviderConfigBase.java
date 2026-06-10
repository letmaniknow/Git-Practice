package com.mmva.newsapp.infrastructure.monetization.ads.external.common.config;

/**
 * Base configuration for all ad providers
 * 
 * Provides common properties that all providers share:
 * - Whether provider is enabled
 * - API credentials (partially - specific details in subclasses)
 * - Sync scheduling configuration
 * 
 * Naming Convention:
 * - Concrete implementations: {ProviderName}Config
 * - Example: GoogleAdsenseConfig, GoogleAdmobConfig, FacebookConfig
 * - Property prefix: app.monetization.adproviders.{provider-code}
 * 
 * YAML Example:
 * app:
 * monetization:
 * adproviders:
 * google-adsense:
 * enabled: true
 * sync-interval-hours: 1
 * google-admob:
 * enabled: true
 * sync-interval-hours: 1
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public abstract class ProviderConfigBase {

    /**
     * Whether this provider is enabled
     * 
     * If false, provider is skipped in:
     * - Sync jobs
     * - Factory instantiation
     * - Controller endpoints
     */
    protected boolean adProviderEnabled;

    /**
     * How often (in hours) to sync metrics from provider
     * 
     * Typical values:
     * - 1 hour for high-volume sites
     * - 6 hours for medium sites
     * - 24 hours for low-volume sites
     */
    protected int adProviderSyncIntervalHours = 1;

    /**
     * Maximum number of retries if sync fails
     */
    protected int adProviderSyncMaxRetries = 3;

    /**
     * Whether to store raw provider responses for debugging
     */
    protected boolean adProviderStoreRawResponses = false;

    /**
     * Timeout in seconds for API calls
     */
    protected int adProviderApiTimeoutSeconds = 30;

    // Getters and setters following naming convention

    public boolean isAdProviderEnabled() {
        return adProviderEnabled;
    }

    public void setAdProviderEnabled(boolean adProviderEnabled) {
        this.adProviderEnabled = adProviderEnabled;
    }

    public int getAdProviderSyncIntervalHours() {
        return adProviderSyncIntervalHours;
    }

    public void setAdProviderSyncIntervalHours(int adProviderSyncIntervalHours) {
        this.adProviderSyncIntervalHours = adProviderSyncIntervalHours;
    }

    public int getAdProviderSyncMaxRetries() {
        return adProviderSyncMaxRetries;
    }

    public void setAdProviderSyncMaxRetries(int adProviderSyncMaxRetries) {
        this.adProviderSyncMaxRetries = adProviderSyncMaxRetries;
    }

    public boolean isAdProviderStoreRawResponses() {
        return adProviderStoreRawResponses;
    }

    public void setAdProviderStoreRawResponses(boolean adProviderStoreRawResponses) {
        this.adProviderStoreRawResponses = adProviderStoreRawResponses;
    }

    public int getAdProviderApiTimeoutSeconds() {
        return adProviderApiTimeoutSeconds;
    }

    public void setAdProviderApiTimeoutSeconds(int adProviderApiTimeoutSeconds) {
        this.adProviderApiTimeoutSeconds = adProviderApiTimeoutSeconds;
    }

    /**
     * Validate configuration
     * 
     * Subclasses must implement to check provider-specific required fields
     * 
     * @return true if all required fields are present and valid
     */
    public abstract boolean validateAdProviderConfiguration();

    /**
     * Get configuration status message
     * 
     * @return Human-readable status
     */
    public abstract String getAdProviderConfigurationStatus();
}
