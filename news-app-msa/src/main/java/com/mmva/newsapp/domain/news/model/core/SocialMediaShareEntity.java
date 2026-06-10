package com.mmva.newsapp.domain.news.model.core;

// ===============================
// Core Java Imports
// ===============================
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

// ===============================
// Spring Framework Imports
// ===============================
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// ===============================
// JPA Imports
// ===============================
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// ===============================
// Lombok Imports
// ===============================
import lombok.*;
import lombok.experimental.SuperBuilder;

// ===============================
// Project Imports
// ===============================

/**
 * Social Media Share Entity - Main record for social media sharing tracking.
 *
 * <p>
 * This entity represents the main sharing record for each news article.
 * Platform-specific statuses are stored in related
 * SocialMediaPlatformStatusEntity records.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "social_media_share", indexes = {
        @Index(name = "idx_social_media_share_news_id", columnList = "news_id"),
        @Index(name = "idx_social_media_share_enabled", columnList = "sharing_enabled"),
        @Index(name = "idx_social_media_share_created_at", columnList = "created_at")
})
public class SocialMediaShareEntity {

    // ===============================
    // PRIMARY IDENTIFIER
    // ===============================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sharing_id")
    private UUID sharingId;

    // ===============================
    // RELATIONSHIPS
    // ===============================

    /**
     * Relationship to the news article.
     * Lazy-loaded to avoid unnecessary joins.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", referencedColumnName = "news_news_id", nullable = false, insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private NewsMasterEntity news;

    /**
     * Platform-specific status records.
     * One-to-many relationship with platform statuses.
     */
    @OneToMany(mappedBy = "sharing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SocialMediaPlatformStatusEntity> platformStatuses;

    // ===============================
    // FOREIGN KEY
    // ===============================

    /**
     * Reference to the news article.
     * Foreign key to news_master.news_id
     */
    @Column(name = "news_id", nullable = false)
    private UUID newsId;

    // ===============================
    // SHARING CONFIGURATION
    // ===============================

    /**
     * Whether social media sharing is enabled for this news article.
     * Defaults to true - all news articles are shared by default.
     */
    @Builder.Default
    @Column(name = "sharing_enabled", nullable = false)
    private Boolean sharingEnabled = true;

    /**
     * Target social media platforms for sharing.
     * Stored as comma-separated values.
     * Defaults to all supported platforms for maximum reach.
     */
    @Builder.Default
    @Column(name = "target_platforms", length = 500)
    private String targetPlatforms = "WHATSAPP,FACEBOOK,TWITTER,INSTAGRAM,LINKEDIN,TELEGRAM,TIKTOK,YOUTUBE";

    // ===============================
    // TIMESTAMPS
    // ===============================

    /**
     * When this sharing record was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * When this sharing record was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ===============================
    // BUSINESS LOGIC METHODS
    // ===============================

    /**
     * Get list of target platforms.
     */
    public List<String> getTargetPlatformsList() {
        if (targetPlatforms == null || targetPlatforms.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(targetPlatforms.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Check if sharing is fully completed (all target platforms shared).
     */
    public boolean isFullyShared() {
        List<String> targets = getTargetPlatformsList();
        if (platformStatuses == null || platformStatuses.isEmpty()) {
            return false;
        }

        Map<String, String> statusMap = platformStatuses.stream()
                .collect(Collectors.toMap(
                        SocialMediaPlatformStatusEntity::getPlatform,
                        SocialMediaPlatformStatusEntity::getStatus));

        return targets.stream()
                .allMatch(platform -> STATUS_COMPLETED.equals(statusMap.get(platform)));
    }

    /**
     * Get count of completed platforms.
     */
    public int getCompletedPlatformsCount() {
        if (platformStatuses == null)
            return 0;

        return (int) platformStatuses.stream()
                .filter(status -> STATUS_COMPLETED.equals(status.getStatus()))
                .count();
    }

    /**
     * Get count of pending platforms.
     */
    public int getPendingPlatformsCount() {
        List<String> targets = getTargetPlatformsList();
        if (platformStatuses == null)
            return targets.size();

        Map<String, String> statusMap = platformStatuses.stream()
                .collect(Collectors.toMap(
                        SocialMediaPlatformStatusEntity::getPlatform,
                        SocialMediaPlatformStatusEntity::getStatus));

        return (int) targets.stream()
                .filter(platform -> !STATUS_COMPLETED.equals(statusMap.get(platform)))
                .count();
    }

    /**
     * Initialize platform status records for all target platforms.
     */
    public void initializePlatformStatuses() {
        if (platformStatuses == null) {
            platformStatuses = new ArrayList<>();
        }

        List<String> targets = getTargetPlatformsList();
        for (String platform : targets) {
            // Check if status already exists
            boolean exists = platformStatuses.stream()
                    .anyMatch(status -> platform.equals(status.getPlatform()));

            if (!exists) {
                SocialMediaPlatformStatusEntity status = SocialMediaPlatformStatusEntity.builder()
                        .sharing(this)
                        .platform(platform)
                        .status(STATUS_PENDING)
                        .build();
                platformStatuses.add(status);
            }
        }
    }

    // ===============================
    // CONSTANTS
    // ===============================

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_SKIPPED = "SKIPPED";

    public static final List<String> SUPPORTED_PLATFORMS = Arrays.asList(
            "WHATSAPP", "FACEBOOK", "TWITTER", "INSTAGRAM",
            "LINKEDIN", "TELEGRAM", "TIKTOK", "YOUTUBE");
}