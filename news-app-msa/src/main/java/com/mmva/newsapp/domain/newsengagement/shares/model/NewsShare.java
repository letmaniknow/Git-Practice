package com.mmva.newsapp.domain.newsengagement.shares.model;

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
 * News share entity for tracking newsapp shares.
 * 
 * <p>
 * Table: news_shares
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * <p>
 * Captures client context for analytics and share attribution.
 * </p>
 * <p>
 * <strong>Design Note:</strong> Shares are EVENTS, not STATES. Unlike likes
 * (binary: liked/not liked), a user can share the same article multiple times
 * to different platforms or at different times. Each share is a discrete event
 * for analytics and attribution tracking.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "news_shares", indexes = {
                @Index(name = "idx_news_shares_news_id", columnList = "news_shares_news_id"),
                @Index(name = "idx_news_shares_user_id", columnList = "news_shares_user_id"),
                @Index(name = "idx_news_shares_country_code", columnList = "news_shares_country_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsShare {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "news_shares_id")
        private Long newsSharesId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "news_shares_news_id", referencedColumnName = "news_news_id", nullable = false, insertable = false, updatable = false)
        @OnDelete(action = OnDeleteAction.CASCADE)
        private NewsMasterEntity news;

        @Column(name = "news_shares_news_id", nullable = false)
        private UUID newsSharesNewsId;

        @Column(name = "news_shares_user_id")
        private UUID newsSharesUserId;

        // ========================================
        // Network Information
        // ========================================

        @Column(name = "news_shares_ip_address", length = 100)
        private String newsSharesIpAddress;

        // ========================================
        // Device Information
        // ========================================

        /** Device type: MOBILE, TABLET, DESKTOP, TV, WEARABLE, BOT, UNKNOWN */
        @Column(name = "news_shares_device_type", length = 20)
        private String newsSharesDeviceType;

        /** Device fingerprint for unique device tracking */
        @Column(name = "news_shares_device_fingerprint", length = 128)
        private String newsSharesDeviceFingerprint;

        // ========================================
        // Location Information
        // ========================================

        /** ISO 3166-1 alpha-2 country code (e.g., US, GB, IN) */
        @Column(name = "news_shares_country_code", length = 5)
        private String newsSharesCountryCode;

        /** City name from GeoIP */
        @Column(name = "news_shares_city", length = 100)
        private String newsSharesCity;

        // ========================================
        // Share Context
        // ========================================

        /**
         * Share infrastructure: TWITTER, FACEBOOK, LINKEDIN, WHATSAPP, EMAIL, COPY_LINK
         */
        @Column(name = "news_shares_platform", length = 30)
        private String newsSharesPlatform;

        /** Access channel: WEB, MOBILE_WEB, IOS_APP, ANDROID_APP, API */
        @Column(name = "news_shares_channel", length = 20)
        private String newsSharesChannel;

        // ========================================
        // Timestamp
        // ========================================

        @Column(name = "news_shares_shared_at")
        private Instant newsSharesSharedAt;
}
