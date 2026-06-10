package com.mmva.newsapp.domain.newsengagement.likes.model;

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
 * News like entity for tracking user likes on articles.
 * 
 * <p>
 * Table: news_likes
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * <p>
 * Captures client context for analytics and fraud prevention.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "news_likes", uniqueConstraints = {
                @UniqueConstraint(name = "uq_news_likes_news_user", columnNames = { "news_likes_news_id",
                                "news_likes_user_id" })
}, indexes = {
                @Index(name = "idx_news_likes_news_id", columnList = "news_likes_news_id"),
                @Index(name = "idx_news_likes_user_id", columnList = "news_likes_user_id"),
                @Index(name = "idx_news_likes_country_code", columnList = "news_likes_country_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsLike {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "news_likes_id", updatable = false, nullable = false)
        private Long newsLikesId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "news_likes_news_id", referencedColumnName = "news_news_id", nullable = false, insertable = false, updatable = false)
        @OnDelete(action = OnDeleteAction.CASCADE)
        private NewsMasterEntity news;

        @Column(name = "news_likes_news_id", nullable = false)
        private UUID newsLikesNewsId;

        @Column(name = "news_likes_user_id")
        private UUID newsLikesUserId;

        // ========================================
        // Network Information
        // ========================================

        @Column(name = "news_likes_ip_address", length = 100)
        private String newsLikesIpAddress;

        // ========================================
        // Device Information
        // ========================================

        /** Device type: MOBILE, TABLET, DESKTOP, TV, WEARABLE, BOT, UNKNOWN */
        @Column(name = "news_likes_device_type", length = 20)
        private String newsLikesDeviceType;

        /** Device fingerprint for unique device tracking */
        @Column(name = "news_likes_device_fingerprint", length = 128)
        private String newsLikesDeviceFingerprint;

        // ========================================
        // Location Information
        // ========================================

        /** ISO 3166-1 alpha-2 country code (e.g., US, GB, IN) */
        @Column(name = "news_likes_country_code", length = 5)
        private String newsLikesCountryCode;

        /** City name from GeoIP */
        @Column(name = "news_likes_city", length = 100)
        private String newsLikesCity;

        // ========================================
        // Request Context
        // ========================================

        /** Access channel: WEB, MOBILE_WEB, IOS_APP, ANDROID_APP, API */
        @Column(name = "news_likes_channel", length = 20)
        private String newsLikesChannel;

        // ========================================
        // Timestamp
        // ========================================

        @Column(name = "news_likes_liked_at")
        private Instant newsLikesLikedAt;
}
