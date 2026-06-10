package com.mmva.newsapp.infrastructure.clientcontext.geolocation.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for GeoLocation service.
 * 
 * <p>
 * Configures:
 * </p>
 * <ul>
 * <li>Client location headers (from Flutter/Web apps)</li>
 * <li>IP lookup API settings (ip-api.com)</li>
 * <li>Caching behavior</li>
 * <li>Fallback and validation settings</li>
 * </ul>
 * 
 * <h3>Configuration Example:</h3>
 * 
 * <pre>
 * geoip:
 *   enabled: true
 *   client-headers:
 *     enabled: true
 *     trust-client: true
 *   ip-api:
 *     enabled: true
 *     base-url: http://ip-api.com/json
 *     timeout-seconds: 5
 *   cache:
 *     enabled: true
 *     max-size: 10000
 *     ttl-hours: 24
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "geoip")
public class GeoLocationProperties {

    /**
     * Enable/disable GeoIP service entirely
     */
    private boolean enabled = true;

    /**
     * Client-provided location settings (from mobile apps/web)
     */
    private ClientHeaders clientHeaders = new ClientHeaders();

    /**
     * IP-API.com configuration (free service)
     */
    private IpApiConfig ipApi = new IpApiConfig();

    /**
     * Cache configuration
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * Cloudflare headers configuration
     */
    private CloudflareConfig cloudflare = new CloudflareConfig();

    /**
     * Privacy and compliance settings
     */
    private PrivacyConfig privacy = new PrivacyConfig();

    // ========================================
    // Nested Configuration Classes
    // ========================================

    /**
     * Client-provided location header settings
     */
    @Data
    public static class ClientHeaders {
        /**
         * Enable parsing of client location headers
         */
        private boolean enabled = true;

        /**
         * Trust client-provided location (use for registered users)
         * Set to false to always validate against IP lookup
         */
        private boolean trustClient = true;

        /**
         * Validate client location against IP lookup (cross-check)
         * Useful for fraud detection
         */
        private boolean validateWithIp = false;

        /**
         * Maximum allowed distance (km) between client and IP location
         * Used when validateWithIp is true
         */
        private int maxDistanceKm = 500;

        /**
         * Header names for client location
         */
        private HeaderNames headers = new HeaderNames();
    }

    /**
     * Header names for client-provided location
     */
    @Data
    public static class HeaderNames {
        private String latitude = "X-Client-Latitude";
        private String longitude = "X-Client-Longitude";
        private String countryCode = "X-Client-Country";
        private String city = "X-Client-City";
        private String timezone = "X-Client-Timezone";
        private String regionCode = "X-Client-Region";
        private String accuracy = "X-Client-Location-Accuracy";
    }

    /**
     * IP-API.com configuration (free tier: 45 requests/minute)
     */
    @Data
    public static class IpApiConfig {
        /**
         * Enable IP-API.com lookups
         */
        private boolean enabled = true;

        /**
         * Base URL for ip-api.com (use http for free tier)
         */
        private String baseUrl = "http://ip-api.com/json";

        /**
         * Connection timeout
         */
        private Duration timeout = Duration.ofSeconds(5);

        /**
         * Read timeout
         */
        private Duration readTimeout = Duration.ofSeconds(5);

        /**
         * Fields to request from API
         * Available: status,message,country,countryCode,region,regionName,city,
         * zip,lat,lon,timezone,isp,org,as,asname,mobile,proxy,hosting,query
         */
        private String fields = "status,message,country,countryCode,region,regionName," +
                "city,zip,lat,lon,timezone,isp,org,as,asname,mobile,proxy,hosting";

        /**
         * Language for country/region names
         */
        private String lang = "en";

        /**
         * Rate limit: requests per minute (free tier is 45)
         */
        private int rateLimitPerMinute = 45;

        /**
         * IPs to skip lookup (private, localhost)
         */
        private List<String> skipPatterns = List.of(
                "127.0.0.1",
                "0:0:0:0:0:0:0:1",
                "::1",
                "localhost",
                "10.*",
                "172.16.*",
                "172.17.*",
                "172.18.*",
                "172.19.*",
                "172.20.*",
                "172.21.*",
                "172.22.*",
                "172.23.*",
                "172.24.*",
                "172.25.*",
                "172.26.*",
                "172.27.*",
                "172.28.*",
                "172.29.*",
                "172.30.*",
                "172.31.*",
                "192.168.*");
    }

    /**
     * Cache configuration using Caffeine
     */
    @Data
    public static class CacheConfig {
        /**
         * Enable caching of IP lookups
         */
        private boolean enabled = true;

        /**
         * Maximum cache entries
         */
        private int maxSize = 10000;

        /**
         * Time to live for cache entries
         */
        private Duration ttl = Duration.ofHours(24);

        /**
         * Cache name for Caffeine
         */
        private String name = "geoip-cache";
    }

    /**
     * Cloudflare geo headers configuration
     */
    @Data
    public static class CloudflareConfig {
        /**
         * Enable parsing Cloudflare geo headers
         */
        private boolean enabled = true;

        /**
         * Trust Cloudflare headers (they're usually accurate)
         */
        private boolean trustCloudflare = true;

        /**
         * Header names
         */
        private String countryHeader = "CF-IPCountry";
        private String cityHeader = "CF-IPCity";
        private String continentHeader = "CF-IPContinent";
        private String latitudeHeader = "CF-IPLatitude";
        private String longitudeHeader = "CF-IPLongitude";
        private String regionHeader = "CF-Region";
        private String regionCodeHeader = "CF-Region-Code";
        private String timezoneHeader = "CF-Timezone";
    }

    /**
     * Privacy and compliance settings
     */
    @Data
    public static class PrivacyConfig {
        /**
         * Hash IP addresses before storing
         */
        private boolean hashIpAddresses = false;

        /**
         * Log IP addresses in debug logs
         */
        private boolean logIpAddresses = false;

        /**
         * Store precise coordinates (lat/long)
         */
        private boolean storeCoordinates = true;

        /**
         * Store city-level data (for GDPR compliance, may want to disable)
         */
        private boolean storeCityLevel = true;

        /**
         * Store ISP/Organization data
         */
        private boolean storeIspData = true;
    }
}
