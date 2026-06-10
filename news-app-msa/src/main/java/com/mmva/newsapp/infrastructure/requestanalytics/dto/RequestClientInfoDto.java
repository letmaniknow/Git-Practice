package com.mmva.newsapp.infrastructure.requestanalytics.dto;

/**
 * Immutable record containing client request information.
 * Extracted from HttpServletRequest for audit logging and analytics.
 * 
 * <p>
 * This is a lightweight version for quick access to common client info.
 * For comprehensive analytics, use {@link RequestAnalyticsInfoDto}.
 * </p>
 * 
 * @param ipAddress      Client IP address (handles proxies)
 * @param userAgent      Browser/client user agent string
 * @param acceptLanguage Preferred language(s)
 * @param referer        Referring page URL
 * @param origin         Request origin (for CORS)
 * @param deviceType     Device type hint (from Sec-CH-UA-Mobile)
 * @param platform       Platform/OS hint (from Sec-CH-UA-Platform)
 * @param requestUri     The request URI path
 * @param requestMethod  HTTP method (GET, POST, etc.)
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public record RequestClientInfoDto(
        String ipAddress,
        String userAgent,
        String acceptLanguage,
        String referer,
        String origin,
        String deviceType,
        String platform,
        String requestUri,
        String requestMethod) {

    /**
     * Creates a minimal ClientInfo with only IP and User-Agent.
     * Useful for audit logging where only basic info is needed.
     * 
     * @param ipAddress the client IP address
     * @param userAgent the user agent string
     * @return ClientInfo with minimal data
     */
    public static RequestClientInfoDto minimal(String ipAddress, String userAgent) {
        return new RequestClientInfoDto(ipAddress, userAgent, null, null, null, null, null, null, null);
    }

    /**
     * Checks if the request appears to be from a mobile device.
     * Based on Sec-CH-UA-Mobile header or User-Agent analysis.
     * 
     * @return true if mobile device detected
     */
    public boolean isMobile() {
        if ("?1".equals(deviceType) || "mobile".equalsIgnoreCase(deviceType)) {
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
     * Gets a summary string for logging.
     * 
     * @return formatted summary string
     */
    public String toLogString() {
        return String.format("ClientInfo[ip=%s, ua=%s, lang=%s]",
                ipAddress,
                truncate(userAgent, 50),
                acceptLanguage);
    }

    private String truncate(String value, int maxLength) {
        if (value == null)
            return "null";
        if (value.length() <= maxLength)
            return value;
        return value.substring(0, maxLength) + "...";
    }
}
