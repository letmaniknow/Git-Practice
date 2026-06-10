package com.mmva.newsapp.infrastructure.clientcontext.core.service;

import com.mmva.newsapp.infrastructure.clientcontext.core.dto.ClientContextDto;
import com.mmva.newsapp.infrastructure.clientcontext.geolocation.core.dto.GeoLocationDto;
import com.mmva.newsapp.infrastructure.clientcontext.core.enums.Channel;
import com.mmva.newsapp.infrastructure.clientcontext.core.enums.DeviceType;
import com.mmva.newsapp.infrastructure.clientcontext.geolocation.core.service.GeoIPService;
import com.mmva.newsapp.infrastructure.requestanalytics.service.RequestInfoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of client context extraction and enrichment service.
 * 
 * <p>
 * Provides comprehensive parsing of HTTP request information including:
 * </p>
 * <ul>
 * <li>User-Agent parsing for device/browser/OS detection</li>
 * <li>Client Hints API support for modern browsers</li>
 * <li>GeoIP ready fields (integration point for MaxMind, etc.)</li>
 * <li>Bot detection for known crawlers</li>
 * <li>Risk scoring for fraud prevention</li>
 * <li>Device fingerprinting for security</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class ClientContextServiceImpl implements ClientContextService {

    private final GeoIPService geoIPService;
    private final RequestInfoService requestInfoService;

    /**
     * Constructor with lazy injection to avoid circular dependency.
     */
    public ClientContextServiceImpl(@Lazy GeoIPService geoIPService,
            @Lazy RequestInfoService requestInfoService) {
        this.geoIPService = geoIPService;
        this.requestInfoService = requestInfoService;
    }

    // ========================================
    // Bot Detection Patterns
    // ========================================
    private static final Pattern BOT_PATTERN = Pattern.compile(
            "(?i)(bot|crawler|spider|slurp|bingbot|googlebot|yandex|baidu|duckduck|" +
                    "facebookexternalhit|twitterbot|linkedinbot|whatsapp|telegram|slack|" +
                    "applebot|msnbot|teoma|ia_archiver|wget|curl|python-requests|" +
                    "java|httpclient|okhttp|go-http|postman|insomnia)");

    private static final List<String> KNOWN_BOTS = List.of(
            "Googlebot", "Bingbot", "Slurp", "DuckDuckBot", "Baiduspider",
            "YandexBot", "facebookexternalhit", "Twitterbot", "LinkedInBot",
            "WhatsApp", "TelegramBot", "Applebot", "PetalBot", "SemrushBot");

    // ========================================
    // OS Detection Patterns
    // ========================================
    private static final List<OsPattern> OS_PATTERNS = List.of(
            new OsPattern("Windows NT 10", "Windows", "10/11"),
            new OsPattern("Windows NT 6.3", "Windows", "8.1"),
            new OsPattern("Windows NT 6.2", "Windows", "8"),
            new OsPattern("Windows NT 6.1", "Windows", "7"),
            new OsPattern("Mac OS X 10", "macOS", null),
            new OsPattern("Mac OS X 11", "macOS", "Big Sur"),
            new OsPattern("Mac OS X 12", "macOS", "Monterey"),
            new OsPattern("Mac OS X 13", "macOS", "Ventura"),
            new OsPattern("Mac OS X 14", "macOS", "Sonoma"),
            new OsPattern("Mac OS X 15", "macOS", "Sequoia"),
            new OsPattern("iPhone OS", "iOS", null),
            new OsPattern("iPad", "iPadOS", null),
            new OsPattern("Android", "Android", null),
            new OsPattern("Linux", "Linux", null),
            new OsPattern("CrOS", "ChromeOS", null),
            new OsPattern("Ubuntu", "Ubuntu", null));

    // ========================================
    // Browser Detection Patterns
    // ========================================
    private static final List<BrowserPattern> BROWSER_PATTERNS = List.of(
            new BrowserPattern("Edg/", "Edge"),
            new BrowserPattern("OPR/", "Opera"),
            new BrowserPattern("Opera", "Opera"),
            new BrowserPattern("Chrome/", "Chrome"),
            new BrowserPattern("CriOS/", "Chrome"), // Chrome on iOS
            new BrowserPattern("Firefox/", "Firefox"),
            new BrowserPattern("FxiOS/", "Firefox"), // Firefox on iOS
            new BrowserPattern("Safari/", "Safari"),
            new BrowserPattern("MSIE", "IE"),
            new BrowserPattern("Trident/", "IE"),
            new BrowserPattern("SamsungBrowser", "Samsung Internet"),
            new BrowserPattern("UCBrowser", "UC Browser"),
            new BrowserPattern("Brave", "Brave"),
            new BrowserPattern("Vivaldi", "Vivaldi"));

    // ========================================
    // Mobile App Headers
    // ========================================
    private static final String HEADER_APP_VERSION = "X-App-Version";
    private static final String HEADER_APP_BUILD = "X-App-Build";
    private static final String HEADER_APP_PLATFORM = "X-App-Platform";
    @SuppressWarnings("unused") // Reserved for future native app integration
    private static final String HEADER_DEVICE_ID = "X-Device-ID";

    // ========================================
    // Client Hints Headers
    // ========================================
    private static final String SEC_CH_UA = "Sec-CH-UA";
    private static final String SEC_CH_UA_MOBILE = "Sec-CH-UA-Mobile";
    private static final String SEC_CH_UA_PLATFORM = "Sec-CH-UA-Platform";
    private static final String SEC_CH_UA_PLATFORM_VERSION = "Sec-CH-UA-Platform-Version";
    @SuppressWarnings("unused") // Reserved for architecture-based optimization
    private static final String SEC_CH_UA_ARCH = "Sec-CH-UA-Arch";
    private static final String SEC_CH_UA_MODEL = "Sec-CH-UA-Model";
    private static final String DEVICE_MEMORY = "Device-Memory";
    private static final String ECT = "ECT";
    private static final String SAVE_DATA = "Save-Data";

    // ========================================
    // ClientContextService Implementation
    // ========================================

    @Override
    public ClientContextDto extractContext(HttpServletRequest request) {
        if (request == null) {
            log.warn("ClientContextService: Request is null, returning empty context");
            return ClientContextDto.empty();
        }

        try {
            // Extract raw values
            String ipAddress = requestInfoService.getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            String acceptLanguage = request.getHeader("Accept-Language");
            String referer = request.getHeader("Referer");
            String origin = request.getHeader("Origin");
            String forwardedFor = request.getHeader("X-Forwarded-For");

            // Client Hints
            String secChUa = request.getHeader(SEC_CH_UA);
            String secChUaMobile = request.getHeader(SEC_CH_UA_MOBILE);
            String secChUaPlatform = cleanQuotes(request.getHeader(SEC_CH_UA_PLATFORM));
            String secChUaPlatformVersion = cleanQuotes(request.getHeader(SEC_CH_UA_PLATFORM_VERSION));
            String secChUaModel = cleanQuotes(request.getHeader(SEC_CH_UA_MODEL));
            String deviceMemoryStr = request.getHeader(DEVICE_MEMORY);
            String ect = request.getHeader(ECT);
            String saveDataStr = request.getHeader(SAVE_DATA);

            // Parse device info
            DeviceType deviceType = detectDeviceType(userAgent, secChUaMobile);
            String osName = secChUaPlatform != null ? secChUaPlatform : parseOsName(userAgent);
            String osVersion = secChUaPlatformVersion != null ? secChUaPlatformVersion
                    : parseOsVersion(userAgent, osName);
            String browserName = parseBrowserFromClientHints(secChUa);
            if (browserName == null) {
                browserName = parseBrowserName(userAgent);
            }
            String browserVersion = parseBrowserVersion(userAgent, browserName);
            String deviceBrand = parseDeviceBrand(userAgent, secChUaModel);
            String deviceModel = secChUaModel != null ? secChUaModel : parseDeviceModel(userAgent);
            Double deviceMemory = parseDouble(deviceMemoryStr);

            // Bot detection
            boolean isBot = isBot(userAgent);
            String botName = isBot ? getBotName(userAgent) : null;
            String botType = isBot ? classifyBotType(userAgent) : null;
            if (isBot) {
                deviceType = DeviceType.BOT;
            }

            // Channel detection
            Channel channel = detectChannel(request);
            String appVersion = request.getHeader(HEADER_APP_VERSION);
            String appBuild = request.getHeader(HEADER_APP_BUILD);

            // Language parsing
            String primaryLanguage = parsePrimaryLanguage(acceptLanguage);

            // Referer parsing
            String refererDomain = extractRefererDomain(referer);

            // Fingerprint
            String fingerprint = generateDeviceFingerprint(request);

            // Network info
            boolean isBehindProxy = forwardedFor != null && !forwardedFor.isEmpty();
            String proxyProvider = detectProxyProvider(request);
            String connectionType = detectConnectionType(request);

            // Security
            Boolean saveData = "on".equalsIgnoreCase(saveDataStr) || "1".equals(saveDataStr);
            Boolean isAnonymized = detectAnonymization(request);

            // UTM parameters
            String utmSource = request.getParameter("utm_source");
            String utmMedium = request.getParameter("utm_medium");
            String utmCampaign = request.getParameter("utm_campaign");

            // Build context
            ClientContextDto context = new ClientContextDto(
                    // Network (6)
                    ipAddress,
                    request.getRemoteAddr(),
                    forwardedFor,
                    isBehindProxy,
                    proxyProvider,
                    connectionType,
                    // Device (12)
                    userAgent,
                    deviceType,
                    osName,
                    osVersion,
                    browserName,
                    browserVersion,
                    deviceBrand,
                    deviceModel,
                    deviceMemory,
                    null, // screenResolution - needs JavaScript
                    null, // devicePixelRatio - needs JavaScript
                    fingerprint,
                    // Location (7) - GeoIP enrichment point
                    null, null, null, null, null, null, null,
                    // Request (8)
                    acceptLanguage,
                    primaryLanguage,
                    referer,
                    refererDomain,
                    origin,
                    Instant.now(),
                    ect,
                    saveData,
                    // Security (6)
                    isBot,
                    botType,
                    botName,
                    isAnonymized,
                    0, // Will be calculated
                    null,
                    // Channel (6)
                    channel,
                    appVersion,
                    appBuild,
                    utmSource,
                    utmMedium,
                    utmCampaign);

            // Calculate risk score
            int riskScore = calculateRiskScore(context);
            String riskFactors = calculateRiskFactors(context);

            // Return with risk assessment
            return new ClientContextDto(
                    context.ipAddress(),
                    context.originalIpAddress(),
                    context.forwardedForChain(),
                    context.isBehindProxy(),
                    context.proxyProvider(),
                    context.connectionType(),
                    context.userAgent(),
                    context.deviceType(),
                    context.osName(),
                    context.osVersion(),
                    context.browserName(),
                    context.browserVersion(),
                    context.deviceBrand(),
                    context.deviceModel(),
                    context.deviceMemory(),
                    context.screenResolution(),
                    context.devicePixelRatio(),
                    context.deviceFingerprint(),
                    context.countryCode(),
                    context.countryName(),
                    context.regionCode(),
                    context.regionName(),
                    context.city(),
                    context.postalCode(),
                    context.timezone(),
                    context.acceptLanguage(),
                    context.primaryLanguage(),
                    context.referer(),
                    context.refererDomain(),
                    context.origin(),
                    context.requestTime(),
                    context.effectiveConnectionType(),
                    context.saveDataPreference(),
                    context.isBot(),
                    context.botType(),
                    context.botName(),
                    context.isAnonymized(),
                    riskScore,
                    riskFactors,
                    context.channel(),
                    context.appVersion(),
                    context.appBuild(),
                    context.utmSource(),
                    context.utmMedium(),
                    context.utmCampaign());

        } catch (Exception e) {
            log.error("ClientContextService: Failed to extract context: {}", e.getMessage(), e);
            return ClientContextDto.empty();
        }
    }

    @Override
    public ClientContextDto getCurrentContext() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest() != null) {
                return extractContext(attrs.getRequest());
            }
        } catch (Exception e) {
            log.debug("ClientContextService: Not in request context: {}", e.getMessage());
        }
        return ClientContextDto.empty();
    }

    @Override
    public ClientContextDto extractMinimalContext(HttpServletRequest request) {
        if (request == null) {
            return ClientContextDto.empty();
        }
        return ClientContextDto.minimal(
                requestInfoService.getClientIpAddress(request),
                request.getHeader("User-Agent"));
    }

    @Override
    public String generateDeviceFingerprint(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // Combine multiple attributes for fingerprinting
        StringBuilder components = new StringBuilder();
        components.append(request.getHeader("User-Agent"));
        components.append("|");
        components.append(request.getHeader("Accept-Language"));
        components.append("|");
        components.append(request.getHeader("Accept-Encoding"));
        components.append("|");
        components.append(request.getHeader(SEC_CH_UA_PLATFORM));
        components.append("|");
        components.append(request.getHeader(SEC_CH_UA));
        components.append("|");
        components.append(request.getHeader(DEVICE_MEMORY));
        components.append("|");
        components.append(request.getHeader("Sec-CH-UA-Arch"));

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(components.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("ClientContextService: SHA-256 not available", e);
            return null;
        }
    }

    @Override
    public int calculateRiskScore(ClientContextDto context) {
        if (context == null) {
            return 0;
        }

        int score = 0;

        // Bot detected
        if (Boolean.TRUE.equals(context.isBot())) {
            score += 30;
        }

        // Anonymous/VPN
        if (Boolean.TRUE.equals(context.isAnonymized())) {
            score += 20;
        }

        // Missing User-Agent
        if (context.userAgent() == null || context.userAgent().isEmpty()) {
            score += 25;
        }

        // Unknown device type
        if (context.deviceType() == DeviceType.UNKNOWN) {
            score += 10;
        }

        // Missing Accept-Language (unusual for real browsers)
        if (context.acceptLanguage() == null || context.acceptLanguage().isEmpty()) {
            score += 10;
        }

        // Suspicious patterns in User-Agent
        if (context.userAgent() != null) {
            String ua = context.userAgent().toLowerCase();
            if (ua.contains("headless") || ua.contains("phantom") || ua.contains("selenium")) {
                score += 40;
            }
        }

        // API channel without proper authentication context
        if (context.channel() == Channel.API) {
            score += 5;
        }

        return Math.min(score, 100);
    }

    @Override
    public boolean isBot(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return false;
        }
        return BOT_PATTERN.matcher(userAgent).find();
    }

    @Override
    public String getBotName(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        for (String botName : KNOWN_BOTS) {
            if (userAgent.toLowerCase().contains(botName.toLowerCase())) {
                return botName;
            }
        }
        if (isBot(userAgent)) {
            return "Unknown Bot";
        }
        return null;
    }

    @Override
    public String parsePrimaryLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            return null;
        }
        // Accept-Language: en-US,en;q=0.9,es;q=0.8
        String primary = acceptLanguage.split(",")[0].trim();
        if (primary.contains("-")) {
            return primary.split("-")[0];
        }
        if (primary.contains(";")) {
            return primary.split(";")[0];
        }
        return primary;
    }

    @Override
    public String extractRefererDomain(String referer) {
        if (referer == null || referer.isEmpty()) {
            return null;
        }
        try {
            URI uri = new URI(referer);
            return uri.getHost();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Channel detectChannel(HttpServletRequest request) {
        // Check for mobile app headers
        String appPlatform = request.getHeader(HEADER_APP_PLATFORM);
        if (appPlatform != null) {
            if (appPlatform.toLowerCase().contains("ios")) {
                return Channel.IOS_APP;
            }
            if (appPlatform.toLowerCase().contains("android")) {
                return Channel.ANDROID_APP;
            }
        }

        // Check for app version header (indicates mobile app)
        if (request.getHeader(HEADER_APP_VERSION) != null) {
            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null) {
                if (userAgent.contains("iOS") || userAgent.contains("iPhone") || userAgent.contains("iPad")) {
                    return Channel.IOS_APP;
                }
                if (userAgent.contains("Android")) {
                    return Channel.ANDROID_APP;
                }
            }
            return Channel.ANDROID_APP; // Default to Android if unknown
        }

        // Check for API clients
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            if (ua.contains("postman") || ua.contains("insomnia") || ua.contains("curl") ||
                    ua.contains("httpie") || ua.contains("python") || ua.contains("java") ||
                    ua.contains("okhttp") || ua.contains("axios")) {
                return Channel.API;
            }
        }

        // Check for mobile web
        String secChUaMobile = request.getHeader(SEC_CH_UA_MOBILE);
        if ("?1".equals(secChUaMobile)) {
            return Channel.MOBILE_WEB;
        }

        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            if (ua.contains("mobile") || ua.contains("android") ||
                    ua.contains("iphone") || ua.contains("ipad")) {
                return Channel.MOBILE_WEB;
            }
        }

        // Default to web
        return Channel.WEB;
    }

    @Override
    public ClientContextDto enrichWithGeoIP(ClientContextDto context, String ipAddress) {
        if (context == null) {
            return ClientContextDto.empty();
        }

        try {
            GeoLocationDto geoLocation = geoIPService.lookupByIp(ipAddress);

            if (!geoLocation.hasLocation()) {
                log.debug("ClientContextService: No GeoIP data found for IP");
                return context;
            }

            log.debug("ClientContextService: Enriched with GeoIP - country={}, city={}",
                    geoLocation.countryCode(), geoLocation.city());

            // Return new context with geo data merged
            return new ClientContextDto(
                    // Network Information (unchanged)
                    context.ipAddress(),
                    context.originalIpAddress(),
                    context.forwardedForChain(),
                    context.isBehindProxy(),
                    context.proxyProvider(),
                    context.connectionType(),

                    // Device Information (unchanged)
                    context.userAgent(),
                    context.deviceType(),
                    context.osName(),
                    context.osVersion(),
                    context.browserName(),
                    context.browserVersion(),
                    context.deviceBrand(),
                    context.deviceModel(),
                    context.deviceMemory(),
                    context.screenResolution(),
                    context.devicePixelRatio(),
                    context.deviceFingerprint(),

                    // Location Information (from GeoIP)
                    geoLocation.countryCode() != null ? geoLocation.countryCode() : context.countryCode(),
                    geoLocation.countryName() != null ? geoLocation.countryName() : context.countryName(),
                    geoLocation.regionCode() != null ? geoLocation.regionCode() : context.regionCode(),
                    geoLocation.regionName() != null ? geoLocation.regionName() : context.regionName(),
                    geoLocation.city() != null ? geoLocation.city() : context.city(),
                    geoLocation.postalCode() != null ? geoLocation.postalCode() : context.postalCode(),
                    geoLocation.timezone() != null ? geoLocation.timezone() : context.timezone(),

                    // Request Context (unchanged)
                    context.acceptLanguage(),
                    context.primaryLanguage(),
                    context.referer(),
                    context.refererDomain(),
                    context.origin(),
                    context.requestTime(),
                    context.effectiveConnectionType(),
                    context.saveDataPreference(),

                    // Security Context (updated with proxy detection)
                    context.isBot(),
                    context.botType(),
                    context.botName(),
                    // Update isAnonymized if proxy detected
                    Boolean.TRUE.equals(geoLocation.isProxy()) || Boolean.TRUE.equals(geoLocation.isHosting())
                            ? true
                            : context.isAnonymized(),
                    context.riskScore(),
                    // Add hosting/proxy info to risk factors
                    enrichRiskFactors(context.riskFactors(), geoLocation),

                    // Channel & Attribution (unchanged)
                    context.channel(),
                    context.appVersion(),
                    context.appBuild(),
                    context.utmSource(),
                    context.utmMedium(),
                    context.utmCampaign());

        } catch (Exception e) {
            log.warn("ClientContextService: Failed to enrich with GeoIP: {}", e.getMessage());
            return context;
        }
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * Enriches risk factors with GeoIP proxy/hosting detection information.
     */
    private String enrichRiskFactors(String existingFactors, GeoLocationDto geoLocation) {
        if (geoLocation == null) {
            return existingFactors;
        }

        StringBuilder factors = new StringBuilder();
        if (existingFactors != null && !existingFactors.isBlank()) {
            factors.append(existingFactors);
        }

        if (Boolean.TRUE.equals(geoLocation.isProxy())) {
            if (!factors.isEmpty())
                factors.append(", ");
            factors.append("PROXY_DETECTED");
        }

        if (Boolean.TRUE.equals(geoLocation.isHosting())) {
            if (!factors.isEmpty())
                factors.append(", ");
            factors.append("HOSTING_PROVIDER");
        }

        return factors.isEmpty() ? null : factors.toString();
    }

    private DeviceType detectDeviceType(String userAgent, String secChUaMobile) {
        // Prefer Client Hints
        if ("?1".equals(secChUaMobile)) {
            return DeviceType.MOBILE;
        }
        if ("?0".equals(secChUaMobile)) {
            return DeviceType.DESKTOP;
        }

        if (userAgent == null) {
            return DeviceType.UNKNOWN;
        }

        String ua = userAgent.toLowerCase();

        // TV devices
        if (ua.contains("smart-tv") || ua.contains("smarttv") || ua.contains("googletv") ||
                ua.contains("appletv") || ua.contains("roku") || ua.contains("firetv")) {
            return DeviceType.TV;
        }

        // Tablets
        if (ua.contains("tablet") || ua.contains("ipad") ||
                (ua.contains("android") && !ua.contains("mobile"))) {
            return DeviceType.TABLET;
        }

        // Mobile
        if (ua.contains("mobile") || ua.contains("iphone") || ua.contains("ipod") ||
                (ua.contains("android") && ua.contains("mobile"))) {
            return DeviceType.MOBILE;
        }

        // Wearables
        if (ua.contains("watch")) {
            return DeviceType.WEARABLE;
        }

        // Gaming consoles
        if (ua.contains("playstation") || ua.contains("xbox") || ua.contains("nintendo")) {
            return DeviceType.CONSOLE;
        }

        // Desktop is the default for browsers
        if (ua.contains("windows") || ua.contains("macintosh") || ua.contains("linux")) {
            return DeviceType.DESKTOP;
        }

        return DeviceType.UNKNOWN;
    }

    private String parseOsName(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        for (OsPattern pattern : OS_PATTERNS) {
            if (userAgent.contains(pattern.marker)) {
                return pattern.osName;
            }
        }
        return null;
    }

    private String parseOsVersion(String userAgent, String osName) {
        if (userAgent == null || osName == null) {
            return null;
        }

        try {
            if ("Android".equals(osName)) {
                int idx = userAgent.indexOf("Android ");
                if (idx >= 0) {
                    String version = userAgent.substring(idx + 8);
                    int endIdx = version.indexOf(";");
                    if (endIdx < 0)
                        endIdx = version.indexOf(")");
                    if (endIdx > 0) {
                        return version.substring(0, endIdx).trim();
                    }
                }
            }
            if ("iOS".equals(osName) || "iPadOS".equals(osName)) {
                int idx = userAgent.indexOf("OS ");
                if (idx >= 0) {
                    String version = userAgent.substring(idx + 3);
                    int endIdx = version.indexOf(" ");
                    if (endIdx > 0) {
                        return version.substring(0, endIdx).replace("_", ".");
                    }
                }
            }
        } catch (Exception e) {
            log.trace("Failed to parse OS version from: {}", userAgent);
        }
        return null;
    }

    private String parseBrowserFromClientHints(String secChUa) {
        if (secChUa == null || secChUa.isEmpty()) {
            return null;
        }
        // Parse: "Google Chrome";v="120", "Chromium";v="120", "Not_A Brand";v="24"
        if (secChUa.contains("Chrome") && !secChUa.contains("Chromium")) {
            return "Chrome";
        }
        if (secChUa.contains("Firefox")) {
            return "Firefox";
        }
        if (secChUa.contains("Safari")) {
            return "Safari";
        }
        if (secChUa.contains("Edge")) {
            return "Edge";
        }
        if (secChUa.contains("Opera")) {
            return "Opera";
        }
        return null;
    }

    private String parseBrowserName(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        for (BrowserPattern pattern : BROWSER_PATTERNS) {
            if (userAgent.contains(pattern.marker)) {
                return pattern.browserName;
            }
        }
        return null;
    }

    private String parseBrowserVersion(String userAgent, String browserName) {
        if (userAgent == null || browserName == null) {
            return null;
        }

        try {
            String marker = null;
            switch (browserName) {
                case "Chrome" -> marker = "Chrome/";
                case "Firefox" -> marker = "Firefox/";
                case "Safari" -> marker = "Version/";
                case "Edge" -> marker = "Edg/";
                case "Opera" -> marker = "OPR/";
                default -> {
                    return null;
                }
            }

            int idx = userAgent.indexOf(marker);
            if (idx >= 0) {
                String version = userAgent.substring(idx + marker.length());
                int endIdx = version.indexOf(" ");
                if (endIdx < 0)
                    endIdx = version.length();
                return version.substring(0, Math.min(endIdx, 20));
            }
        } catch (Exception e) {
            log.trace("Failed to parse browser version from: {}", userAgent);
        }
        return null;
    }

    private String parseDeviceBrand(String userAgent, String secChUaModel) {
        if (secChUaModel != null && !secChUaModel.isEmpty()) {
            // Try to extract brand from model
            if (secChUaModel.toLowerCase().contains("iphone") ||
                    secChUaModel.toLowerCase().contains("ipad")) {
                return "Apple";
            }
            if (secChUaModel.toLowerCase().contains("samsung")) {
                return "Samsung";
            }
            if (secChUaModel.toLowerCase().contains("pixel")) {
                return "Google";
            }
        }

        if (userAgent == null) {
            return null;
        }

        String ua = userAgent.toLowerCase();
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("macintosh")) {
            return "Apple";
        }
        if (ua.contains("samsung")) {
            return "Samsung";
        }
        if (ua.contains("huawei")) {
            return "Huawei";
        }
        if (ua.contains("xiaomi") || ua.contains("redmi")) {
            return "Xiaomi";
        }
        if (ua.contains("pixel")) {
            return "Google";
        }
        if (ua.contains("oneplus")) {
            return "OnePlus";
        }
        if (ua.contains("oppo")) {
            return "Oppo";
        }
        if (ua.contains("vivo")) {
            return "Vivo";
        }
        return null;
    }

    private String parseDeviceModel(String userAgent) {
        if (userAgent == null) {
            return null;
        }

        // Try to extract model from Android User-Agent
        // Example: Mozilla/5.0 (Linux; Android 14; SM-S918B) ...
        if (userAgent.contains("Android")) {
            int startIdx = userAgent.indexOf("; ", userAgent.indexOf("Android"));
            if (startIdx > 0) {
                startIdx += 2;
                int endIdx = userAgent.indexOf(")", startIdx);
                if (endIdx > startIdx) {
                    String model = userAgent.substring(startIdx, endIdx);
                    // Remove "Build" suffix if present
                    if (model.contains(" Build")) {
                        model = model.substring(0, model.indexOf(" Build"));
                    }
                    return model.trim();
                }
            }
        }

        // iPhone/iPad model detection is limited from User-Agent
        if (userAgent.contains("iPhone")) {
            return "iPhone";
        }
        if (userAgent.contains("iPad")) {
            return "iPad";
        }

        return null;
    }

    private String classifyBotType(String userAgent) {
        if (userAgent == null) {
            return "UNKNOWN";
        }
        String ua = userAgent.toLowerCase();

        if (ua.contains("googlebot") || ua.contains("bingbot") || ua.contains("yandex") ||
                ua.contains("baidu") || ua.contains("duckduck") || ua.contains("slurp")) {
            return "SEARCH_ENGINE";
        }
        if (ua.contains("facebook") || ua.contains("twitter") || ua.contains("linkedin") ||
                ua.contains("whatsapp") || ua.contains("telegram") || ua.contains("slack")) {
            return "SOCIAL";
        }
        if (ua.contains("uptimerobot") || ua.contains("pingdom") || ua.contains("statuspage")) {
            return "MONITORING";
        }
        if (ua.contains("semrush") || ua.contains("ahrefs") || ua.contains("moz")) {
            return "SEO";
        }

        return "UNKNOWN";
    }

    private String detectProxyProvider(HttpServletRequest request) {
        // Cloudflare
        if (request.getHeader("CF-Ray") != null || request.getHeader("CF-Connecting-IP") != null) {
            return "Cloudflare";
        }
        // AWS
        if (request.getHeader("X-Amzn-Trace-Id") != null) {
            return "AWS";
        }
        // Google Cloud
        if (request.getHeader("X-Cloud-Trace-Context") != null) {
            return "GCP";
        }
        // Akamai
        if (request.getHeader("True-Client-IP") != null) {
            return "Akamai";
        }
        // Fastly
        if (request.getHeader("Fastly-Client-IP") != null) {
            return "Fastly";
        }
        return null;
    }

    private String detectConnectionType(HttpServletRequest request) {
        if (request.getHeader("X-Forwarded-For") != null) {
            return "proxy";
        }
        // TOR detection would require checking exit node IPs
        return "direct";
    }

    private Boolean detectAnonymization(HttpServletRequest request) {
        // Basic heuristics - real implementation would check against TOR exit nodes,
        // VPN IPs
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.toLowerCase().contains("tor")) {
            return true;
        }
        // Could check IP against known VPN/proxy lists here
        return false;
    }

    private String calculateRiskFactors(ClientContextDto context) {
        StringBuilder factors = new StringBuilder();

        if (Boolean.TRUE.equals(context.isBot())) {
            factors.append("BOT;");
        }
        if (Boolean.TRUE.equals(context.isAnonymized())) {
            factors.append("ANONYMIZED;");
        }
        if (context.userAgent() == null || context.userAgent().isEmpty()) {
            factors.append("NO_USER_AGENT;");
        }
        if (context.acceptLanguage() == null) {
            factors.append("NO_ACCEPT_LANG;");
        }
        if (context.deviceType() == DeviceType.UNKNOWN) {
            factors.append("UNKNOWN_DEVICE;");
        }
        if (context.userAgent() != null) {
            String ua = context.userAgent().toLowerCase();
            if (ua.contains("headless")) {
                factors.append("HEADLESS_BROWSER;");
            }
        }

        return factors.length() > 0 ? factors.toString() : null;
    }

    private String cleanQuotes(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\"", "").trim();
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

    // ========================================
    // Helper Records
    // ========================================

    private record OsPattern(String marker, String osName, String version) {
    }

    private record BrowserPattern(String marker, String browserName) {
    }
}
