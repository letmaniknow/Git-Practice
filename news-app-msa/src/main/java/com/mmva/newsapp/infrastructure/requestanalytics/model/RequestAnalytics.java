package com.mmva.newsapp.infrastructure.requestanalytics.model;

import com.mmva.newsapp.infrastructure.common.util.MapToJsonConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Entity for storing comprehensive HTTP request analytics data.
 * 
 * <p>
 * Captures ALL available information from HttpServletRequest for:
 * </p>
 * <ul>
 * <li>Traffic analytics and reporting</li>
 * <li>Security monitoring and threat detection</li>
 * <li>User behavior analysis</li>
 * <li>Performance optimization</li>
 * <li>Geographic and device analytics</li>
 * <li>Debugging and troubleshooting</li>
 * </ul>
 * 
 * <h3>Storage Considerations:</h3>
 * <p>
 * This table can grow quickly. Consider:
 * </p>
 * <ul>
 * <li>Partitioning by date</li>
 * <li>Retention policies (e.g., delete after 90 days)</li>
 * <li>Archiving to cold storage</li>
 * <li>Sampling for high-traffic endpoints</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "request_analytics", indexes = {
        @Index(name = "idx_request_analytics_created_at", columnList = "createdAt"),
        @Index(name = "idx_request_analytics_client_ip", columnList = "clientIpAddress"),
        @Index(name = "idx_request_analytics_user_id", columnList = "userId"),
        @Index(name = "idx_request_analytics_endpoint", columnList = "method, requestUri"),
        @Index(name = "idx_request_analytics_response_status", columnList = "responseStatus"),
        @Index(name = "idx_request_analytics_created_endpoint", columnList = "createdAt, requestUri")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ========================================
    // Timestamp & Identity
    // ========================================

    @CreatedDate
    @Column(name = "createdAt", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "requestId", length = 100)
    private String requestId;

    @Column(name = "correlationId", length = 100)
    private String correlationId;

    @Column(name = "userId")
    private UUID userId;

    @Column(name = "username", length = 255)
    private String username;

    // ========================================
    // Connection Information
    // ========================================

    @Column(name = "clientIpAddress", length = 45)
    private String clientIpAddress;

    @Column(name = "remoteAddress", length = 45)
    private String remoteAddress;

    @Column(name = "remoteHost", length = 255)
    private String remoteHost;

    @Column(name = "remotePort")
    private Integer remotePort;

    @Column(name = "serverPort")
    private Integer serverPort;

    @Column(name = "protocol", length = 20)
    private String protocol;

    @Column(name = "isSecure")
    private Boolean isSecure;

    // ========================================
    // Request Information
    // ========================================

    @Column(name = "method", length = 10)
    private String method;

    @Column(name = "requestUri", length = 2048)
    private String requestUri;

    @Column(name = "requestUrl", length = 2048)
    private String requestUrl;

    @Column(name = "queryString", length = 4096)
    private String queryString;

    @Column(name = "contentType", length = 255)
    private String contentType;

    @Column(name = "contentLength")
    private Long contentLength;

    // ========================================
    // Client Information
    // ========================================

    @Column(name = "userAgent", length = 1024)
    private String userAgent;

    @Column(name = "acceptHeader", length = 512)
    private String acceptHeader;

    @Column(name = "acceptLanguage", length = 255)
    private String acceptLanguage;

    @Column(name = "acceptEncoding", length = 255)
    private String acceptEncoding;

    @Column(name = "host", length = 255)
    private String host;

    @Column(name = "referer", length = 2048)
    private String referer;

    @Column(name = "origin", length = 255)
    private String origin;

    // ========================================
    // Proxy/Forwarding Headers
    // ========================================

    @Column(name = "xForwardedFor", length = 512)
    private String xForwardedFor;

    @Column(name = "xForwardedProto", length = 10)
    private String xForwardedProto;

    @Column(name = "xForwardedHost", length = 255)
    private String xForwardedHost;

    // ========================================
    // Security Information
    // ========================================

    @Column(name = "authType", length = 50)
    private String authType;

    @Column(name = "secFetchSite", length = 50)
    private String secFetchSite;

    @Column(name = "secFetchMode", length = 50)
    private String secFetchMode;

    @Column(name = "cookieCount")
    private Integer cookieCount;

    // ========================================
    // Client Hints
    // ========================================

    @Column(name = "secChUa", length = 512)
    private String secChUa;

    @Column(name = "secChUaMobile", length = 10)
    private String secChUaMobile;

    @Column(name = "secChUaPlatform", length = 50)
    private String secChUaPlatform;

    @Column(name = "secChUaPlatformVersion", length = 50)
    private String secChUaPlatformVersion;

    @Column(name = "secChUaArch", length = 20)
    private String secChUaArch;

    @Column(name = "deviceMemory", length = 10)
    private String deviceMemory;

    @Column(name = "ect", length = 20)
    private String ect;

    @Column(name = "saveData", length = 10)
    private String saveData;

    // ========================================
    // Derived/Computed Fields
    // ========================================

    @Column(name = "browserName", length = 50)
    private String browserName;

    @Column(name = "browserVersion", length = 50)
    private String browserVersion;

    @Column(name = "osName", length = 50)
    private String osName;

    @Column(name = "osVersion", length = 50)
    private String osVersion;

    @Column(name = "deviceType", length = 20)
    private String deviceType;

    @Column(name = "isBot")
    private Boolean isBot;

    // ========================================
    // Response Information
    // ========================================

    @Column(name = "responseStatus")
    private Integer responseStatus;

    @Column(name = "responseContentType", length = 255)
    private String responseContentType;

    @Column(name = "responseContentLength")
    private Long responseContentLength;

    @Column(name = "processingTimeMs")
    private Long processingTimeMs;

    @Column(name = "hasError")
    private Boolean hasError;

    @Column(name = "errorMessage", length = 1024)
    private String errorMessage;

    @Column(name = "exceptionClass", length = 255)
    private String exceptionClass;

    // ========================================
    // Session & Context
    // ========================================

    @Column(name = "sessionId", length = 100)
    private String sessionId;

    @Column(name = "isNewSession")
    private Boolean isNewSession;

    @Column(name = "dispatcherType", length = 20)
    private String dispatcherType;

    // ========================================
    // Full Header Map (JSON)
    // ========================================

    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "all_headers", columnDefinition = "TEXT")
    private Map<String, String> allHeaders;

    @Column(name = "headerCount")
    private Integer headerCount;

    @Column(name = "parameterCount")
    private Integer parameterCount;

    // ========================================
    // Geographic Information (enrichment)
    // ========================================

    @Column(name = "geoCountry", length = 2)
    private String geoCountry;

    @Column(name = "geoRegion", length = 100)
    private String geoRegion;

    @Column(name = "geoCity", length = 100)
    private String geoCity;

    @Column(name = "geoTimezone", length = 50)
    private String geoTimezone;

    @Column(name = "geoIsp", length = 255)
    private String geoIsp;
}
