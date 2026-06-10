package com.mmva.newsapp.infrastructure.common.audit.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Base entity with audit fields for tracking record lifecycle.
 * 
 * <p>
 * Provides automatic population of audit fields via Spring Data JPA Auditing:
 * </p>
 * <ul>
 * <li>{@code createdAt} - Auto-set on insert</li>
 * <li>{@code createdBy} - Auto-set on insert (from AuditorAware)</li>
 * <li>{@code updatedAt} - Auto-set on insert and update</li>
 * <li>{@code updatedBy} - Auto-set on insert and update (from
 * AuditorAware)</li>
 * <li>{@code deletedAt} - Manually set during soft delete</li>
 * <li>{@code deletedBy} - Manually set during soft delete</li>
 * </ul>
 * 
 * <h2>Usage:</h2>
 * 
 * <pre>
 * {
 *     &#64;code
 *     &#64;Entity
 *     &#64;Table(name = "my_table")
 *     public class MyEntity extends BaseAuditEntity {
 *         @Id
 *         private UUID id;
 *         // ... entity-specific fields
 *     }
 * }
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {

    // ========================================
    // Creation Audit Fields (Auto-populated)
    // ========================================

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    // ========================================
    // Modification Audit Fields (Auto-populated)
    // ========================================

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    // ========================================
    // Soft Delete Fields (Manually set)
    // ========================================

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    // ========================================
    // Soft Delete Helper Methods
    // ========================================

    /**
     * Checks if this entity has been soft deleted.
     * 
     * @return true if deletedAt is set, false otherwise
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Performs a soft delete by setting deletedAt and deletedBy.
     * 
     * @param deletedBy the UUID of the user performing the delete
     */
    public void softDelete(UUID deletedBy) {
        this.deletedAt = Instant.now();
        this.deletedBy = deletedBy;
    }

    /**
     * Restores a soft-deleted entity by clearing delete fields.
     */
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }
}
