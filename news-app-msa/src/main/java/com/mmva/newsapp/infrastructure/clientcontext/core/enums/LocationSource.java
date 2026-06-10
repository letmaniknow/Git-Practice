package com.mmva.newsapp.infrastructure.clientcontext.core.enums;

/**
 * Source of location data for GeoIP lookups.
 * 
 * <p>
 * Identifies where the geolocation information was obtained from:
 * </p>
 * <ul>
 * <li>CLIENT - Client-provided via GPS or Browser Geolocation API</li>
 * <li>IP_API - ip-api.com free service</li>
 * <li>MAXMIND - MaxMind GeoLite2 database</li>
 * <li>CLOUDFLARE - CloudFlare geo headers</li>
 * <li>MANUAL - User manually set location</li>
 * <li>UNKNOWN - Unknown or fallback source</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum LocationSource {
    /** Client-provided via GPS or Browser Geolocation */
    CLIENT,

    /** ip-api.com free service */
    IP_API,

    /** MaxMind GeoLite2 database */
    MAXMIND,

    /** CloudFlare headers */
    CLOUDFLARE,

    /** User manually set */
    MANUAL,

    /** Unknown or fallback */
    UNKNOWN
}
