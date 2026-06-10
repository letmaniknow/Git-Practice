package com.mmva.newsapp.infrastructure.monetization.ads.external.common.service;

import com.google.api.client.auth.oauth2.Credential;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.AdProviderMetricsDto;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Base interface for all ad provider integrations
 * 
 * All ad providers (Google AdSense, Google AdMob, Facebook, etc.)
 * must implement this contract.
 * 
 * This enables:
 * - Polymorphic handling of multiple providers
 * - Consistent API across all providers
 * - Easy testing via mocks
 * - Future provider additions without core code changes
 * 
 * Naming Convention:
 * - All provider-specific implementations: {ProviderName}AdProviderService
 * - Example: GoogleAdsenseAdProviderService, GoogleAdmobAdProviderService
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface AdProviderService {

    /**
     * Get the type of provider this service implements
     * 
     * @return The provider type (GOOGLE_ADSENSE, GOOGLE_ADMOB, etc.)
     */
    ProviderType getAdProviderType();

    /**
     * Get provider display name
     * 
     * @return Human-readable provider name
     */
    String getAdProviderDisplayName();

    /**
     * Check if this provider is enabled in configuration
     * 
     * @return true if provider is enabled and ready to use
     */
    boolean isAdProviderEnabled();

    /**
     * Authenticate with the provider using authorization code
     * 
     * This implements OAuth 2.0 authorization code flow:
     * 1. User redirects to provider login
     * 2. Provider returns authorization code
     * 3. Backend exchanges code for access token
     * 4. Token stored for future API calls
     * 
     * @param authorizationCode Code from provider's OAuth callback
     * @return Access token for making authenticated API calls
     * @throws IOException if authentication fails
     */
    String authenticateWithProvider(String authorizationCode) throws IOException;

    /**
     * Get authorization URL for user to visit
     * 
     * This is the login link user clicks to authorize the app
     * 
     * @return Full URL user should visit for authorization
     * @throws IOException if URL generation fails
     */
    String getAdProviderAuthorizationUrl() throws IOException;

    /**
     * Fetch current metrics from provider API
     * 
     * Queries provider's API to get:
     * - Impressions, clicks, earnings
     * - Calculates CTR, CPM, CPC
     * - Normalizes data to standard format
     * 
     * @param credential Authenticated credential for API access
     * @param startDate  Period start (YYYY-MM-DD)
     * @param endDate    Period end (YYYY-MM-DD)
     * @return Normalized metrics in AdProviderMetricsDto format
     * @throws IOException if API call fails
     */
    AdProviderMetricsDto fetchAdProviderMetrics(
            Credential credential,
            LocalDate startDate,
            LocalDate endDate) throws IOException;

    /**
     * Sync metrics with local database
     * 
     * Periodic job that:
     * 1. Queries provider API
     * 2. Fetches latest metrics
     * 3. Stores in local database
     * 4. Enables historical analysis
     * 
     * Typically called hourly by scheduler
     * 
     * @param credential Authenticated credential for API access
     * @throws IOException if sync fails
     */
    void syncAdProviderMetricsToDatabase(Credential credential) throws IOException;

    /**
     * Get last sync timestamp
     * 
     * @return When metrics were last synchronized, null if never synced
     */
    String getLastAdProviderSyncTime();

    /**
     * Get provider configuration status
     * 
     * @return Human-readable status message
     */
    String getAdProviderConfigurationStatus();
}
