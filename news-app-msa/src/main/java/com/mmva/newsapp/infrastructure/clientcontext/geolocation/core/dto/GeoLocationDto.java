package com.mmva.newsapp.infrastructure.clientcontext.geolocation.core.dto;

import com.mmva.newsapp.infrastructure.clientcontext.core.enums.AccuracyLevel;
import com.mmva.newsapp.infrastructure.clientcontext.core.enums.LocationSource;

import java.time.Instant;

/**
 * GeoLocation DTO representing geographic location data.
 * 
 * <p>
 * This record holds location information from multiple sources:
 * </p>
 * <ul>
 * <li>Client-provided (GPS/Browser Geolocation API)</li>
 * <li>IP-based lookup (ip-api.com)</li>
 * <li>Manual user settings</li>
 * </ul>
 * 
 * <h3>Data Accuracy:</h3>
 * <ul>
 * <li>GPS: ~10 meters</li>
 * <li>IP-based: ~10-50 km (city level)</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public record GeoLocationDto(
        // ========================================
        // Country Information
        // ========================================

        /** ISO 3166-1 alpha-2 country code (e.g., "US", "IN", "GB") */
        String countryCode,

        /** Full country name */
        String countryName,

        // ========================================
        // Region/State Information
        // ========================================

        /** Region/State code (e.g., "CA" for California) */
        String regionCode,

        /** Region/State name */
        String regionName,

        // ========================================
        // City Information
        // ========================================

        /** City name */
        String city,

        /** Postal/ZIP code */
        String postalCode,

        // ========================================
        // Coordinates (from GPS or IP lookup)
        // ========================================

        /** Latitude */
        Double latitude,

        /** Longitude */
        Double longitude,

        // ========================================
        // Timezone Information
        // ========================================

        /** IANA timezone (e.g., "America/New_York", "Asia/Kolkata") */
        String timezone,

        /** UTC offset in hours (e.g., -5.0 for EST) */
        Double utcOffset,

        // ========================================
        // ISP/Network Information (from IP lookup)
        // ========================================

        /** Internet Service Provider name */
        String isp,

        /** Organization name (company/institution) */
        String organization,

        /** AS Number (e.g., "AS15169") */
        String asNumber,

        /** AS Name (e.g., "Google LLC") */
        String asName,

        // ========================================
        // Mobile/Carrier Information
        // ========================================

        /** Is mobile network */
        Boolean isMobile,

        /** Is proxy/VPN */
        Boolean isProxy,

        /** Is hosting/datacenter */
        Boolean isHosting,

        // ========================================
        // Source Metadata
        // ========================================

        /** Source of location data: CLIENT, IP_API, MAXMIND, MANUAL */
        LocationSource source,

        /** Accuracy level: GPS, CITY, COUNTRY */
        AccuracyLevel accuracy,

        /** Whether location is from cache */
        Boolean cached,

        /** When this lookup was performed */
        Instant lookupTime) {

    // ========================================
    // Factory Methods
    // ========================================

    /**
     * Creates an empty GeoLocationDto (lookup failed or not available)
     */
    public static GeoLocationDto empty() {
        return new GeoLocationDto(
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null,
                LocationSource.UNKNOWN, AccuracyLevel.UNKNOWN, false, Instant.now());
    }

    /**
     * Creates a GeoLocationDto from client-provided coordinates
     */
    public static GeoLocationDto fromClient(
            Double latitude,
            Double longitude,
            String countryCode,
            String city,
            String timezone) {
        return new GeoLocationDto(
                countryCode, null, null, null, city, null,
                latitude, longitude, timezone, null,
                null, null, null, null,
                null, null, null,
                LocationSource.CLIENT, AccuracyLevel.GPS, false, Instant.now());
    }

    /**
     * Creates a GeoLocationDto from Cloudflare headers
     */
    public static GeoLocationDto fromCloudflare(String countryCode, String city) {
        return new GeoLocationDto(
                countryCode, null, null, null, city, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null,
                LocationSource.CLOUDFLARE, AccuracyLevel.CITY, false, Instant.now());
    }

    // ========================================
    // Utility Methods
    // ========================================

    /**
     * Checks if location data is available
     */
    public boolean hasLocation() {
        return countryCode != null && !countryCode.isEmpty();
    }

    /**
     * Checks if coordinates are available
     */
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    /**
     * Checks if this is a high-accuracy GPS location
     */
    public boolean isGpsAccuracy() {
        return accuracy == AccuracyLevel.GPS && hasCoordinates();
    }

    /**
     * Checks if request is from a suspicious source (proxy/hosting)
     */
    public boolean isSuspicious() {
        return Boolean.TRUE.equals(isProxy) || Boolean.TRUE.equals(isHosting);
    }

    /**
     * Gets display string for location
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        if (city != null) {
            sb.append(city);
        }
        if (regionName != null) {
            if (!sb.isEmpty())
                sb.append(", ");
            sb.append(regionName);
        }
        if (countryName != null) {
            if (!sb.isEmpty())
                sb.append(", ");
            sb.append(countryName);
        } else if (countryCode != null) {
            if (!sb.isEmpty())
                sb.append(", ");
            sb.append(countryCode);
        }
        return sb.isEmpty() ? "Unknown" : sb.toString();
    }
}
