package com.mmva.newsapp.infrastructure.monetization.ads.external.factory;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.exception.AdProviderException;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.service.AdProviderService;
import com.mmva.newsapp.infrastructure.monetization.ads.external.google.service.GoogleAdsenseAdProviderService;
import com.mmva.newsapp.infrastructure.monetization.ads.external.admob.service.GoogleAdMobAdProviderService;
import com.mmva.newsapp.infrastructure.monetization.ads.external.facebook.service.FacebookAudienceNetworkAdProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory for creating ad provider service instances
 * 
 * Implements Factory Pattern to centralize provider instantiation
 * 
 * Benefits:
 * - Single point to add new providers
 * - Decouples controllers from specific provider implementations
 * - Easy to enable/disable providers
 * - Supports dependency injection per provider
 * 
 * Usage:
 * AdProviderService service =
 * adProviderFactory.getAdProviderService(ProviderType.GOOGLE_ADSENSE);
 * 
 * Naming Convention: AdProviderFactory (Factory suffix indicates factory
 * pattern)
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdProviderFactory {

    private final GoogleAdsenseAdProviderService googleAdsenseAdProviderService;
    private final GoogleAdMobAdProviderService googleAdMobAdProviderService;
    private final FacebookAudienceNetworkAdProviderService facebookAudienceNetworkAdProviderService;

    /**
     * Get provider service instance for given provider type
     * 
     * @param adProviderType The provider type (GOOGLE_ADSENSE, GOOGLE_ADMOB, etc.)
     * @return Provider service implementation
     * @throws AdProviderException if provider type not supported or not enabled
     */
    public AdProviderService getAdProviderService(ProviderType adProviderType) {
        if (adProviderType == null) {
            log.error("❌ Provider type cannot be null");
            throw new AdProviderException(
                    "Provider type cannot be null",
                    null,
                    "INVALID_PROVIDER_TYPE");
        }

        log.debug("🔍 Getting provider service for: {}", adProviderType.getAdProviderDisplayName());

        return switch (adProviderType) {
            case GOOGLE_ADSENSE -> {
                if (!googleAdsenseAdProviderService.isAdProviderEnabled()) {
                    log.warn("⚠️  Google AdSense provider is not enabled");
                    throw new AdProviderException(
                            "Google AdSense provider is not enabled",
                            adProviderType,
                            "PROVIDER_NOT_ENABLED");
                }
                yield googleAdsenseAdProviderService;
            }

            case GOOGLE_ADMOB -> {
                if (!googleAdMobAdProviderService.isAdProviderEnabled()) {
                    log.warn("⚠️  Google AdMob provider is not enabled");
                    throw new AdProviderException(
                            "Google AdMob provider is not enabled",
                            adProviderType,
                            "PROVIDER_NOT_ENABLED");
                }
                yield googleAdMobAdProviderService;
            }

            case FACEBOOK_AUDIENCE_NETWORK -> {
                if (!facebookAudienceNetworkAdProviderService.isAdProviderEnabled()) {
                    log.warn("⚠️  Facebook Audience Network provider is not enabled");
                    throw new AdProviderException(
                            "Facebook Audience Network provider is not enabled",
                            adProviderType,
                            "PROVIDER_NOT_ENABLED");
                }
                yield facebookAudienceNetworkAdProviderService;
            }

            case CRITEO -> {
                log.warn("⚠️  Criteo provider is not yet implemented");
                throw new AdProviderException(
                        "Criteo provider is not yet implemented",
                        adProviderType,
                        "PROVIDER_NOT_IMPLEMENTED");
            }

            case PUBMATIC -> {
                log.warn("⚠️  PubMatic provider is not yet implemented");
                throw new AdProviderException(
                        "PubMatic provider is not yet implemented",
                        adProviderType,
                        "PROVIDER_NOT_IMPLEMENTED");
            }
        };
    }

    /**
     * Get provider service by provider code (string identifier)
     * 
     * @param adProviderCode Provider code (e.g., "google-adsense", "google-admob")
     * @return Provider service implementation
     * @throws AdProviderException if code is invalid
     */
    public AdProviderService getAdProviderServiceByCode(String adProviderCode) {
        try {
            ProviderType adProviderType = ProviderType.fromCode(adProviderCode);
            return getAdProviderService(adProviderType);
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid provider code: {}", adProviderCode);
            throw new AdProviderException(
                    "Invalid provider code: " + adProviderCode,
                    null,
                    "INVALID_PROVIDER_CODE");
        }
    }

    /**
     * Get all enabled provider services
     * 
     * Useful for:
     * - Syncing all providers
     * - Aggregating metrics across providers
     * - Health check of all providers
     * 
     * @return Map of provider types to services (only enabled providers)
     */
    public Map<ProviderType, AdProviderService> getAllEnabledProviders() {
        Map<ProviderType, AdProviderService> enabledProviders = new EnumMap<>(ProviderType.class);

        if (googleAdsenseAdProviderService.isAdProviderEnabled()) {
            enabledProviders.put(ProviderType.GOOGLE_ADSENSE, googleAdsenseAdProviderService);
            log.debug("✅ Google AdSense provider is enabled");
        }

        if (googleAdMobAdProviderService.isAdProviderEnabled()) {
            enabledProviders.put(ProviderType.GOOGLE_ADMOB, googleAdMobAdProviderService);
            log.debug("✅ Google AdMob provider is enabled");
        }

        if (facebookAudienceNetworkAdProviderService.isAdProviderEnabled()) {
            enabledProviders.put(ProviderType.FACEBOOK_AUDIENCE_NETWORK, facebookAudienceNetworkAdProviderService);
            log.debug("✅ Facebook Audience Network provider is enabled");
        }

        log.info("📊 Total enabled providers: {}", enabledProviders.size());

        return enabledProviders;
    }

    /**
     * Check if provider is supported and enabled
     * 
     * @param adProviderType Provider type to check
     * @return true if provider is supported and enabled
     */
    public boolean isAdProviderSupportedAndEnabled(ProviderType adProviderType) {
        if (adProviderType == null) {
            return false;
        }

        try {
            AdProviderService service = getAdProviderService(adProviderType);
            return service.isAdProviderEnabled();
        } catch (AdProviderException e) {
            return false;
        }
    }
}
