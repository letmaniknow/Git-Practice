package com.mmva.newsapp.infrastructure.clientcontext.geolocation.core.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mmva.newsapp.infrastructure.clientcontext.geolocation.core.config.GeoLocationProperties;
import com.mmva.newsapp.infrastructure.clientcontext.geolocation.core.dto.GeoLocationDto;
import com.mmva.newsapp.infrastructure.clientcontext.core.enums.AccuracyLevel;
import com.mmva.newsapp.infrastructure.clientcontext.core.enums.LocationSource;
import com.mmva.newsapp.infrastructure.requestanalytics.service.RequestInfoService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * GeoIP service implementation with multiple data sources.
 * 
 * <p>
 * Data source priority:
 * </p>
 * <ol>
 * <li><b>Client Headers</b> - GPS from Flutter/Web (X-Client-* headers)</li>
 * <li><b>Cloudflare</b> - CF-IPCountry, CF-IPCity headers</li>
 * <li><b>ip-api.com</b> - Free IP geolocation API (45 req/min)</li>
 * </ol>
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Caffeine caching (10K entries, 24h TTL)</li>
 * <li>Private IP detection (skip lookups for localhost)</li>
 * <li>Rate limiting aware</li>
 * <li>Graceful fallback on errors</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class GeoIPServiceImpl implements GeoIPService {

    private final GeoLocationProperties properties;
    private final ObjectMapper objectMapper;
    private final RequestInfoService requestInfoService;

    private Cache<String, GeoLocationDto> cache;
    private HttpClient httpClient;

    /**
     * Constructor with lazy injection to avoid circular dependency.
     */
    public GeoIPServiceImpl(GeoLocationProperties properties, ObjectMapper objectMapper,
            @Lazy RequestInfoService requestInfoService) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.requestInfoService = requestInfoService;
    }

    // ========================================
    // IP Patterns
    // ========================================
    private static final Pattern IPV4_PRIVATE_PATTERN = Pattern.compile(
            "^(127\\.)|(10\\.)|(172\\.(1[6-9]|2[0-9]|3[0-1])\\.)|(192\\.168\\.)|" +
                    "(::1)|(0:0:0:0:0:0:0:1)|(localhost)$",
            Pattern.CASE_INSENSITIVE);

    // ========================================
    // Initialization
    // ========================================

    @PostConstruct
    public void init() {
        // Initialize cache
        if (properties.getCache().isEnabled()) {
            cache = Caffeine.newBuilder()
                    .maximumSize(properties.getCache().getMaxSize())
                    .expireAfterWrite(properties.getCache().getTtl().toHours(), TimeUnit.HOURS)
                    .recordStats()
                    .build();
            log.info("GeoIPService: Cache initialized - maxSize={}, ttl={}h",
                    properties.getCache().getMaxSize(),
                    properties.getCache().getTtl().toHours());
        }

        // Initialize HTTP client
        httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getIpApi().getTimeout())
                .build();

        log.info("GeoIPService: Initialized - enabled={}, clientHeaders={}, ipApi={}, cloudflare={}",
                properties.isEnabled(),
                properties.getClientHeaders().isEnabled(),
                properties.getIpApi().isEnabled(),
                properties.getCloudflare().isEnabled());
    }

    // ========================================
    // GeoIPService Implementation
    // ========================================

    @Override
    public GeoLocationDto lookup(HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return GeoLocationDto.empty();
        }

        // 1. Try client-provided location first (most accurate)
        if (properties.getClientHeaders().isEnabled()) {
            Optional<GeoLocationDto> clientLocation = extractClientLocation(request);
            if (clientLocation.isPresent()) {
                log.debug("GeoIPService: Using client-provided location");

                // Optionally validate against IP lookup
                if (properties.getClientHeaders().isValidateWithIp()) {
                    GeoLocationDto ipLocation = lookupByIpFromRequest(request);
                    if (!validateClientLocation(clientLocation.get(), ipLocation)) {
                        log.warn("GeoIPService: Client location validation failed, using IP location");
                        return ipLocation;
                    }
                }
                return clientLocation.get();
            }
        }

        // 2. Try Cloudflare headers
        if (properties.getCloudflare().isEnabled()) {
            Optional<GeoLocationDto> cfLocation = extractCloudflareLocation(request);
            if (cfLocation.isPresent()) {
                log.debug("GeoIPService: Using Cloudflare location");
                return cfLocation.get();
            }
        }

        // 3. Fall back to IP API lookup
        return lookupByIpFromRequest(request);
    }

    @Override
    public Optional<GeoLocationDto> lookupCurrentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest() != null) {
                return Optional.of(lookup(attrs.getRequest()));
            }
        } catch (Exception e) {
            log.warn("GeoIPService: Failed to get current request: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public GeoLocationDto lookupByIp(String ipAddress) {
        if (!properties.isEnabled() || !properties.getIpApi().isEnabled()) {
            return GeoLocationDto.empty();
        }

        if (ipAddress == null || ipAddress.isEmpty()) {
            return GeoLocationDto.empty();
        }

        // Check for private IP
        if (isPrivateIp(ipAddress)) {
            log.debug("GeoIPService: Skipping lookup for private IP: {}", maskIp(ipAddress));
            return getLocalLocation();
        }

        // Check cache first
        if (cache != null) {
            GeoLocationDto cached = cache.getIfPresent(ipAddress);
            if (cached != null) {
                log.debug("GeoIPService: Cache hit for IP: {}", maskIp(ipAddress));
                return withCacheFlag(cached, true);
            }
        }

        // Perform API lookup
        GeoLocationDto location = performIpApiLookup(ipAddress);

        // Cache the result
        if (cache != null && location.hasLocation()) {
            cache.put(ipAddress, location);
        }

        return location;
    }

    @Override
    public Optional<GeoLocationDto> extractClientLocation(HttpServletRequest request) {
        var headerNames = properties.getClientHeaders().getHeaders();

        String latitude = request.getHeader(headerNames.getLatitude());
        String longitude = request.getHeader(headerNames.getLongitude());
        String countryCode = request.getHeader(headerNames.getCountryCode());
        String city = request.getHeader(headerNames.getCity());
        String timezone = request.getHeader(headerNames.getTimezone());
        String regionCode = request.getHeader(headerNames.getRegionCode());

        // Need at least country code or coordinates
        boolean hasCoordinates = isValidCoordinate(latitude) && isValidCoordinate(longitude);
        boolean hasCountry = countryCode != null && !countryCode.isEmpty();

        if (!hasCoordinates && !hasCountry) {
            return Optional.empty();
        }

        Double lat = hasCoordinates ? parseDouble(latitude) : null;
        Double lon = hasCoordinates ? parseDouble(longitude) : null;

        GeoLocationDto location = new GeoLocationDto(
                sanitize(countryCode),
                null, // countryName - not provided by client
                sanitize(regionCode),
                null, // regionName
                sanitize(city),
                null, // postalCode
                lat,
                lon,
                sanitize(timezone),
                null, // utcOffset
                null, // isp
                null, // organization
                null, // asNumber
                null, // asName
                null, // isMobile
                null, // isProxy
                null, // isHosting
                LocationSource.CLIENT,
                hasCoordinates ? AccuracyLevel.GPS : AccuracyLevel.CITY,
                false,
                Instant.now());

        return Optional.of(location);
    }

    @Override
    public Optional<GeoLocationDto> extractCloudflareLocation(HttpServletRequest request) {
        var cf = properties.getCloudflare();

        String countryCode = request.getHeader(cf.getCountryHeader());

        // Need at least country code
        if (countryCode == null || countryCode.isEmpty() || "XX".equals(countryCode)) {
            return Optional.empty();
        }

        String city = request.getHeader(cf.getCityHeader());
        String latitude = request.getHeader(cf.getLatitudeHeader());
        String longitude = request.getHeader(cf.getLongitudeHeader());
        String timezone = request.getHeader(cf.getTimezoneHeader());
        String regionCode = request.getHeader(cf.getRegionCodeHeader());

        Double lat = parseDouble(latitude);
        Double lon = parseDouble(longitude);

        GeoLocationDto location = new GeoLocationDto(
                sanitize(countryCode),
                null, // countryName
                sanitize(regionCode),
                request.getHeader(cf.getRegionHeader()), // regionName
                sanitize(city),
                null, // postalCode
                lat,
                lon,
                sanitize(timezone),
                null, // utcOffset
                null, // isp
                null, // organization
                null, // asNumber
                null, // asName
                null, // isMobile
                null, // isProxy
                null, // isHosting
                LocationSource.CLOUDFLARE,
                lat != null ? AccuracyLevel.CITY : AccuracyLevel.COUNTRY,
                false,
                Instant.now());

        return Optional.of(location);
    }

    @Override
    public boolean isPrivateIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return true;
        }
        return IPV4_PRIVATE_PATTERN.matcher(ipAddress).find();
    }

    @Override
    public GeoLocationDto getLocalLocation() {
        return new GeoLocationDto(
                "XX", // Unknown
                "Local/Development",
                null, null, "localhost", null,
                null, null,
                "UTC", 0.0,
                "Local", "Development", null, null,
                false, false, false,
                LocationSource.UNKNOWN,
                AccuracyLevel.UNKNOWN,
                false,
                Instant.now());
    }

    @Override
    public boolean validateClientLocation(GeoLocationDto clientLocation, GeoLocationDto ipLocation) {
        // If either doesn't have coordinates, just compare country
        if (!clientLocation.hasCoordinates() || !ipLocation.hasCoordinates()) {
            if (clientLocation.countryCode() != null && ipLocation.countryCode() != null) {
                return clientLocation.countryCode().equalsIgnoreCase(ipLocation.countryCode());
            }
            return true; // Can't validate, assume ok
        }

        double distance = calculateDistance(
                clientLocation.latitude(), clientLocation.longitude(),
                ipLocation.latitude(), ipLocation.longitude());

        int maxDistance = properties.getClientHeaders().getMaxDistanceKm();
        boolean valid = distance <= maxDistance;

        if (!valid) {
            log.warn("GeoIPService: Location mismatch - client: {},{} vs IP: {},{} - distance: {}km (max: {}km)",
                    clientLocation.latitude(), clientLocation.longitude(),
                    ipLocation.latitude(), ipLocation.longitude(),
                    String.format("%.1f", distance), maxDistance);
        }

        return valid;
    }

    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula
        final double R = 6371; // Earth radius in km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    @Override
    public void clearCache() {
        if (cache != null) {
            cache.invalidateAll();
            log.info("GeoIPService: Cache cleared");
        }
    }

    @Override
    public String getCacheStats() {
        if (cache != null) {
            var stats = cache.stats();
            return String.format(
                    "hits=%d, misses=%d, hitRate=%.2f%%, evictions=%d, size=%d",
                    stats.hitCount(),
                    stats.missCount(),
                    stats.hitRate() * 100,
                    stats.evictionCount(),
                    cache.estimatedSize());
        }
        return "Cache disabled";
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    private GeoLocationDto lookupByIpFromRequest(HttpServletRequest request) {
        String ipAddress = requestInfoService.getClientIpAddress(request);
        return lookupByIp(ipAddress);
    }

    private GeoLocationDto performIpApiLookup(String ipAddress) {
        try {
            var config = properties.getIpApi();
            String url = String.format("%s/%s?fields=%s&lang=%s",
                    config.getBaseUrl(),
                    ipAddress,
                    config.getFields(),
                    config.getLang());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(config.getReadTimeout())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseIpApiResponse(response.body());
            } else if (response.statusCode() == 429) {
                log.warn("GeoIPService: Rate limit exceeded for ip-api.com");
                return GeoLocationDto.empty();
            } else {
                log.warn("GeoIPService: API returned status {}", response.statusCode());
                return GeoLocationDto.empty();
            }

        } catch (Exception e) {
            log.error("GeoIPService: Failed to lookup IP {}: {}", maskIp(ipAddress), e.getMessage());
            return GeoLocationDto.empty();
        }
    }

    private GeoLocationDto parseIpApiResponse(String jsonResponse) {
        try {
            IpApiResponse response = objectMapper.readValue(jsonResponse, IpApiResponse.class);

            if (!"success".equals(response.status)) {
                log.warn("GeoIPService: API error - {}", response.message);
                return GeoLocationDto.empty();
            }

            return new GeoLocationDto(
                    response.countryCode,
                    response.country,
                    response.region,
                    response.regionName,
                    response.city,
                    response.zip,
                    response.lat,
                    response.lon,
                    response.timezone,
                    null, // utcOffset - not provided
                    response.isp,
                    response.org,
                    response.as,
                    response.asname,
                    response.mobile,
                    response.proxy,
                    response.hosting,
                    LocationSource.IP_API,
                    response.city != null ? AccuracyLevel.CITY : AccuracyLevel.COUNTRY,
                    false,
                    Instant.now());

        } catch (Exception e) {
            log.error("GeoIPService: Failed to parse response: {}", e.getMessage());
            return GeoLocationDto.empty();
        }
    }

    private GeoLocationDto withCacheFlag(GeoLocationDto location, boolean cached) {
        return new GeoLocationDto(
                location.countryCode(),
                location.countryName(),
                location.regionCode(),
                location.regionName(),
                location.city(),
                location.postalCode(),
                location.latitude(),
                location.longitude(),
                location.timezone(),
                location.utcOffset(),
                location.isp(),
                location.organization(),
                location.asNumber(),
                location.asName(),
                location.isMobile(),
                location.isProxy(),
                location.isHosting(),
                location.source(),
                location.accuracy(),
                cached,
                location.lookupTime());
    }

    private boolean isValidCoordinate(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            double d = Double.parseDouble(value);
            return !Double.isNaN(d) && !Double.isInfinite(d);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        // Remove any potentially dangerous characters
        return value.replaceAll("[<>\"'&;]", "").trim();
    }

    private String maskIp(String ip) {
        if (ip == null)
            return "null";
        if (!properties.getPrivacy().isLogIpAddresses()) {
            // Mask last octet for privacy
            int lastDot = ip.lastIndexOf('.');
            if (lastDot > 0) {
                return ip.substring(0, lastDot) + ".xxx";
            }
            return "xxx";
        }
        return ip;
    }

    // ========================================
    // IP-API Response DTO
    // ========================================

    /**
     * Response object from ip-api.com
     */
    private static class IpApiResponse {
        @JsonProperty("status")
        public String status;

        @JsonProperty("message")
        public String message;

        @JsonProperty("country")
        public String country;

        @JsonProperty("countryCode")
        public String countryCode;

        @JsonProperty("region")
        public String region;

        @JsonProperty("regionName")
        public String regionName;

        @JsonProperty("city")
        public String city;

        @JsonProperty("zip")
        public String zip;

        @JsonProperty("lat")
        public Double lat;

        @JsonProperty("lon")
        public Double lon;

        @JsonProperty("timezone")
        public String timezone;

        @JsonProperty("isp")
        public String isp;

        @JsonProperty("org")
        public String org;

        @JsonProperty("as")
        public String as;

        @JsonProperty("asname")
        public String asname;

        @JsonProperty("mobile")
        public Boolean mobile;

        @JsonProperty("proxy")
        public Boolean proxy;

        @JsonProperty("hosting")
        public Boolean hosting;

        @JsonProperty("query")
        public String query;
    }
}
