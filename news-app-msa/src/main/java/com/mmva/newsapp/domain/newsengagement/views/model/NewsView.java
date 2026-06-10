package com.mmva.newsapp.domain.newsengagement.views.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;

/**
 * News view entity for tracking newsapp views.
 * 
 * <p>
 * Table: news_views
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * <p>
 * Captures comprehensive client context for analytics including:
 * </p>
 * <ul>
 * <li>Device information (type, fingerprint)</li>
 * <li>Location data (country, city)</li>
 * <li>Browser/OS details</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "news_views", indexes = {
        @Index(name = "idx_news_views_news_id", columnList = "news_views_news_id"),
        @Index(name = "idx_news_views_user_id", columnList = "news_views_user_id"),
        @Index(name = "idx_news_views_viewed_at", columnList = "news_views_viewed_at"),
        @Index(name = "idx_news_views_country_code", columnList = "news_views_country_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_views_id", updatable = false, nullable = false)
    private Long newsViewsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_views_news_id", referencedColumnName = "news_news_id", nullable = false, insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private NewsMasterEntity news;

    @Column(name = "news_views_news_id", nullable = false)
    private UUID newsViewsNewsId;

    @Column(name = "news_views_user_id")
    private UUID newsViewsUserId;

    // ========================================
    // Network Information
    // ========================================

    @Column(name = "news_views_ip_address", length = 100)
    private String newsViewsIpAddress;

    // ========================================
    // Device Information
    // ========================================

    /** Device type: MOBILE, TABLET, DESKTOP, TV, WEARABLE, BOT, UNKNOWN */
    @Column(name = "news_views_device_type", length = 20)
    private String newsViewsDeviceType;

    /** Device fingerprint for unique device tracking */
    @Column(name = "news_views_device_fingerprint", length = 128)
    private String newsViewsDeviceFingerprint;

    /** Browser name: Chrome, Firefox, Safari, Edge, Opera */
    @Column(name = "news_views_browser_name", length = 50)
    private String newsViewsBrowserName;

    /** Browser version */
    @Column(name = "news_views_browser_version", length = 30)
    private String newsViewsBrowserVersion;

    /** Operating system name: Windows, macOS, iOS, Android, Linux */
    @Column(name = "news_views_os_name", length = 50)
    private String newsViewsOsName;

    /** Operating system version */
    @Column(name = "news_views_os_version", length = 30)
    private String newsViewsOsVersion;

    // ========================================
    // Location Information
    // ========================================

    /** ISO 3166-1 alpha-2 country code (e.g., US, GB, IN) */
    @Column(name = "news_views_country_code", length = 5)
    private String newsViewsCountryCode;

    /** City name from GeoIP */
    @Column(name = "news_views_city", length = 100)
    private String newsViewsCity;

    /** IANA timezone (e.g., America/New_York) */
    @Column(name = "news_views_timezone", length = 60)
    private String newsViewsTimezone;

    // ========================================
    // Security Context
    // ========================================

    /** Whether the request appears to be from a bot */
    @Builder.Default
    @Column(name = "news_views_is_bot")
    private Boolean newsViewsIsBot = false;

    /** Whether using VPN/Proxy/TOR */
    @Builder.Default
    @Column(name = "news_views_is_anonymized")
    private Boolean newsViewsIsAnonymized = false;

    // ========================================
    // Request Context
    // ========================================

    /** Access channel: WEB, MOBILE_WEB, IOS_APP, ANDROID_APP, API */
    @Column(name = "news_views_channel", length = 20)
    private String newsViewsChannel;

    /** User's preferred language (from Accept-Language) */
    @Column(name = "news_views_language", length = 10)
    private String newsViewsLanguage;

    /** Referrer domain (where user came from) */
    @Column(name = "news_views_referer_domain", length = 255)
    private String newsViewsRefererDomain;

    // ========================================
    // Timestamp
    // ========================================

    @Column(name = "news_views_viewed_at")
    private Instant newsViewsViewedAt;
}
