package com.mmva.newsapp.infrastructure.rbac.role.audit.repository;

import com.mmva.newsapp.infrastructure.rbac.role.audit.model.RbacRoleAuditLog;
import com.mmva.newsapp.infrastructure.common.audit.repository.UnifiedAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleAuditLogRepository extends UnifiedAuditLogRepository<RbacRoleAuditLog> {

        List<RbacRoleAuditLog> findByRoleId(UUID roleId);

        @Query("SELECT ral FROM RbacRoleAuditLog ral WHERE ral.roleId = :roleId " +
                        "AND (:action IS NULL OR ral.action = :action) " +
                        "AND (:fromDate IS NULL OR ral.createdAt >= :fromDate) " +
                        "AND (:toDate IS NULL OR ral.createdAt <= :toDate) " +
                        "ORDER BY ral.createdAt DESC")
        List<RbacRoleAuditLog> findByRoleIdWithFilters(
                        @Param("roleId") UUID roleId,
                        @Param("action") String action,
                        @Param("fromDate") String fromDate,
                        @Param("toDate") String toDate);

        @Query("SELECT ral FROM RbacRoleAuditLog ral WHERE ral.roleId = :roleId " +
                        "AND (:action IS NULL OR ral.action = :action) " +
                        "AND (:fromDate IS NULL OR ral.createdAt >= :fromDate) " +
                        "AND (:toDate IS NULL OR ral.createdAt <= :toDate)")
        Page<RbacRoleAuditLog> findByRoleIdWithFiltersPaginated(
                        @Param("roleId") UUID roleId,
                        @Param("action") String action,
                        @Param("fromDate") String fromDate,
                        @Param("toDate") String toDate,
                        Pageable pageable);

        Page<RbacRoleAuditLog> findByRoleIdOrderByCreatedAtDesc(UUID roleId, Pageable pageable);
}
