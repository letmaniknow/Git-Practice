package com.mmva.newsapp.infrastructure.rbac.role.core.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import com.mmva.newsapp.infrastructure.rbac.permission.core.model.RbacPermission;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Role entity for RBAC (Role-Based Access Control).
 * 
 * <p>
 * Defines roles that can be assigned to admindashboard users, with associated
 * permissions.
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
@Table(name = "rbac_roles", indexes = {
        @Index(name = "idx_rbac_roles_deleted_at", columnList = "deleted_at")
})
public class RbacRole extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @UuidGenerator
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    // ========================================
    // Core Fields
    // ========================================

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    @Column(name = "role_description", length = 255)
    private String roleDescription;

    // ========================================
    // Status Flags
    // ========================================

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // ========================================
    // Relationships
    // ========================================

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "rbac_role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<RbacPermission> permissions = new HashSet<>();
}
