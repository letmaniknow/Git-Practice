package com.mmva.newsapp.infrastructure.clientcontext.core.service;

import com.mmva.newsapp.infrastructure.clientcontext.core.dto.ClientContextDto;
import com.mmva.newsapp.infrastructure.clientcontext.core.enums.Channel;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service interface for extracting and enriching client context information.
 * 
 * <p>
 * Provides comprehensive client information extraction including:
 * </p>
 * <ul>
 * <li>Device detection and fingerprinting</li>
 * <li>Browser and OS parsing</li>
 * <li>GeoIP location lookup</li>
 * <li>Bot detection</li>
 * <li>Risk assessment</li>
 * </ul>
 * 
 * <h3>Usage:</h3>
 * 
 * <pre>{@code
 * @Autowired
 * private ClientContextService clientContextService;
 * 
 * // In a controller (with request)
 * ClientContextDto ctx = clientContextService.extractContext(request);
 * 
 * // In any service (without request, uses RequestContextHolder)
 * ClientContextDto ctx = clientContextService.getCurrentContext();
 * }</pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface ClientContextService {

    /**
     * Extracts full client context from an HTTP request.
     * This is the primary method for capturing client information.
     * 
     * @param request the HTTP servlet request
     * @return fully populated ClientContextDto
     */
    ClientContextDto extractContext(HttpServletRequest request);

    /**
     * Gets the current request's context using RequestContextHolder.
     * Can be called from any service layer without passing the request.
     * 
     * @return ClientContextDto for current request, or minimal context if not in
     *         request scope
     */
    ClientContextDto getCurrentContext();

    /**
     * Extracts a lightweight context with only essential fields.
     * Use for high-frequency operations where full extraction is expensive.
     * 
     * @param request the HTTP servlet request
     * @return ClientContextDto with only essential fields populated
     */
    ClientContextDto extractMinimalContext(HttpServletRequest request);

    /**
     * Generates a device fingerprint from request attributes.
     * Used for device tracking and fraud prevention.
     * 
     * @param request the HTTP servlet request
     * @return SHA-256 hash of fingerprint components
     */
    String generateDeviceFingerprint(HttpServletRequest request);

    /**
     * Calculates risk score based on client context.
     * Higher score indicates higher risk of fraud/abuse.
     * 
     * @param context the client context to assess
     * @return risk score from 0 (safe) to 100 (high risk)
     */
    int calculateRiskScore(ClientContextDto context);

    /**
     * Detects if the request is from a known bot/crawler.
     * 
     * @param userAgent the User-Agent string
     * @return true if bot detected
     */
    boolean isBot(String userAgent);

    /**
     * Gets the bot name if request is from a bot.
     * 
     * @param userAgent the User-Agent string
     * @return bot name or null if not a bot
     */
    String getBotName(String userAgent);

    /**
     * Parses the primary language from Accept-Language header.
     * 
     * @param acceptLanguage the Accept-Language header value
     * @return primary language code (e.g., "en", "es")
     */
    String parsePrimaryLanguage(String acceptLanguage);

    /**
     * Extracts the domain from a referer URL.
     * 
     * @param referer the full referer URL
     * @return domain only, or null if invalid
     */
    String extractRefererDomain(String referer);

    /**
     * Detects the access channel based on request characteristics.
     * 
     * @param request the HTTP servlet request
     * @return detected channel (WEB, MOBILE_WEB, IOS_APP, ANDROID_APP, API, etc.)
     */
    Channel detectChannel(HttpServletRequest request);

    /**
     * Enriches context with GeoIP information.
     * Call this separately if GeoIP lookup is optional/expensive.
     * 
     * @param context   the context to enrich
     * @param ipAddress the IP address to lookup
     * @return enriched context with location data
     */
    ClientContextDto enrichWithGeoIP(ClientContextDto context, String ipAddress);
}
