package com.mmva.newsapp.infrastructure.push.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import com.mmva.newsapp.infrastructure.push.enums.PushTopicCategory;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Push notification topic subscription entity.
 * 
 * <h3>Purpose:</h3>
 * <p>
 * Tracks which devices are subscribed to which FCM topics.
 * Topics allow efficient broadcast to large groups without
 * managing individual device tokens.
 * </p>
 * 
 * <h3>Standard Topic Naming:</h3>
 * <ul>
 * <li>{@code all_news} - All news updates</li>
 * <li>{@code breaking_news} - Breaking news alerts</li>
 * <li>{@code category_{slug}} - Category-specific (e.g., category_sports)</li>
 * <li>{@code language_{code}} - Language-specific (e.g., language_es)</li>
 * <li>{@code platform_{type}} - Platform-specific (e.g., platform_android)</li>
 * <li>{@code digest_daily} - Daily digest subscribers</li>
 * <li>{@code digest_weekly} - Weekly digest subscribers</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@SuperBuilder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "push_topic_subscriptions", uniqueConstraints = {
                @UniqueConstraint(name = "uk_push_topic_device", columnNames = { "device_id", "topic" })
}, indexes = {
                @Index(name = "idx_push_topic_sub_device_id", columnList = "device_id"),
                @Index(name = "idx_push_topic_sub_topic", columnList = "topic"),
                @Index(name = "idx_push_topic_sub_is_active", columnList = "is_active"),
                @Index(name = "idx_push_topic_sub_deleted_at", columnList = "deleted_at")
})
public class PushTopicSubscription extends BaseAuditEntity {

        // ========================================
        // Primary Key
        // ========================================

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "subscription_id", nullable = false)
        private UUID subscriptionId;

        // ========================================
        // Subscription Details
        // ========================================

        /**
         * Device that subscribed to the topic.
         */
        @Column(name = "device_id", nullable = false)
        private UUID deviceId;

        /**
         * FCM topic name.
         * Following naming convention: {type}_{identifier}
         * Examples: breaking_news, category_sports, language_es
         */
        @Column(name = "topic", nullable = false, length = 100)
        private String topic;

        /**
         * Human-readable topic display name.
         */
        @Column(name = "topic_display_name", length = 255)
        private String topicDisplayName;

        /**
         * Topic category for grouping.
         */
        @Enumerated(EnumType.STRING)
        @Column(name = "topic_category", length = 50)
        private PushTopicCategory topicCategory;

        // ========================================
        // Status
        // ========================================

        /**
         * Whether the subscription is currently active.
         */
        @Column(name = "is_active", nullable = false)
        @Builder.Default
        private Boolean isActive = true;

        /**
         * Whether subscription was confirmed by FCM.
         */
        @Column(name = "is_synced", nullable = false)
        @Builder.Default
        private Boolean isSynced = false;

        /**
         * When subscription was synced with FCM.
         */
        @Column(name = "synced_at")
        private Instant syncedAt;

        /**
         * Error message if sync failed.
         */
        @Column(name = "sync_error", length = 500)
        private String syncError;
}
