package com.mmva.newsapp.infrastructure.requestanalytics.service;

import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestAnalyticsInfoDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

/**
 * Default implementation of {@link RequestInfoService}.
 * 
 * <p>
 * Extracts information from HTTP requests for analytics and audit purposes.
 * Supports both explicit request passing and automatic request context
 * resolution.
 * </p>
 * 
 * <h3>Naming Convention:</h3>
 * <p>
 * Per PROJECT_PRINCIPLES.md section 6.3, interfaces use the plain name
 * (e.g., {@code RequestInfoService}) and implementations use the Impl suffix
 * (e.g., {@code RequestInfoServiceImpl}). No "I" prefix is used per
 * Java/Spring conventions.
 * </p>
 * 
 * <h3>Usage Examples:</h3>
 * 
 * <pre>{@code
 * // In Controller - pass request explicitly
 * ClientInfo clientInfo = requestInfoService.getClientInfo(request);
 * 
 * // In any Service - get from current context
 * ClientInfo clientInfo = requestInfoService.getCurrentClientInfo();
 * }</pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class RequestInfoServiceImpl implements RequestInfoService {

    // Common header names for proxy-forwarded client IP
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_X_REAL_IP = "X-Real-IP";
    private static final String HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";
    private static final String HEADER_X_FORWARDED_HOST = "X-Forwarded-Host";
    private static final String HEADER_PROXY_CLIENT_IP = "Proxy-Client-IP";
    private static final String HEADER_WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    private static final String HEADER_HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    private static final String HEADER_HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";

    // Client hints headers
    private static final String HEADER_SEC_CH_UA = "Sec-CH-UA";
    private static final String HEADER_SEC_CH_UA_MOBILE = "Sec-CH-UA-Mobile";
    private static final String HEADER_SEC_CH_UA_PLATFORM = "Sec-CH-UA-Platform";

    // Additional client hints
    private static final String HEADER_SEC_CH_UA_PLATFORM_VERSION = "Sec-CH-UA-Platform-Version";
    private static final String HEADER_SEC_CH_UA_ARCH = "Sec-CH-UA-Arch";
    private static final String HEADER_SEC_CH_UA_BITNESS = "Sec-CH-UA-Bitness";
    private static final String HEADER_SEC_CH_UA_MODEL = "Sec-CH-UA-Model";
    private static final String HEADER_SEC_CH_UA_FULL_VERSION_LIST = "Sec-CH-UA-Full-Version-List";

    // ========================================
    // Current Request Context Methods
    // ========================================

    /**
     * Gets the current HTTP request from RequestContextHolder.
     * Can be called from any service without passing the request.
     * 
     * @return the current HttpServletRequest, or null if not in request context
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestClientInfoDto getCurrentClientInfo() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            log.debug("getCurrentClientInfo called outside of request context");
            return RequestClientInfoDto.minimal("unknown", "unknown");
        }
        return getClientInfo(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestAnalyticsInfoDto getCurrentFullRequestInfo() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            log.debug("getCurrentFullRequestInfo called outside of request context");
            return RequestAnalyticsInfoDto.empty();
        }
        return getFullRequestInfo(request);
    }

    // ========================================
    // Full Request Info Extraction
    // ========================================

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestAnalyticsInfoDto getFullRequestInfo(HttpServletRequest request) {
        if (request == null) {
            log.warn("RequestInfoServiceImpl.getFullRequestInfo called with null request");
            return RequestAnalyticsInfoDto.empty();
        }

        try {
            // Get all headers as a map
            Map<String, String> allHeaders = getAllHeaders(request);

            // Get all parameters
            Map<String, String[]> allParams = new LinkedHashMap<>(request.getParameterMap());

            // Session info (don't create session if it doesn't exist)
            HttpSession session = request.getSession(false);
            String sessionId = session != null ? session.getId() : null;
            Boolean isNewSession = session != null ? session.isNew() : null;
            Boolean hasValidSession = session != null;

            // Cookie count (not values for security)
            Cookie[] cookies = request.getCookies();
            Integer cookieCount = cookies != null ? cookies.length : 0;

            // Auth type extraction
            String authHeader = request.getHeader("Authorization");
            String authType = extractAuthType(authHeader);

            // Locales
            String locales = extractLocales(request);

            // Build full URL
            String requestUrl = buildFullUrl(request);

            return new RequestAnalyticsInfoDto(
                    // Connection Info
                    getClientIpAddress(request),
                    request.getRemoteAddr(),
                    request.getRemoteHost(),
                    request.getRemotePort(),
                    request.getLocalAddr(),
                    request.getLocalName(),
                    request.getLocalPort(),
                    request.getServerName(),
                    request.getServerPort(),
                    request.getProtocol(),
                    request.getScheme(),
                    request.isSecure() || "https".equalsIgnoreCase(request.getHeader(HEADER_X_FORWARDED_PROTO)),

                    // Request Info
                    request.getMethod(),
                    request.getRequestURI(),
                    requestUrl,
                    request.getQueryString(),
                    request.getContextPath(),
                    request.getServletPath(),
                    request.getPathInfo(),
                    request.getPathTranslated(),
                    request.getContentType(),
                    request.getContentLengthLong() >= 0 ? request.getContentLengthLong() : null,
                    request.getCharacterEncoding(),
                    request.getLocale() != null ? request.getLocale().toString() : null,
                    locales,

                    // Client Headers
                    request.getHeader("User-Agent"),
                    request.getHeader("Accept"),
                    request.getHeader("Accept-Language"),
                    request.getHeader("Accept-Encoding"),
                    request.getHeader("Accept-Charset"),
                    request.getHeader("Host"),
                    request.getHeader("Connection"),
                    request.getHeader("Cache-Control"),
                    request.getHeader("Pragma"),
                    request.getHeader("DNT"),
                    request.getHeader("Referer"),
                    request.getHeader("Origin"),

                    // Proxy/Forwarding Headers
                    request.getHeader(HEADER_X_FORWARDED_FOR),
                    request.getHeader(HEADER_X_FORWARDED_PROTO),
                    request.getHeader(HEADER_X_FORWARDED_HOST),
                    request.getHeader("X-Forwarded-Port"),
                    request.getHeader(HEADER_X_REAL_IP),
                    request.getHeader("Via"),
                    request.getHeader("Forwarded"),

                    // Security Headers
                    authType,
                    request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null,
                    request.getAuthType(),
                    request.getHeader("Sec-Fetch-Site"),
                    request.getHeader("Sec-Fetch-Mode"),
                    request.getHeader("Sec-Fetch-User"),
                    request.getHeader("Sec-Fetch-Dest"),
                    cookieCount,
                    request.getHeader("X-CSRF-TOKEN") != null || request.getHeader("X-XSRF-TOKEN") != null
                            ? "present"
                            : null,

                    // Client Hints
                    request.getHeader(HEADER_SEC_CH_UA),
                    request.getHeader(HEADER_SEC_CH_UA_MOBILE),
                    cleanQuotes(request.getHeader(HEADER_SEC_CH_UA_PLATFORM)),
                    cleanQuotes(request.getHeader(HEADER_SEC_CH_UA_PLATFORM_VERSION)),
                    cleanQuotes(request.getHeader(HEADER_SEC_CH_UA_ARCH)),
                    cleanQuotes(request.getHeader(HEADER_SEC_CH_UA_BITNESS)),
                    cleanQuotes(request.getHeader(HEADER_SEC_CH_UA_MODEL)),
                    request.getHeader(HEADER_SEC_CH_UA_FULL_VERSION_LIST),
                    request.getHeader("Device-Memory"),
                    request.getHeader("Downlink"),
                    request.getHeader("ECT"),
                    request.getHeader("RTT"),
                    request.getHeader("Save-Data"),
                    request.getHeader("Viewport-Width"),
                    request.getHeader("Width"),
                    request.getHeader("DPR"),

                    // Content Negotiation
                    request.getHeader("If-Modified-Since"),
                    request.getHeader("If-None-Match"),
                    request.getHeader("If-Match"),
                    request.getHeader("If-Unmodified-Since"),
                    request.getHeader("Range"),

                    // CORS
                    request.getHeader("Access-Control-Request-Method"),
                    request.getHeader("Access-Control-Request-Headers"),

                    // Tracing Headers
                    request.getHeader("X-Request-ID"),
                    request.getHeader("X-Correlation-ID"),
                    request.getHeader("X-Trace-ID"),
                    request.getHeader("X-Span-ID"),
                    request.getHeader("X-B3-TraceId"),
                    request.getHeader("X-B3-SpanId"),
                    request.getHeader("traceparent"),
                    request.getHeader("X-Api-Key") != null,

                    // Session
                    sessionId,
                    isNewSession,
                    hasValidSession,

                    // Servlet Container
                    request.getDispatcherType() != null ? request.getDispatcherType().name() : null,
                    request.isAsyncStarted(),
                    request.isAsyncSupported(),

                    // Full maps
                    allHeaders,
                    allParams,
                    allHeaders.size(),
                    allParams.size());
        } catch (Exception e) {
            log.error("Error extracting full request info: {}", e.getMessage());
            return RequestAnalyticsInfoDto.empty();
        }
    }

    // ========================================
    // Lightweight Client Info Extraction
    // ========================================

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestClientInfoDto getClientInfo(HttpServletRequest request) {
        if (request == null) {
            log.warn("RequestInfoServiceImpl.getClientInfo called with null request");
            return RequestClientInfoDto.minimal("unknown", "unknown");
        }

        return new RequestClientInfoDto(
                getClientIpAddress(request),
                getUserAgent(request),
                getAcceptLanguage(request),
                getReferer(request),
                getOrigin(request),
                getDeviceType(request),
                getPlatform(request),
                request.getRequestURI(),
                request.getMethod());
    }

    // ========================================
    // Individual Field Extractors
    // ========================================

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = null;

        // Try X-Forwarded-For first (most common for proxies/load balancers)
        ip = request.getHeader(HEADER_X_FORWARDED_FOR);
        if (isValidIp(ip)) {
            return ip.split(",")[0].trim();
        }

        // Try X-Real-IP (commonly used by Nginx)
        ip = request.getHeader(HEADER_X_REAL_IP);
        if (isValidIp(ip)) {
            return ip.trim();
        }

        // Try Proxy-Client-IP (Apache)
        ip = request.getHeader(HEADER_PROXY_CLIENT_IP);
        if (isValidIp(ip)) {
            return ip.trim();
        }

        // Try WL-Proxy-Client-IP (WebLogic)
        ip = request.getHeader(HEADER_WL_PROXY_CLIENT_IP);
        if (isValidIp(ip)) {
            return ip.trim();
        }

        // Try HTTP_CLIENT_IP
        ip = request.getHeader(HEADER_HTTP_CLIENT_IP);
        if (isValidIp(ip)) {
            return ip.trim();
        }

        // Try HTTP_X_FORWARDED_FOR
        ip = request.getHeader(HEADER_HTTP_X_FORWARDED_FOR);
        if (isValidIp(ip)) {
            return ip.split(",")[0].trim();
        }

        // Fall back to remote address
        return request.getRemoteAddr();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAcceptLanguage(HttpServletRequest request) {
        return request != null ? request.getHeader("Accept-Language") : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReferer(HttpServletRequest request) {
        return request != null ? request.getHeader("Referer") : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOrigin(HttpServletRequest request) {
        return request != null ? request.getHeader("Origin") : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDeviceType(HttpServletRequest request) {
        return request != null ? request.getHeader(HEADER_SEC_CH_UA_MOBILE) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPlatform(HttpServletRequest request) {
        String platform = request != null ? request.getHeader(HEADER_SEC_CH_UA_PLATFORM) : null;
        return cleanQuotes(platform);
    }

    /**
     * Gets the browser info from Sec-CH-UA header.
     */
    public String getBrowserInfo(HttpServletRequest request) {
        return request != null ? request.getHeader(HEADER_SEC_CH_UA) : null;
    }

    /**
     * Gets the forwarded protocol (http/https) from X-Forwarded-Proto.
     */
    public String getForwardedProtocol(HttpServletRequest request) {
        return request != null ? request.getHeader(HEADER_X_FORWARDED_PROTO) : null;
    }

    /**
     * Gets the forwarded host from X-Forwarded-Host.
     */
    public String getForwardedHost(HttpServletRequest request) {
        return request != null ? request.getHeader(HEADER_X_FORWARDED_HOST) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAllHeaders(HttpServletRequest request) {
        if (request == null) {
            return Collections.emptyMap();
        }

        Map<String, String> headers = new LinkedHashMap<>();
        var headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name.toLowerCase(), request.getHeader(name));
        }

        return headers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getSecurityHeaders(HttpServletRequest request) {
        if (request == null) {
            return Collections.emptyMap();
        }

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("client_ip", getClientIpAddress(request));
        headers.put("user_agent", getUserAgent(request));
        headers.put("origin", getOrigin(request));
        headers.put("referer", getReferer(request));
        headers.put("x_forwarded_for", request.getHeader(HEADER_X_FORWARDED_FOR));
        headers.put("x_forwarded_proto", request.getHeader(HEADER_X_FORWARDED_PROTO));
        headers.put("x_forwarded_host", request.getHeader(HEADER_X_FORWARDED_HOST));

        headers.values().removeIf(Objects::isNull);

        return headers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSecureRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        if (request.isSecure()) {
            return true;
        }
        String proto = getForwardedProtocol(request);
        return "https".equalsIgnoreCase(proto);
    }

    // ========================================
    // Helper Methods
    // ========================================

    private String extractAuthType(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        int spaceIndex = authHeader.indexOf(' ');
        if (spaceIndex > 0) {
            return authHeader.substring(0, spaceIndex);
        }
        return authHeader.length() > 20 ? "Unknown" : authHeader;
    }

    private String extractLocales(HttpServletRequest request) {
        Enumeration<Locale> locales = request.getLocales();
        if (locales == null) {
            return null;
        }
        List<String> localeList = new ArrayList<>();
        while (locales.hasMoreElements() && localeList.size() < 10) {
            localeList.add(locales.nextElement().toString());
        }
        return String.join(", ", localeList);
    }

    private String buildFullUrl(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString != null) {
            url.append('?').append(queryString);
        }
        return url.toString();
    }

    private String cleanQuotes(String value) {
        if (value != null && value.startsWith("\"") && value.endsWith("\"") && value.length() > 2) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}
