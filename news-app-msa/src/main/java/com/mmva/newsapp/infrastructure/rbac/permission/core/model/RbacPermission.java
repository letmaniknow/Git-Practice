package com.mmva.newsapp.infrastructure.rbac.permission.core.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Permission entity for RBAC (Role-Based Access Control).
 * 
 * <p>
 * Defines granular permissions that can be assigned to roles.
 * </p>
 * 
 * <p>
 * Soft-delete filtering is handled via {@code SoftDeleteSpec} in repository
 * queries.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rbac_permissions", indexes = {
        @Index(name = "idx_rbac_permissions_deleted_at", columnList = "deleted_at")
})
public class RbacPermission extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    // ========================================
    // Core Fields
    // ========================================

    @Column(name = "permission_name", unique = true, nullable = false, length = 100)
    private String permissionName;

    @Column(name = "permission_description", length = 255)
    private String permissionDescription;

    // ========================================
    // Status Flags
    // ========================================

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
