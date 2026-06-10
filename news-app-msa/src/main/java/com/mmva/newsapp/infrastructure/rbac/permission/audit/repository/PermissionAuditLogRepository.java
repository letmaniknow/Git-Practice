package com.mmva.newsapp.infrastructure.rbac.permission.audit.repository;

import com.mmva.newsapp.infrastructure.rbac.permission.audit.model.RbacPermissionAuditLog;
import com.mmva.newsapp.infrastructure.common.audit.repository.UnifiedAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionAuditLogRepository extends UnifiedAuditLogRepository<RbacPermissionAuditLog> {
    List<RbacPermissionAuditLog> findByPermissionId(UUID permissionId);

    Page<RbacPermissionAuditLog> findByPermissionIdOrderByCreatedAtDesc(UUID permissionId, Pageable pageable);
}
