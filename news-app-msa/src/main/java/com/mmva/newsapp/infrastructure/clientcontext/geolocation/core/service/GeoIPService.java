package com.mmva.newsapp.infrastructure.clientcontext.geolocation.core.service;

import com.mmva.newsapp.infrastructure.clientcontext.geolocation.core.dto.GeoLocationDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * Service interface for GeoIP location lookup.
 * 
 * <p>
 * Provides geolocation from multiple sources with fallback chain:
 * </p>
 * <ol>
 * <li><b>Client Headers</b> - GPS/Browser location from Flutter/Web (most
 * accurate)</li>
 * <li><b>Cloudflare Headers</b> - If behind Cloudflare (fast, accurate)</li>
 * <li><b>IP API Lookup</b> - ip-api.com free service (city-level)</li>
 * </ol>
 * 
 * <h3>Client Headers (from Flutter/Web apps):</h3>
 * <ul>
 * <li>X-Client-Latitude</li>
 * <li>X-Client-Longitude</li>
 * <li>X-Client-Country</li>
 * <li>X-Client-City</li>
 * <li>X-Client-Timezone</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * 
 * <pre>{@code
 * @Autowired
 * private GeoIPService geoIPService;
 * 
 * // In controller (with request)
 * GeoLocation location = geoIPService.lookup(request);
 * 
 * // In service layer (without request)
 * Optional<GeoLocation> location = geoIPService.lookupCurrentRequest();
 * 
 * // Direct IP lookup
 * GeoLocation location = geoIPService.lookupByIp("8.8.8.8");
 * }</pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface GeoIPService {

    /**
     * Primary lookup method - tries all sources with fallback.
     * 
     * <p>
     * Lookup order:
     * </p>
     * <ol>
     * <li>Client-provided headers (if enabled and present)</li>
     * <li>Cloudflare headers (if present)</li>
     * <li>IP API lookup (if enabled)</li>
     * </ol>
     * 
     * @param request the HTTP servlet request
     * @return GeoLocationDto with best available data
     */
    GeoLocationDto lookup(HttpServletRequest request);

    /**
     * Lookup using current request from RequestContextHolder.
     * Safe to call from any service layer.
     * 
     * @return Optional GeoLocationDto, empty if not in request scope
     */
    Optional<GeoLocationDto> lookupCurrentRequest();

    /**
     * Lookup by IP address only (uses ip-api.com).
     * Results are cached.
     * 
     * @param ipAddress the IP address to lookup
     * @return GeoLocationDto for the IP
     */
    GeoLocationDto lookupByIp(String ipAddress);

    /**
     * Extract client-provided location from request headers.
     * 
     * @param request the HTTP servlet request
     * @return Optional GeoLocationDto from client headers
     */
    Optional<GeoLocationDto> extractClientLocation(HttpServletRequest request);

    /**
     * Extract Cloudflare geo headers.
     * 
     * @param request the HTTP servlet request
     * @return Optional GeoLocationDto from Cloudflare headers
     */
    Optional<GeoLocationDto> extractCloudflareLocation(HttpServletRequest request);

    /**
     * Check if IP is private/localhost (skip lookup).
     * 
     * @param ipAddress the IP address to check
     * @return true if private/localhost
     */
    boolean isPrivateIp(String ipAddress);

    /**
     * Get location for private/localhost IPs.
     * 
     * @return default location for local development
     */
    GeoLocationDto getLocalLocation();

    /**
     * Validate client location against IP location.
     * Useful for fraud detection.
     * 
     * @param clientLocation client-provided location
     * @param ipLocation     IP-based location
     * @return true if locations are within acceptable distance
     */
    boolean validateClientLocation(GeoLocationDto clientLocation, GeoLocationDto ipLocation);

    /**
     * Calculate distance between two coordinates.
     * Uses Haversine formula.
     * 
     * @param lat1 latitude of point 1
     * @param lon1 longitude of point 1
     * @param lat2 latitude of point 2
     * @param lon2 longitude of point 2
     * @return distance in kilometers
     */
    double calculateDistance(double lat1, double lon1, double lat2, double lon2);

    /**
     * Clear the GeoIP cache.
     */
    void clearCache();

    /**
     * Get cache statistics.
     * 
     * @return cache hit/miss stats as string
     */
    String getCacheStats();
}
