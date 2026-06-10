package com.mmva.newsapp.domain.newsengagement.bookmarks.model;

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
 * News bookmark entity for tracking user bookmarks.
 * 
 * <p>
 * Captures client context for analytics and sync across devices.
 * </p>
 * 
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "news_bookmarks", uniqueConstraints = {
        @UniqueConstraint(name = "uq_news_bookmarks_user_news", columnNames = { "news_bookmarks_user_id",
                "news_bookmarks_news_id" })
}, indexes = {
        @Index(name = "idx_news_bookmarks_user_id", columnList = "news_bookmarks_user_id"),
        @Index(name = "idx_news_bookmarks_news_id", columnList = "news_bookmarks_news_id"),
        @Index(name = "idx_news_bookmarks_country_code", columnList = "news_bookmarks_country_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "news_bookmarks_id", updatable = false, nullable = false)
    private UUID newsBookmarksId;

    @Column(name = "news_bookmarks_user_id", nullable = false)
    private UUID newsBookmarksUserId;

    @Column(name = "news_bookmarks_news_id")
    private UUID newsBookmarksNewsId;

    /**
     * Reference to NewsMasterEntity for cascade delete.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_bookmarks_news_id", referencedColumnName = "news_news_id", insertable = false, updatable = false, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private NewsMasterEntity news;

    @Column(name = "news_bookmarks_folder_name", length = 255)
    private String newsBookmarksFolderName;

    // ========================================
    // Device Information
    // ========================================

    /** Device type: MOBILE, TABLET, DESKTOP, TV, WEARABLE, BOT, UNKNOWN */
    @Column(name = "news_bookmarks_device_type", length = 20)
    private String newsBookmarksDeviceType;

    /** Device fingerprint for unique device tracking */
    @Column(name = "news_bookmarks_device_fingerprint", length = 128)
    private String newsBookmarksDeviceFingerprint;

    // ========================================
    // Location Information
    // ========================================

    /** ISO 3166-1 alpha-2 country code (e.g., US, GB, IN) */
    @Column(name = "news_bookmarks_country_code", length = 5)
    private String newsBookmarksCountryCode;

    /** City name from GeoIP */
    @Column(name = "news_bookmarks_city", length = 100)
    private String newsBookmarksCity;

    // ========================================
    // Request Context
    // ========================================

    /** Access channel: WEB, MOBILE_WEB, IOS_APP, ANDROID_APP, API */
    @Column(name = "news_bookmarks_channel", length = 20)
    private String newsBookmarksChannel;

    // ========================================
    // Timestamp
    // ========================================

    @Column(name = "news_bookmarks_bookmarked_at")
    private Instant newsBookmarksBookmarkedAt;

    // ...existing code...
}
