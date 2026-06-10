package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.util.Map;

/**
 * Comprehensive record containing ALL possible information from
 * HttpServletRequest.
 * Used for analytics, debugging, and audit purposes.
 * 
 * <h3>Categories of Information (96 fields total):</h3>
 * <ul>
 * <li>Connection Info (12) - IP, port, protocol</li>
 * <li>Request Info (13) - Method, URI, query, content</li>
 * <li>Client Headers (12) - User-Agent, language, encoding</li>
 * <li>Proxy/Forwarding (7) - Forwarded IPs, hosts, protocols</li>
 * <li>Security Info (9) - Auth, HTTPS, cookies</li>
 * <li>Client Hints (16) - Modern browser capabilities</li>
 * <li>Content Negotiation (5) - Caching, ranges</li>
 * <li>CORS Headers (2) - Cross-origin requests</li>
 * <li>Tracing Headers (8) - Distributed tracing IDs</li>
 * <li>Session Info (3) - Session ID, status</li>
 * <li>Servlet Container (3) - Dispatcher, async</li>
 * <li>Complete Maps (4) - All headers, parameters</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public record RequestAnalyticsInfoDto(
        // ========================================
        // Connection Information (12 fields)
        // ========================================
        /** Client IP address (resolved through proxies) */
        String clientIpAddress,
        /** Raw remote address from socket */
        String remoteAddress,
        /** Remote host name */
        String remoteHost,
        /** Remote port number */
        Integer remotePort,
        /** Local server IP address */
        String localAddress,
        /** Local server name */
        String localName,
        /** Local server port */
        Integer localPort,
        /** Server name from Host header or request */
        String serverName,
        /** Server port from request */
        Integer serverPort,
        /** Protocol (HTTP/1.1, HTTP/2, etc.) */
        String protocol,
        /** Scheme (http or https) */
        String scheme,
        /** Whether connection is secure (HTTPS) */
        Boolean isSecure,

        // ========================================
        // Request Information (13 fields)
        // ========================================
        /** HTTP method (GET, POST, PUT, DELETE, etc.) */
        String method,
        /** Request URI path */
        String requestUri,
        /** Full request URL */
        String requestUrl,
        /** Query string (parameters after ?) */
        String queryString,
        /** Context path of the application */
        String contextPath,
        /** Servlet path */
        String servletPath,
        /** Path info after servlet path */
        String pathInfo,
        /** Translated path on server */
        String pathTranslated,
        /** Content type of request body */
        String contentType,
        /** Content length of request body (-1 if unknown) */
        Long contentLength,
        /** Character encoding of request */
        String characterEncoding,
        /** Request locale */
        String locale,
        /** All accepted locales */
        String locales,

        // ========================================
        // Client Information Headers (12 fields)
        // ========================================
        /** User-Agent header - browser/client identification */
        String userAgent,
        /** Accept header - accepted content types */
        String accept,
        /** Accept-Language header - preferred languages */
        String acceptLanguage,
        /** Accept-Encoding header - supported compressions */
        String acceptEncoding,
        /** Accept-Charset header - supported character sets */
        String acceptCharset,
        /** Host header */
        String host,
        /** Connection header (keep-alive, close) */
        String connection,
        /** Cache-Control header */
        String cacheControl,
        /** Pragma header */
        String pragma,
        /** DNT (Do Not Track) header */
        String doNotTrack,
        /** Referer header - referring page */
        String referer,
        /** Origin header - request origin (CORS) */
        String origin,

        // ========================================
        // Proxy/Forwarding Headers (7 fields)
        // ========================================
        /** X-Forwarded-For - original client IP chain */
        String xForwardedFor,
        /** X-Forwarded-Proto - original protocol */
        String xForwardedProto,
        /** X-Forwarded-Host - original host */
        String xForwardedHost,
        /** X-Forwarded-Port - original port */
        String xForwardedPort,
        /** X-Real-IP - real client IP (Nginx) */
        String xRealIp,
        /** Via header - proxy chain */
        String via,
        /** Forwarded header (RFC 7239) */
        String forwarded,

        // ========================================
        // Security Headers (9 fields)
        // ========================================
        /** Authorization header (type only, not value for security) */
        String authorizationType,
        /** Authenticated user principal name */
        String userPrincipal,
        /** Authentication type (BASIC, BEARER, etc.) */
        String authType,
        /** Sec-Fetch-Site header */
        String secFetchSite,
        /** Sec-Fetch-Mode header */
        String secFetchMode,
        /** Sec-Fetch-User header */
        String secFetchUser,
        /** Sec-Fetch-Dest header */
        String secFetchDest,
        /** Cookie count (not values for security) */
        Integer cookieCount,
        /** CSRF token header name if present */
        String csrfTokenPresent,

        // ========================================
        // Client Hints - Modern Browsers (16 fields)
        // ========================================
        /** Sec-CH-UA - browser brand and version */
        String secChUa,
        /** Sec-CH-UA-Mobile - mobile device indicator */
        String secChUaMobile,
        /** Sec-CH-UA-Platform - operating system */
        String secChUaPlatform,
        /** Sec-CH-UA-Platform-Version - OS version */
        String secChUaPlatformVersion,
        /** Sec-CH-UA-Arch - CPU architecture */
        String secChUaArch,
        /** Sec-CH-UA-Bitness - 32 or 64 bit */
        String secChUaBitness,
        /** Sec-CH-UA-Model - device model */
        String secChUaModel,
        /** Sec-CH-UA-Full-Version-List - detailed versions */
        String secChUaFullVersionList,
        /** Device-Memory - device RAM in GB */
        String deviceMemory,
        /** Downlink - network speed estimate */
        String downlink,
        /** ECT - effective connection type */
        String ect,
        /** RTT - round trip time estimate */
        String rtt,
        /** Save-Data - data saver mode */
        String saveData,
        /** Viewport-Width */
        String viewportWidth,
        /** Width - resource width */
        String width,
        /** DPR - device pixel ratio */
        String dpr,

        // ========================================
        // Content Negotiation (5 fields)
        // ========================================
        /** If-Modified-Since header */
        String ifModifiedSince,
        /** If-None-Match header (ETag) */
        String ifNoneMatch,
        /** If-Match header */
        String ifMatch,
        /** If-Unmodified-Since header */
        String ifUnmodifiedSince,
        /** Range header for partial content */
        String range,

        // ========================================
        // CORS Headers (2 fields)
        // ========================================
        /** Access-Control-Request-Method */
        String accessControlRequestMethod,
        /** Access-Control-Request-Headers */
        String accessControlRequestHeaders,

        // ========================================
        // Custom/Application Tracing Headers (8 fields)
        // ========================================
        /** X-Request-ID for request tracing */
        String xRequestId,
        /** X-Correlation-ID for distributed tracing */
        String xCorrelationId,
        /** X-Trace-ID */
        String xTraceId,
        /** X-Span-ID */
        String xSpanId,
        /** X-B3-TraceId (Zipkin) */
        String xB3TraceId,
        /** X-B3-SpanId (Zipkin) */
        String xB3SpanId,
        /** traceparent (W3C Trace Context) */
        String traceparent,
        /** X-Api-Key header presence */
        Boolean xApiKeyPresent,

        // ========================================
        // Session Information (3 fields)
        // ========================================
        /** Session ID (if session exists) */
        String sessionId,
        /** Whether session is new */
        Boolean isNewSession,
        /** Whether request has valid session */
        Boolean hasValidSession,

        // ========================================
        // Servlet Container Info (3 fields)
        // ========================================
        /** Dispatcher type (REQUEST, FORWARD, INCLUDE, ERROR, ASYNC) */
        String dispatcherType,
        /** Whether async started */
        Boolean isAsyncStarted,
        /** Whether async supported */
        Boolean isAsyncSupported,

        // ========================================
        // Complete Maps (4 fields)
        // ========================================
        /** All request headers as map */
        Map<String, String> allHeaders,
        /** All request parameters as map */
        Map<String, String[]> allParameters,
        /** Count of total headers */
        Integer headerCount,
        /** Count of total parameters */
        Integer parameterCount) {

    /**
     * Creates an empty RequestAnalyticsInfo for error cases.
     * 
     * @return RequestAnalyticsInfo with default/null values
     */
    public static RequestAnalyticsInfoDto empty() {
        return new RequestAnalyticsInfoDto(
                // Connection (12)
                "unknown", null, null, null, null, null, null, null, null, null, null, null,
                // Request (13)
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                // Client Headers (12)
                null, null, null, null, null, null, null, null, null, null, null, null,
                // Proxy/Forwarding (7)
                null, null, null, null, null, null, null,
                // Security (9)
                null, null, null, null, null, null, null, null, null,
                // Client Hints (16)
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // Content Negotiation (5)
                null, null, null, null, null,
                // CORS (2)
                null, null,
                // Tracing (8)
                null, null, null, null, null, null, null, null,
                // Session (3)
                null, null, null,
                // Servlet (3)
                null, null, null,
                // Maps (4)
                java.util.Collections.emptyMap(), java.util.Collections.emptyMap(), 0, 0);
    }

    /**
     * Gets a compact summary for logging.
     */
    public String toLogSummary() {
        return String.format(
                "[%s %s] client=%s ua=%s infrastructure=%s secure=%s",
                method,
                requestUri,
                clientIpAddress,
                truncate(userAgent, 30),
                secChUaPlatform,
                isSecure);
    }

    /**
     * Checks if the request appears to be from a mobile device.
     */
    public boolean isMobile() {
        if ("?1".equals(secChUaMobile)) {
            return true;
        }
        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            return ua.contains("mobile") || ua.contains("android") ||
                    ua.contains("iphone") || ua.contains("ipad");
        }
        return false;
    }

    /**
     * Checks if the request is from a known bot/crawler.
     */
    public boolean isBot() {
        if (userAgent == null)
            return false;
        String ua = userAgent.toLowerCase();
        return ua.contains("bot") || ua.contains("crawler") || ua.contains("spider") ||
                ua.contains("slurp") || ua.contains("googlebot") || ua.contains("bingbot") ||
                ua.contains("yandex") || ua.contains("baidu") || ua.contains("duckduck") ||
                ua.contains("facebookexternalhit") || ua.contains("twitterbot") ||
                ua.contains("linkedinbot") || ua.contains("whatsapp") || ua.contains("telegram");
    }

    /**
     * Gets the detected browser name from User-Agent or Client Hints.
     */
    public String getBrowserName() {
        if (secChUa != null && !secChUa.isEmpty()) {
            if (secChUa.contains("Chrome"))
                return "Chrome";
            if (secChUa.contains("Firefox"))
                return "Firefox";
            if (secChUa.contains("Safari"))
                return "Safari";
            if (secChUa.contains("Edge"))
                return "Edge";
            if (secChUa.contains("Opera"))
                return "Opera";
        }
        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            if (ua.contains("edg/"))
                return "Edge";
            if (ua.contains("chrome"))
                return "Chrome";
            if (ua.contains("firefox"))
                return "Firefox";
            if (ua.contains("safari"))
                return "Safari";
            if (ua.contains("opera") || ua.contains("opr/"))
                return "Opera";
            if (ua.contains("msie") || ua.contains("trident"))
                return "IE";
        }
        return "Unknown";
    }

    /**
     * Gets the detected OS from Client Hints or User-Agent.
     */
    public String getOsName() {
        if (secChUaPlatform != null && !secChUaPlatform.isEmpty()) {
            return secChUaPlatform.replace("\"", "");
        }
        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            if (ua.contains("windows"))
                return "Windows";
            if (ua.contains("mac os"))
                return "macOS";
            if (ua.contains("linux"))
                return "Linux";
            if (ua.contains("android"))
                return "Android";
            if (ua.contains("iphone") || ua.contains("ipad"))
                return "iOS";
            if (ua.contains("chromeos"))
                return "ChromeOS";
        }
        return "Unknown";
    }

    private String truncate(String value, int maxLength) {
        if (value == null)
            return "null";
        if (value.length() <= maxLength)
            return value;
        return value.substring(0, maxLength) + "...";
    }
}
