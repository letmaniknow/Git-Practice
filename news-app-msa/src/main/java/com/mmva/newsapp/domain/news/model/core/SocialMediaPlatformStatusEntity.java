package com.mmva.newsapp.domain.news.model.core;

// ===============================
// Core Java Imports
// ===============================
import java.time.Instant;
import java.util.UUID;

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

/**
 * Social Media Platform Status Entity - Individual platform sharing status.
 *
 * <p>
 * This entity tracks the sharing status for each individual platform
 * for a news article. Each platform gets its own record for better
 * querying and analytics capabilities.
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
@Table(name = "social_media_platform_status", indexes = {
        @Index(name = "idx_platform_status_sharing_id", columnList = "sharing_id"),
        @Index(name = "idx_platform_status_platform", columnList = "platform"),
        @Index(name = "idx_platform_status_status", columnList = "status"),
        @Index(name = "idx_platform_status_shared_at", columnList = "shared_at"),
        @Index(name = "uk_sharing_platform", columnList = "sharing_id,platform", unique = true)
})
public class SocialMediaPlatformStatusEntity {

    // ===============================
    // PRIMARY IDENTIFIER
    // ===============================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "status_id")
    private UUID statusId;

    // ===============================
    // RELATIONSHIPS
    // ===============================

    /**
     * Relationship to the main sharing record.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sharing_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SocialMediaShareEntity sharing;

    // ===============================
    // PLATFORM DETAILS
    // ===============================

    /**
     * The social media platform (WHATSAPP, FACEBOOK, etc.)
     */
    @Column(name = "platform", nullable = false, length = 50)
    private String platform;

    /**
     * Current status of sharing for this platform.
     * Values: PENDING, COMPLETED, FAILED, SKIPPED
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * When this platform was actually shared.
     * Null if not yet shared.
     */
    @Column(name = "shared_at")
    private Instant sharedAt;

    /**
     * ID of the editor/admin who marked this platform as shared.
     * Null if not yet shared or shared automatically.
     */
    @Column(name = "shared_by")
    private UUID sharedBy;

    /**
     * Optional notes about the sharing (e.g., "Shared at 9:15 AM", "Used image
     * variant 2")
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ===============================
    // TIMESTAMPS
    // ===============================

    /**
     * When this status record was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * When this status record was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ===============================
    // BUSINESS LOGIC METHODS
    // ===============================

    /**
     * Check if this platform sharing is completed.
     */
    public boolean isCompleted() {
        return SocialMediaShareEntity.STATUS_COMPLETED.equals(this.status);
    }

    /**
     * Check if this platform sharing is pending.
     */
    public boolean isPending() {
        return SocialMediaShareEntity.STATUS_PENDING.equals(this.status);
    }

    /**
     * Mark this platform as completed with optional timestamp and notes.
     */
    public void markCompleted(Instant sharedAt, String notes) {
        this.status = SocialMediaShareEntity.STATUS_COMPLETED;
        this.sharedAt = sharedAt != null ? sharedAt : Instant.now();
        this.notes = notes;
    }

    /**
     * Mark this platform as completed.
     */
    public void markCompleted() {
        markCompleted(Instant.now(), null);
    }
}