package com.mmva.newsapp.domain.newsengagement.comments.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * News comment like/dislike entity.
 * 
 * <p>
 * Tracks user likes and dislikes on comments with client context.
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
@Table(name = "news_comment_likes", indexes = {
        @Index(name = "idx_news_comment_likes_comment_id", columnList = "news_comment_likes_comment_id"),
        @Index(name = "idx_news_comment_likes_user_id", columnList = "news_comment_likes_user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsCommentLike {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "news_comment_likes_id", updatable = false, nullable = false)
    private UUID newsCommentLikesId;

    // ========================================
    // Relationships
    // ========================================

    @Column(name = "news_comment_likes_comment_id", nullable = false)
    private UUID newsCommentLikesCommentId;

    @Column(name = "news_comment_likes_user_id", nullable = false)
    private UUID newsCommentLikesUserId;

    // ========================================
    // Like State
    // ========================================

    @Column(name = "news_comment_likes_liked", nullable = false)
    private boolean newsCommentLikesLiked;

    // ========================================
    // Client Context
    // ========================================

    /** Device type: MOBILE, TABLET, DESKTOP */
    @Column(name = "news_comment_likes_device_type", length = 20)
    private String newsCommentLikesDeviceType;

    /** ISO 3166-1 alpha-2 country code */
    @Column(name = "news_comment_likes_country_code", length = 5)
    private String newsCommentLikesCountryCode;

    /** Access channel: WEB, MOBILE_WEB, IOS_APP, ANDROID_APP */
    @Column(name = "news_comment_likes_channel", length = 20)
    private String newsCommentLikesChannel;

    // ========================================
    // Timestamp
    // ========================================

    @Column(name = "news_comment_likes_liked_at")
    private Instant newsCommentLikesLikedAt;
}
